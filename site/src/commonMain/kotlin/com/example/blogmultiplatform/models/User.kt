package com.example.blogmultiplatform.models

expect class User {
    val _id: String
    val username: String
    val password: String
    val role: String
}

expect class UserWithoutPassword {
    val _id: String
    val username: String
    val role: String
}