package com.example.blogmultiplatform.models

import kotlinx.serialization.Serializable
import org.bson.codecs.ObjectIdGenerator

@Serializable
actual data class User(
    actual val _id: String = ObjectIdGenerator().generate().toString(),
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
    actual val _id: String = ObjectIdGenerator().generate().toString(),
    actual val username: String = "",
    actual val role: String = "client",

    actual val displayName: String? = null,
    actual val avatarUrl: String? = null
)