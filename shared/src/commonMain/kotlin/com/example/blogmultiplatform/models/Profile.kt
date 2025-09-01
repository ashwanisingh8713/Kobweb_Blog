package com.example.blogmultiplatform.models

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = ""
) {
    fun isComplete(): Boolean = name.isNotBlank() && email.isNotBlank() && bio.isNotBlank()
}

