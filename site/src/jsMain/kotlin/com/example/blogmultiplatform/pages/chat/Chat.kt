package com.example.blogmultiplatform.pages.chat

import androidx.compose.runtime.*
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.example.shared.JsTheme
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.js.Date
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.mediacapture.MediaStream
import kotlin.js.Promise
import kotlin.random.Random
import com.varabyte.kobweb.compose.ui.modifiers.id

@Page("/chat")
@Composable
fun ChatScreen() {
    // Debug mount
    js("console.log('ChatScreen mounted')")

    // UI state (in-memory per tab; no persistence)
    data class ChatMessage(val text: String, val incoming: Boolean, val ts: Long = Date().getTime().toLong())

    var messages by remember { mutableStateOf(listOf(ChatMessage("Welcome to the chat!", incoming = true))) }
    var chatShareEnabled by remember { mutableStateOf(false) }

    // WebRTC state
    var localStream by remember { mutableStateOf<MediaStream?>(null) }
    var pc by remember { mutableStateOf<dynamic?>(null) }
    val inputId = "chatInput"
    val localVideoId = "localVideo"
    val remoteVideoId = "remoteVideo"
    val localContainerId = "localVideoContainer"
    val remoteContainerId = "remoteVideoContainer"

    // client id to ignore own BroadcastChannel messages
    val clientId = remember { Random.nextInt(0, 1_000_000_000).toString() }
    val bc = remember {
        try {
            val ch = js("new BroadcastChannel('webrtc-signaling')")
            js("console.log('BroadcastChannel created')")
            ch
        } catch (_: Throwable) {
            js("console.log('BroadcastChannel not available')")
            null
        }
    }

    // prevent adding same tracks multiple times
    var tracksAdded by remember { mutableStateOf(false) }

    // Optional BroadcastChannel for chat sharing across tabs (separate channel from signaling)
    val bcChat = remember {
        try {
            val ch = js("new BroadcastChannel('webrtc-chat')")
            js("console.log('BroadcastChannel chat created')")
            ch
        } catch (_: Throwable) {
            js("console.log('BroadcastChannel chat not available')")
            null
        }
    }

    // Listen for incoming chat messages on bcChat
    DisposableEffect(bcChat) {
        val handler = fun(ev: dynamic) {
            try {
                val data = ev.data
                if (data == null) return
                val t = data.type as? String
                if (t == "chat") {
                    val from = data.from as? String
                    // ignore our own messages
                    if (from == clientId) return
                    val text = data.text as? String ?: ""
                    if (text.isNotEmpty()) {
                        messages = messages + ChatMessage(text, incoming = true, ts = (data.time as? Number)?.toLong() ?: Date().getTime().toLong())
                    }
                }
            } catch (e: Throwable) {
                console.log("bcChat handler error:", e)
            }
        }
        if (bcChat != null) bcChat.onmessage = handler
        onDispose { try { bcChat?.close() } catch (_: Throwable) {} }
    }

    // Ensure a <video> element exists and return it (create inside container if needed)
    fun ensureVideoElement(id: String, containerId: String): HTMLVideoElement? {
        var v = document.getElementById(id) as? HTMLVideoElement
        if (v == null) {
            // create a video element inside the container if container exists
            val container = document.getElementById(containerId)
            val videoHtml = "<video id=\"${id}\" autoplay playsinline style=\"width:100%;height:100%;object-fit:cover;border-radius:8px;\"></video>"
            if (container != null) {
                container.innerHTML = videoHtml
            } else {
                // fallback append to body
                val div = document.createElement("div")
                div.setAttribute("id", "container_$id")
                div.innerHTML = videoHtml
                document.body?.appendChild(div)
            }
            v = document.getElementById(id) as? HTMLVideoElement
        }
        return v
    }

    fun createPeerConnection() {
        if (pc != null) return
        val connection = js("new RTCPeerConnection({ iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] })")

        connection.onicecandidate = { evt: dynamic ->
            try {
                val cand = evt?.candidate
                if (cand != null && bc != null) {
                    // Build a plain cloneable candidate object
                    val candObj = js("({})")
                    candObj.candidate = cand.candidate
                    candObj.sdpMid = cand.sdpMid
                    candObj.sdpMLineIndex = cand.sdpMLineIndex
                    val msg = js("({})")
                    msg.type = "ice"
                    msg.candidate = candObj
                    msg.from = clientId
                    bc.postMessage(msg)
                }
            } catch (e: dynamic) {
                console.log("ice candidate post failed:", e)
            }
        }

        connection.ontrack = { evt: dynamic ->
             try {
                 val streamsArr = (evt.streams as? Array<dynamic>)
                 val stream = streamsArr?.getOrNull(0) ?: (evt.stream as? MediaStream)

                 val v = ensureVideoElement(remoteVideoId, remoteContainerId)
                 if (v != null && stream != null) {
                     v.srcObject = stream
                     v.muted = false
                     v.playsInline = true
                     v.play()
                 }
             } catch (e: Throwable) {
                 console.log("ontrack error:", e)
             }
         }

         pc = connection
     }

    fun closePeer() {
        try { pc?.close() } catch (_: Throwable) {}
        pc = null
        val rv = document.getElementById(remoteVideoId) as? HTMLVideoElement
        if (rv != null) rv.srcObject = null
    }

    // BroadcastChannel message handling
    DisposableEffect(bc) {
        val handler = fun(ev: dynamic) {
            try {
                js("console.log('signaling message received')")
                val data = ev.data
                if (data == null) return
                val from = data.from as String?
                if (from == clientId) return
                val type = data.type as String
                js("console.log('signaling type: ' + type)")
                when (type) {
                    "offer" -> {
                        createPeerConnection()
                        // add local tracks if present
                        // add local tracks only once
                        if (!tracksAdded) {
                            localStream?.let { s ->
                                val tracks = (s.getTracks()).unsafeCast<Array<dynamic>>()
                                for (track in tracks) {
                                    try {
                                        pc.addTrack(track, s)
                                    } catch (e: dynamic) {
                                        console.log("addTrack failed (offer handler):", e)
                                    }
                                }
                            }
                            tracksAdded = true
                        }
                        // remote description expects an object { sdp, type }
                        val offerSdp = data.sdp as String?
                        val offerType = data.sdpType as String? ?: "offer"
                        val offerDesc = js("({})")
                        offerDesc.sdp = offerSdp
                        offerDesc.type = offerType
                        pc.setRemoteDescription(offerDesc).then {
                            pc.createAnswer().then { ans: dynamic ->
                                pc.setLocalDescription(ans).then {
                                    val msg = js("({})")
                                    msg.type = "answer"
                                    // send plain sdp and type so it's cloneable
                                    msg.sdp = ans.sdp
                                    msg.sdpType = ans.type
                                    msg.from = clientId
                                    bc.postMessage(msg)
                                }
                            }
                        }
                    }
                    "answer" -> {
                        val answerSdp = data.sdp as String?
                        val answerType = data.sdpType as String? ?: "answer"
                        val answerDesc = js("({})")
                        answerDesc.sdp = answerSdp
                        answerDesc.type = answerType
                        pc?.setRemoteDescription(answerDesc)
                    }
                    "ice" -> {
                        val cand = data.candidate
                        if (cand != null) {
                            try {
                                // cand is a plain object (from toJSON), passing to addIceCandidate should work
                                pc?.addIceCandidate(cand)
                            } catch (e: Throwable) {
                                console.log("addIceCandidate failed:", e)
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                console.log("signaling handler error:", e)
            }
        }
        if (bc != null) bc.onmessage = handler
        onDispose { try { bc?.close() } catch (_: Throwable) {} }
    }

    fun startLocalPreview() {
        js("console.log('startLocalPreview called')")
        try {
            val p = js("navigator.mediaDevices.getUserMedia({ video: true, audio: true })") as Promise<dynamic>
            p.then { s: dynamic ->
                val stream = s.unsafeCast<MediaStream>()
                localStream = stream
                val lv = ensureVideoElement(localVideoId, localContainerId)
                if (lv != null) {
                    lv.srcObject = stream
                    lv.muted = true
                    lv.playsInline = true
                    lv.play()
                    js("console.log('Local preview started')")
                }
            }.catch { e -> console.log("getUserMedia failed:", e) }
        } catch (e: Throwable) {
            console.log("startLocalPreview exception:", e)
        }
    }

    fun initiateCall() {
        js("console.log('initiateCall called')")
        createPeerConnection()
        if (!tracksAdded) {
            localStream?.let { s ->
                val tracks = (s.getTracks()).unsafeCast<Array<dynamic>>()
                for (track in tracks) {
                    try {
                        pc.addTrack(track, s)
                    } catch (e: dynamic) {
                        console.log("addTrack failed (initiateCall):", e)
                    }
                }
            }
            tracksAdded = true
        }

        pc.createOffer().then { offer: dynamic ->
            pc.setLocalDescription(offer).then {
                val msg = js("({})")
                msg.type = "offer"
                // send plain sdp and type so it's cloneable
                msg.sdp = offer.sdp
                msg.sdpType = offer.type
                msg.from = clientId
                bc?.postMessage(msg)
                js("console.log('offer posted')")
            }
        }
    }

    fun hangup() {
        js("console.log('hangup called')")
        closePeer()
        tracksAdded = false
        val lv = document.getElementById(localVideoId) as? HTMLVideoElement
        if (lv != null) lv.srcObject = null
        val rv = document.getElementById(remoteVideoId) as? HTMLVideoElement
        if (rv != null) rv.srcObject = null
    }

    // Load saved messages for this tab on first composition
    // LaunchedEffect(tabId) {
    //     try {
    //         val stored = sessionStorage.getItem("chatMessages_" + tabId)
    //         if (stored != null) {
    //             val arr = JSON.parse(stored) as Array<dynamic>
    //             messages = arr.map { if (it == null) "" else it.toString() }
    //         } else {
    //             messages = listOf("Welcome to the chat!")
    //         }
    //     } catch (e: dynamic) {
    //         console.log("Failed to load chat messages:", e)
    //         messages = listOf("Welcome to the chat!")
    //     }
    // }

    // Debug: log messages changes so we can see when a tab's messages update
    LaunchedEffect(messages) {
        try {
            println("messages changed (local): ${messages.size}")
        } catch (_: Throwable) {}
    }

    // Prevent responding to storage events from other tabs (ignore cross-tab writes)
    DisposableEffect(Unit) {
        val handler = { e: dynamic ->
            try {
                console.log("storage event ignored in Chat (key):", e?.key, "newValue:", e?.newValue)
            } catch (_: Throwable) {}
        }
        window.addEventListener("storage", handler)
        onDispose { window.removeEventListener("storage", handler) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.px)
            .backgroundColor(JsTheme.LightGray.rgb),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(22.px)
                .margin(bottom = 16.px),
            text = "Chat (WebRTC)"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.px)
                .borderRadius(r = 10.px)
                .padding(10.px),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Video panel
            Column(
                modifier = Modifier
                    .width(65.percent)
                    .fillMaxHeight()
                    .backgroundColor(Colors.White)
                    .borderRadius(r = 8.px)
                    .padding(12.px)
            ) {
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(16.px).margin(bottom = 8.px), text = "Video")

                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.width(48.percent).fillMaxHeight().backgroundColor(Colors.Black).borderRadius(r = 8.px).id(remoteContainerId)) {}
                    Box(modifier = Modifier.width(4.px))
                    Box(modifier = Modifier.width(48.percent).fillMaxHeight().backgroundColor(Colors.Black).borderRadius(r = 8.px).id(localContainerId)) {}
                }

                Row(modifier = Modifier.fillMaxWidth().margin(top = 12.px), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(attrs = { onClick { startLocalPreview() } }) { Text("Start Preview") }
                    Button(attrs = { onClick { initiateCall() } }) { Text("Start Call") }
                    Button(attrs = { onClick { hangup() } }) { Text("Hang Up") }
                }
            }

            // Chat panel
            Column(
                modifier = Modifier
                    .width(33.percent)
                    .fillMaxHeight()
                    .backgroundColor(Colors.White)
                    .borderRadius(r = 8.px)
                    .padding(12.px),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(16.px).margin(bottom = 8.px), text = "Chat")

                Box(modifier = Modifier.fillMaxWidth().height(380.px).styleModifier { property("overflow-y", "auto") }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        for (msg in messages) {
                            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.px)) {
                                if (msg.incoming) {
                                    // incoming: left-aligned light bubble
                                    Box(modifier = Modifier.margin(right = 40.px).backgroundColor(Colors.LightGray).padding(10.px).borderRadius(r = 8.px)) {
                                        SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).color(Colors.Black), text = msg.text)
                                    }
                                } else {
                                    // outgoing: right-aligned primary bubble
                                    Box(modifier = Modifier.styleModifier { property("display", "flex"); property("justify-content", "flex-end") }) {
                                        Box(modifier = Modifier.margin(left = 40.px).backgroundColor(JsTheme.Primary.rgb).padding(10.px).borderRadius(r = 8.px)) {
                                            SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).color(Colors.White), text = msg.text)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().height(48.px), verticalAlignment = Alignment.CenterVertically) {
                    Input(type = InputType.Text, attrs = { attr("id", inputId); attr("placeholder", "Type a message...") })
                    Box(modifier = Modifier.width(8.px))
                    Button(attrs = { onClick {
                        val el = document.getElementById(inputId) as? org.w3c.dom.HTMLInputElement
                        val v = el?.value ?: ""
                        if (v.isNotBlank()) {
                            // append local outgoing message
                            messages = messages + ChatMessage(v, incoming = false)
                            el?.value = ""
                            // if sharing enabled, broadcast to other tabs
                            if (chatShareEnabled && bcChat != null) {
                                try {
                                    val msg = js("({})")
                                    msg.type = "chat"
                                    msg.text = v
                                    msg.from = clientId
                                    msg.time = js("Date.now()")
                                    bcChat.postMessage(msg)
                                } catch (e: dynamic) {
                                    console.log("failed to post chat message:", e)
                                }
                            }
                        }
                    } }) { Text("Send") }
                }

                // Chat sharing toggle
                Row(modifier = Modifier.fillMaxWidth().margin(top = 8.px), verticalAlignment = Alignment.CenterVertically) {
                    Input(type = InputType.Checkbox, attrs = {
                        if (chatShareEnabled) attr("checked", "checked")
                        onClick { chatShareEnabled = !chatShareEnabled }
                    })
                    Box(modifier = Modifier.width(8.px))
                    SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(12.px).color(Colors.Gray), text = if (chatShareEnabled) "Chat sharing: ON — messages will be broadcast to other tabs" else "Chat sharing: OFF — messages remain local")
                }
            }
        }
    }
}
