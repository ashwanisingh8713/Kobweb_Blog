package com.example.blogmultiplatform.pages

import androidx.compose.runtime.*
import com.example.blogmultiplatform.components.VideoCallComponent
import com.example.blogmultiplatform.models.User
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*

@Page("/chat")
@Composable
fun ChatPage() {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var availableUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isVideoCallVisible by remember { mutableStateOf(false) }
    var incomingCall by remember { mutableStateOf(false) }
    var callerName by remember { mutableStateOf("") }

    // Initialize current user from localStorage
    LaunchedEffect(Unit) {
        val userId = localStorage.getItem("userId")
        val username = localStorage.getItem("username")

        if (userId != null && username != null) {
            currentUser = User(
                _id = userId,
                username = username,
                password = "", // Not needed for chat
                role = "USER"
            )
            loadAvailableUsers { users ->
                availableUsers = users.filter { it._id != userId }
            }
        } else {
            // Redirect to login if not authenticated
            window.location.href = "/admin"
        }
    }

    currentUser?.let { user ->
        Row(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .width(100.percent)
                .minHeight(100.vh)
                .backgroundColor(rgb(243, 244, 246))
                .padding(20.px),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // Sidebar with user list
            Column(
                modifier = Modifier
                    .width(300.px)
                    .height(600.px)
                    .backgroundColor(Colors.White)
                    .borderRadius(8.px)
                    .margin(right = 20.px)
                    .padding(16.px)
            ) {
                SpanText(
                    text = "Contacts",
                    modifier = Modifier
                        .fontSize(20.px)
                        .fontWeight(FontWeight.Bold)
                )

                Div(attrs = Modifier.height(16.px).toAttrs()) // Spacer

                availableUsers.forEach { contactUser ->
                    UserListItem(
                        user = contactUser,
                        isSelected = selectedUser?._id == contactUser._id,
                        onClick = { selectedUser = contactUser }
                    )
                }

                if (availableUsers.isEmpty()) {
                    SpanText(
                        text = "No contacts available",
                        modifier = Modifier
                            .color(rgb(107, 114, 128))
                            .fontSize(14.px)
                            .textAlign(TextAlign.Center)
                            .width(100.percent)
                            .padding(20.px)
                    )
                }
            }

            // Main chat area
            Box(
                modifier = Modifier
                    .flexGrow(1)
                    .maxWidth(800.px)
            ) {
                selectedUser?.let { chatPartner ->
                    SimpleChatArea(
                        currentUserId = user._id,
                        chatWithUserId = chatPartner._id,
                        chatWithUserName = chatPartner.username,
                        onSendMessage = { message ->
                            sendMessage(user._id, chatPartner._id, message)
                        },
                        onVideoCall = {
                            isVideoCallVisible = true
                            callerName = chatPartner.username
                        }
                    )
                } ?: run {
                    // No user selected placeholder
                    Column(
                        modifier = Modifier
                            .width(100.percent)
                            .height(600.px)
                            .backgroundColor(Colors.White)
                            .borderRadius(8.px),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        SpanText(
                            text = "💬",
                            modifier = Modifier.fontSize(48.px)
                        )

                        Div(attrs = Modifier.height(16.px).toAttrs()) // Spacer

                        SpanText(
                            text = "Select a contact to start chatting",
                            modifier = Modifier
                                .fontSize(18.px)
                                .color(rgb(107, 114, 128))
                        )
                    }
                }
            }
        }

        // Video call overlay
        VideoCallComponent(
            isVisible = isVideoCallVisible,
            callerName = callerName,
            isIncoming = incomingCall,
            onAccept = {
                incomingCall = false
                console.log("Video call accepted")
            },
            onDecline = {
                isVideoCallVisible = false
                incomingCall = false
                console.log("Video call declined")
            },
            onEndCall = {
                isVideoCallVisible = false
                console.log("Video call ended")
            }
        )
    }
}

