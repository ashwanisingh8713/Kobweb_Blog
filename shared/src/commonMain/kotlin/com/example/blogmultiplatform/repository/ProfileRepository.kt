package com.example.blogmultiplatform.repository

import com.example.blogmultiplatform.models.Profile

object ProfileRepository {
    private val profiles = mutableMapOf<String, Profile>()

    fun getProfile(userId: String): Profile? = profiles[userId]

    fun saveProfile(profile: Profile) {
        profiles[profile.userId] = profile
    }

    fun isProfileComplete(userId: String): Boolean =
        profiles[userId]?.isComplete() ?: false
}
