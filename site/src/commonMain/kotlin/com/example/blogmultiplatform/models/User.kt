package com.example.blogmultiplatform.models

expect class User {
    val _id: String
    val username: String
    val password: String
    val role: String

    // Optional profile fields
    val displayName: String?
    val bio: String?
    val avatarUrl: String?
}

expect class UserWithoutPassword {
    val _id: String
    val username: String
    val role: String

    val displayName: String?
    val avatarUrl: String?
}