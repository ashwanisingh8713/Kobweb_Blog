package com.example.blogmultiplatform.signaling

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.js.JSON
import kotlin.js.json
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.WebSocket

@Suppress("unused") // This file provides a client used from the site UI; suppress unused warnings
private val scope = MainScope()
private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class WSMessage(val type: String, val from: String? = null, val to: String? = null, val payload: String? = null)

@Suppress("unused")
class SignalingClient(private val roomId: String, private val clientId: String) {
    private val ws: WebSocket
    private var pc: dynamic = null
    private var localStream: dynamic = null
    private var remoteVideo: HTMLVideoElement? = null
    private val configuration = js("({ iceServers: [{ urls: ['stun:stun.l.google.com:19302'] }] })")

    init {
        val wsProto = if (window.location.protocol == "https:") "wss" else "ws"
        val host = window.location.host
        val url = "$wsProto://$host/signaling/$roomId"
        // Use the platform WebSocket constructor instead of js() with interpolation
        ws = WebSocket(url)
        ws.onopen = {
            console.log("WS open")
        }
        ws.onmessage = { ev: dynamic ->
            val text = ev.data as String
            val msg = json.decodeFromString(WSMessage.serializer(), text)
            handleMessage(msg)
        }
    }

    private fun send(msg: WSMessage) {
        ws.send(json.encodeToString(WSMessage.serializer(), msg))
    }

    fun sendChat(text: String) {
        val msg = WSMessage(type = "chat", from = clientId, payload = text)
        send(msg)
    }

    fun startLocalMedia(remoteVideoElement: HTMLVideoElement, audio: Boolean = true, video: Boolean = true) {
        remoteVideo = remoteVideoElement
        // Build constraints with kotlin.js.json to avoid non-constant js() strings
        val constraints = json("audio" to audio, "video" to video)
        scope.launch {
            val promise = window.navigator.asDynamic().mediaDevices.getUserMedia(constraints)
            promise.then { stream: dynamic ->
                localStream = stream
                val local = document.getElementById("localVideo") as? HTMLVideoElement
                local?.asDynamic()?.srcObject = stream
            }
        }
    }

    fun createPeerConnection() {
        // reference configuration so it's not flagged unused
        val _cfg = configuration
        pc = js("new RTCPeerConnection(configuration)")
        pc.onicecandidate = { event: dynamic ->
            val candidate = event.asDynamic().candidate
            if (candidate != null) {
                val payload = JSON.stringify(candidate)
                send(WSMessage(type = "ice", from = clientId, payload = payload))
            }
        }
        pc.ontrack = { ev: dynamic ->
            val streams = ev.asDynamic().streams
            if (streams != null && streams.length > 0) {
                remoteVideo?.asDynamic()?.srcObject = streams[0]
            }
        }
        if (localStream != null) {
            val tracks = localStream.getTracks() as Array<dynamic>
            for (t in tracks) pc.asDynamic().addTrack(t, localStream)
        }
    }

    fun call() {
        createPeerConnection()
        scope.launch {
            val offerPromise = pc.createOffer()
            offerPromise.then { offer: dynamic ->
                pc.setLocalDescription(offer)
                val payload = JSON.stringify(offer)
                send(WSMessage(type = "offer", from = clientId, payload = payload))
            }
        }
    }

    private fun handleMessage(msg: WSMessage) {
        when (msg.type) {
            "chat" -> console.log("Chat from ${'$'}{msg.from}: ${'$'}{msg.payload}")
            "offer" -> {
                createPeerConnection()
                val offer = JSON.parse<dynamic>(msg.payload ?: "{}")
                pc.setRemoteDescription(offer)
                val answerPromise = pc.createAnswer()
                answerPromise.then { answer: dynamic ->
                    pc.setLocalDescription(answer)
                    send(WSMessage(type = "answer", from = clientId, payload = JSON.stringify(answer)))
                }
            }
            "answer" -> {
                val answer = JSON.parse<dynamic>(msg.payload ?: "{}")
                pc.setRemoteDescription(answer)
            }
            "ice" -> {
                val ice = JSON.parse<dynamic>(msg.payload ?: "{}")
                pc.addIceCandidate(ice)
            }
            else -> console.log("Unknown message: ${'$'}{msg.type}")
        }
    }
}
