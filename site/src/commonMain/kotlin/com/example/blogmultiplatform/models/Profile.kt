package com.example.blogmultiplatform.models

import kotlinx.serialization.Serializable

// Shared profile model used for storing public profile info in MongoDB
@Serializable
data class Profile(
    val _id: String = "",
    val username: String = "",
    val displayName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val role: String = "client",
    val ts: Long = 0L
)
