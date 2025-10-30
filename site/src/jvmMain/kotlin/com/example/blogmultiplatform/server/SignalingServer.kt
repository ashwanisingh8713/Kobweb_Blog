package com.example.blogmultiplatform.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class SignalMessage(val type: String, val from: String? = null, val to: String? = null, val payload: String? = null)

fun main() {
    val rooms = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()
    val json = Json { ignoreUnknownKeys = true }

    embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            timeout = Duration.ofSeconds(30)
        }

        routing {
            webSocket("/signaling/{room}") {
                val roomId = call.parameters["room"] ?: "default"
                val set = rooms.computeIfAbsent(roomId) { ConcurrentHashMap.newKeySet<DefaultWebSocketServerSession>() }
                set.add(this)
                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            set.forEach { session ->
                                if (session != this) {
                                    // send as Frame.Text to avoid argument type mismatch
                                    session.send(Frame.Text(text))
                                }
                            }
                        }
                    }
                } finally {
                    set.remove(this)
                    if (set.isEmpty()) rooms.remove(roomId)
                }
            }
        }
    }.start(wait = true)
}
