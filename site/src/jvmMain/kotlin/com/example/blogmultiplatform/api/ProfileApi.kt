package com.example.blogmultiplatform.api

import com.example.blogmultiplatform.data.MongoDB
import com.example.blogmultiplatform.models.User
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.data.getValue
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Api(routeOverride = "saveprofile")
suspend fun saveProfile(context: ApiContext) {
    try {
        val userRequest = context.req.body?.decodeToString()?.let { Json.decodeFromString<User>(it) }
        if (userRequest == null) {
            context.res.setBodyText(Json.encodeToString("Invalid request body."))
            return
        }
        val result = context.data.getValue<MongoDB>().saveProfile(userRequest)
        context.res.setBodyText(Json.encodeToString(result))
    } catch (e: Exception) {
        context.res.setBodyText(Json.encodeToString(e.message))
    }
}
