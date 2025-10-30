package com.example.blogmultiplatform.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String = "",
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long = 0L,
    val messageType: MessageType = MessageType.TEXT
)

@Serializable
data class ChatRoom(
    val id: String = "",
    val participants: List<String>,
    val lastMessage: ChatMessage? = null,
    val createdAt: Long = 0L
)

@Serializable
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO_CALL_REQUEST,
    VIDEO_CALL_ACCEPTED,
    VIDEO_CALL_DECLINED,
    VIDEO_CALL_ENDED
}

@Serializable
data class VideoCallSignal(
    val type: SignalType,
    val senderId: String,
    val receiverId: String,
    val data: String? = null
)

@Serializable
enum class SignalType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    CALL_START,
    CALL_END
}

@Serializable
data class ChatApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ChatMessage? = null
)

@Serializable
data class ChatListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<ChatMessage>? = null
)
