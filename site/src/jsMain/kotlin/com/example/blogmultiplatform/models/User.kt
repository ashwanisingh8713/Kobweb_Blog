package com.example.blogmultiplatform.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
actual data class User(
    @SerialName(value = "_id")
    actual val _id: String = "",
    actual val username: String = "",
    actual val password: String = "",
    actual val role: String = "client",

    // profile fields
    actual val displayName: String? = null,
    actual val bio: String? = null,
    actual val avatarUrl: String? = null
)

@Serializable
actual data class UserWithoutPassword(
    @SerialName(value = "_id")
    actual val _id: String = "",
    actual val username: String = "",
    actual val role: String = "client",

    actual val displayName: String? = null,
    actual val avatarUrl: String? = null
)