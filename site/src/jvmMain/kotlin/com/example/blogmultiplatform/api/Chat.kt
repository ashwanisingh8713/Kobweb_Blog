package com.example.blogmultiplatform.api

import com.example.blogmultiplatform.data.MongoDB
import com.example.blogmultiplatform.models.*
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.data.getValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.codecs.ObjectIdGenerator

@Api(routeOverride = "sendmessage")
suspend fun sendMessage(context: ApiContext) {
    try {
        val message = context.req.getBody<ChatMessage>()
        val newMessage = message?.copy(
            id = ObjectIdGenerator().generate().toString(),
            timestamp = System.currentTimeMillis()
        )

        val result = newMessage?.let {
            context.data.getValue<MongoDB>().sendMessage(it)
        }

        context.res.setBody(
            ChatApiResponse(
                success = result == true,
                message = if (result == true) "Message sent successfully" else "Failed to send message",
                data = if (result == true) newMessage else null
            )
        )
    } catch (e: Exception) {
        context.res.setBody(
            ChatApiResponse(
                success = false,
                message = e.message ?: "Unknown error occurred"
            )
        )
    }
}

@Api(routeOverride = "getmessages")
suspend fun getMessages(context: ApiContext) {
    try {
        val senderId = context.req.params["senderId"] ?: ""
        val receiverId = context.req.params["receiverId"] ?: ""
        val skip = context.req.params["skip"]?.toInt() ?: 0
        val limit = context.req.params["limit"]?.toInt() ?: 50

        if (senderId.isEmpty() || receiverId.isEmpty()) {
            context.res.setBody(
                ChatListResponse(
                    success = false,
                    message = "Both senderId and receiverId are required"
                )
            )
            return
        }

        val messages = context.data.getValue<MongoDB>().getMessages(
            senderId = senderId,
            receiverId = receiverId,
            skip = skip,
            limit = limit
        )

        context.res.setBody(
            ChatListResponse(
                success = true,
                data = messages
            )
        )
    } catch (e: Exception) {
        context.res.setBody(
            ChatListResponse(
                success = false,
                message = e.message ?: "Failed to retrieve messages"
            )
        )
    }
}

@Api(routeOverride = "getchatrooms")
suspend fun getChatRooms(context: ApiContext) {
    try {
        val userId = context.req.params["userId"] ?: ""

        if (userId.isEmpty()) {
            context.res.setBody(
                ChatListResponse(
                    success = false,
                    message = "userId is required"
                )
            )
            return
        }

        val chatRooms = context.data.getValue<MongoDB>().getChatRooms(userId)
        context.res.setBody(ChatListResponse(success = true, data = emptyList())) // Simplified for now
    } catch (e: Exception) {
        context.res.setBody(
            ChatListResponse(
                success = false,
                message = e.message ?: "Failed to retrieve chat rooms"
            )
        )
    }
}

@Api(routeOverride = "createchatroom")
suspend fun createChatRoom(context: ApiContext) {
    try {
        val chatRoom = context.req.getBody<ChatRoom>()
        val newChatRoom = chatRoom?.copy(
            id = ObjectIdGenerator().generate().toString(),
            createdAt = System.currentTimeMillis()
        )

        val result = newChatRoom?.let {
            context.data.getValue<MongoDB>().createChatRoom(it)
        }

        context.res.setBody(
            ChatApiResponse(
                success = result == true,
                message = if (result == true) "Chat room created successfully" else "Failed to create chat room"
            )
        )
    } catch (e: Exception) {
        context.res.setBody(
            ChatApiResponse(
                success = false,
                message = e.message ?: "Failed to create chat room"
            )
        )
    }
}