@Composable
private fun SimpleChatArea(
    currentUserId: String,
    chatWithUserId: String,
    chatWithUserName: String,
    onSendMessage: (String) -> Unit,
    onVideoCall: () -> Unit
) {
    var newMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .width(100.percent)
            .height(600.px)
            .backgroundColor(Colors.White)
            .borderRadius(8.px)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .width(100.percent)
                .backgroundColor(rgb(59, 130, 246))
                .color(Colors.White)
                .borderRadius(topLeft = 8.px, topRight = 8.px)
                .padding(16.px),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpanText(
                text = chatWithUserName,
                modifier = Modifier.fontSize(18.px).fontWeight(FontWeight.SemiBold)
            )

            Button(
                attrs = Modifier
                    .backgroundColor(rgba(255, 255, 255, 0.2))
                    .color(Colors.White)
                    .borderRadius(4.px)
                    .padding(8.px, 12.px)
                    .onClick { onVideoCall() }
                    .toAttrs()
            ) {
                Text("📹 Video Call")
            }
        }

        // Messages Area
        Div(
            attrs = Modifier
                .flexGrow(1)
                .backgroundColor(rgb(249, 250, 251))
                .padding(16.px)
                .toAttrs()
        ) {
            P {
                Text("Messages will appear here...")
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .width(100.percent)
                .backgroundColor(Colors.White)
                .borderRadius(bottomLeft = 8.px, bottomRight = 8.px)
                .padding(16.px),
            horizontalArrangement = Arrangement.spacedBy(8.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Input(
                type = InputType.Text,
                attrs = Modifier
                    .flexGrow(1)
                    .borderRadius(20.px)
                    .padding(12.px)
                    .toAttrs {
                        placeholder("Type a message...")
                        value(newMessage)
                        onInput { event ->
                            newMessage = event.value
                        }
                    }
            )

            Button(
                attrs = Modifier
                    .backgroundColor(rgb(59, 130, 246))
                    .color(Colors.White)
                    .borderRadius(20.px)
                    .cursor(Cursor.Pointer)
                    .padding(12.px, 20.px)
                    .onClick {
                        if (newMessage.trim().isNotEmpty()) {
                            onSendMessage(newMessage.trim())
                            newMessage = ""
                        }
                    }
                    .toAttrs()
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: User,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Div(
        attrs = Modifier
            .width(100.percent)
            .padding(12.px)
            .borderRadius(8.px)
            .cursor(Cursor.Pointer)
            .backgroundColor(if (isSelected) rgb(59, 130, 246) else Colors.Transparent)
            .color(if (isSelected) Colors.White else Colors.Black)
            .onClick { onClick() }
            .toAttrs()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.px)
        ) {
            // Avatar placeholder
            Div(
                attrs = Modifier
                    .width(40.px)
                    .height(40.px)
                    .backgroundColor(if (isSelected) rgba(255, 255, 255, 0.2) else rgb(243, 244, 246))
                    .borderRadius(50.percent)
                    .display(DisplayStyle.Flex)
                    .fontSize(16.px)
                    .toAttrs()
            ) {
                SpanText(user.username.first().uppercase())
            }

            Column {
                SpanText(
                    text = user.username,
                    modifier = Modifier
                        .fontSize(14.px)
                        .fontWeight(FontWeight.SemiBold)
                )

                Div(attrs = Modifier.height(4.px).toAttrs()) // Small spacer

                SpanText(
                    text = "Online", // You could make this dynamic
                    modifier = Modifier
                        .fontSize(12.px)
                        .color(if (isSelected) rgba(255, 255, 255, 0.8) else rgb(107, 114, 128))
                )
            }
        }
    }

    Div(attrs = Modifier.height(8.px).toAttrs()) // Bottom margin
}

// Helper functions for API calls
private fun loadAvailableUsers(onLoaded: (List<User>) -> Unit) {
    try {
        // This would typically make an API call to fetch all users
        // For now, we'll use a placeholder with some dummy users
        val dummyUsers = listOf(
            User(_id = "user1", username = "john_doe", password = "", role = "USER"),
            User(_id = "user2", username = "jane_smith", password = "", role = "USER"),
            User(_id = "user3", username = "bob_wilson", password = "", role = "USER")
        )
        onLoaded(dummyUsers)
    } catch (e: Exception) {
        console.log("Error loading users: ${e.message}")
        onLoaded(emptyList())
    }
}

private fun sendMessage(senderId: String, receiverId: String, message: String) {
    try {
        val requestBody = """{"senderId":"$senderId","receiverId":"$receiverId","message":"$message"}"""

        // Use dynamic object creation instead of js() template
        val fetchOptions = js("({})")
        fetchOptions.method = "POST"
        fetchOptions.headers = js("({'Content-Type': 'application/json'})")
        fetchOptions.body = requestBody

        window.fetch("/api/sendmessage", fetchOptions)
    } catch (e: Exception) {
        console.log("Error sending message: ${e.message}")
    }
}
