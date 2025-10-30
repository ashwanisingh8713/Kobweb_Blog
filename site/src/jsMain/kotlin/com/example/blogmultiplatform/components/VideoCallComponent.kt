package com.example.blogmultiplatform.components

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun VideoCallComponent(
    isVisible: Boolean,
    callerName: String = "",
    isIncoming: Boolean = false,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onEndCall: () -> Unit = {}
) {
    var isCallActive by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible && !isIncoming) {
            initializeCamera()
        }
    }

    if (isVisible) {
        Div(
            attrs = Modifier
                .position(Position.Fixed)
                .top(0.px)
                .left(0.px)
                .width(100.percent)
                .height(100.percent)
                .backgroundColor(rgba(0, 0, 0, 0.9))
                .zIndex(1000)
                .display(DisplayStyle.Flex)
                .flexDirection(FlexDirection.Column)
                .toAttrs()
        ) {
            if (isIncoming && !isCallActive) {
                // Incoming call UI
                IncomingCallUI(
                    callerName = callerName,
                    onAccept = {
                        isCallActive = true
                        onAccept()
                        initializeCamera()
                    },
                    onDecline = onDecline
                )
            } else {
                // Active call UI
                Column(
                    modifier = Modifier
                        .width(80.percent)
                        .maxWidth(800.px)
                        .height(60.percent)
                        .backgroundColor(Colors.Black)
                        .borderRadius(8.px),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Video area placeholder
                    Div(
                        attrs = Modifier
                            .width(100.percent)
                            .height(400.px)
                            .backgroundColor(rgb(30, 30, 30))
                            .display(DisplayStyle.Flex)
                            .toAttrs()
                    ) {
                        Video(
                            attrs = Modifier
                                .width(100.percent)
                                .height(100.percent)
                                .toAttrs {
                                    id("localVideo")
                                    attr("autoplay", "")
                                    attr("muted", "")
                                }
                        )
                    }

                    // Controls
                    Row(
                        modifier = Modifier.padding(16.px),
                        horizontalArrangement = Arrangement.spacedBy(16.px)
                    ) {
                        Button(
                            attrs = Modifier
                                .backgroundColor(rgba(255, 255, 255, 0.2))
                                .color(Colors.White)
                                .borderRadius(50.percent)
                                .width(50.px)
                                .height(50.px)
                                .border(0.px)
                                .cursor(Cursor.Pointer)
                                .onClick { toggleMute() }
                                .toAttrs()
                        ) {
                            Text("🎤")
                        }

                        Button(
                            attrs = Modifier
                                .backgroundColor(rgba(255, 255, 255, 0.2))
                                .color(Colors.White)
                                .borderRadius(50.percent)
                                .width(50.px)
                                .height(50.px)
                                .border(0.px)
                                .cursor(Cursor.Pointer)
                                .onClick { toggleVideo() }
                                .toAttrs()
                        ) {
                            Text("📹")
                        }

                        Button(
                            attrs = Modifier
                                .backgroundColor(Colors.Red)
                                .color(Colors.White)
                                .borderRadius(50.percent)
                                .width(50.px)
                                .height(50.px)
                                .border(0.px)
                                .cursor(Cursor.Pointer)
                                .onClick {
                                    endCall()
                                    onEndCall()
                                }
                                .toAttrs()
                        ) {
                            Text("📞")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingCallUI(
    callerName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.px),
        modifier = Modifier
            .backgroundColor(Colors.White)
            .padding(32.px)
            .borderRadius(16.px)
            .textAlign(TextAlign.Center)
    ) {
        SpanText(
            text = "Incoming Video Call",
            modifier = Modifier
                .fontSize(24.px)
                .fontWeight(FontWeight.Bold)
        )

        SpanText(
            text = callerName,
            modifier = Modifier
                .fontSize(18.px)
                .color(rgb(107, 114, 128))
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.px)
        ) {
            Button(
                attrs = Modifier
                    .backgroundColor(Colors.Red)
                    .color(Colors.White)
                    .borderRadius(50.percent)
                    .width(60.px)
                    .height(60.px)
                    .border(0.px)
                    .cursor(Cursor.Pointer)
                    .onClick { onDecline() }
                    .toAttrs()
            ) {
                Text("❌")
            }

            Button(
                attrs = Modifier
                    .backgroundColor(Colors.Green)
                    .color(Colors.White)
                    .borderRadius(50.percent)
                    .width(60.px)
                    .height(60.px)
                    .border(0.px)
                    .cursor(Cursor.Pointer)
                    .onClick { onAccept() }
                    .toAttrs()
            ) {
                Text("✅")
            }
        }
    }
}

// JavaScript integration functions for WebRTC
private fun initializeCamera() {
    console.log("Initialize camera called")
    // Camera initialization will be handled by the browser when accessing the video element
}

private fun toggleMute() {
    console.log("Toggle mute called")
    // Mute functionality placeholder
}

private fun toggleVideo() {
    console.log("Toggle video called")
    // Video toggle functionality placeholder
}

private fun endCall() {
    console.log("End call called")
    // End call functionality placeholder
}


