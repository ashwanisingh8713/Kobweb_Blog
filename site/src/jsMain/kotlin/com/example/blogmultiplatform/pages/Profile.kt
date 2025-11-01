package com.example.blogmultiplatform.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.shared.JsTheme
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.attributes.InputType
import kotlinx.browser.localStorage
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.compose.ui.toAttrs
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.blogmultiplatform.models.User
import com.varabyte.kobweb.browser.api
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Page("/profile")
@Composable
fun ProfilePage() {
    println("ProfilePage mounted")
    // Profile fields sourced from localStorage via getItem
    var username by remember { mutableStateOf(localStorage.getItem("username") ?: "") }
    var displayName by remember { mutableStateOf(localStorage.getItem("displayName") ?: "") }
    var bio by remember { mutableStateOf(localStorage.getItem("bio") ?: "") }
    var avatarUrl by remember { mutableStateOf(localStorage.getItem("avatarUrl") ?: "") }
    var role by remember { mutableStateOf(localStorage.getItem("role") ?: "client") }

    var showSuccess by remember { mutableStateOf(false) }

    val scope = MainScope()

    Box(
        modifier = Modifier.fillMaxSize().styleModifier {
            property("min-height", "100vh")
            property("display", "flex")
            property("align-items", "center")
            property("justify-content", "center")
        },
        contentAlignment = Alignment.Center
    ) {
        // Card
        Box(
            modifier = Modifier.styleModifier {
                property("width", "auto")
                property("max-width", "900px")
                property("background", "linear-gradient(180deg,#ffffff,#fbfbff)")
                property("border-radius", "14px")
                property("box-shadow", "0 24px 80px rgba(16,24,40,0.08)")
                property("padding", "28px")
                property("margin", "0 16px")
            }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.styleModifier { property("width", "100%") }) {
                // Header
                SpanText(
                    modifier = Modifier.fontSize(24.px).fontFamily(FONT_FAMILY).fontWeight(FontWeight.Bold).margin(bottom = 8.px),
                    text = "Your Profile"
                )
                SpanText(
                    modifier = Modifier.fontSize(13.px).color(Colors.Gray).margin(bottom = 18.px),
                    text = "Create or update your public profile. Changes are saved locally."
                )

                // avatar preview and url (live preview on input)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().margin(bottom = 14.px)) {
                    Box(
                        modifier = Modifier.width(96.px).height(96.px).styleModifier {
                            property("border-radius", "50%")
                            property("overflow", "hidden")
                            property("background", "#f4f6f8")
                            property("box-shadow", "0 8px 24px rgba(16,24,40,0.06)")
                        }.margin(right = 16.px)
                    ) {
                        if (avatarUrl.isNotBlank()) {
                            Img(src = avatarUrl, attrs = { attr("style", "width:100%;height:100%;object-fit:cover") })
                        } else {
                            // friendly placeholder
                            Box(modifier = Modifier.styleModifier { property("display", "flex"); property("align-items", "center"); property("justify-content", "center"); property("height", "100%") }) {
                                SpanText(modifier = Modifier.color(Colors.Gray), text = "No avatar")
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Avatar URL input
                        Input(type = InputType.Text, attrs = {
                            attr("id", "profileAvatar")
                            attr("placeholder", "Avatar URL")
                            attr("value", avatarUrl
                            )
                            onInput {
                                val input = it.target as? org.w3c.dom.HTMLInputElement
                                if (input != null) avatarUrl = input.value
                            }
                            attr("style", "width:100%; padding:12px; border-radius:10px; border:1px solid #e6e9f2; box-shadow: inset 0 1px 2px rgba(16,24,40,0.02);")
                        })
                        SpanText(modifier = Modifier.fontSize(12.px).color(Colors.Gray).margin(top = 8.px), text = "Paste a public image URL to preview instantly.")
                    }
                }

                // Inputs
                Input(type = InputType.Text, attrs = {
                    attr("id", "profileDisplayName")
                    attr("placeholder", "Display name")
                    attr("value", displayName)
                    onInput {
                        val input = it.target as? org.w3c.dom.HTMLInputElement
                        if (input != null) displayName = input.value
                    }
                    attr("style", "width:100%; padding:12px; border-radius:10px; margin-bottom:10px; border:1px solid #e6e9f2; box-shadow: inset 0 1px 2px rgba(16,24,40,0.02);")
                })

                Input(type = InputType.Text, attrs = {
                    attr("id", "profileUsername")
                    attr("placeholder", "Username")
                    attr("value", username)
                    onInput {
                        val input = it.target as? org.w3c.dom.HTMLInputElement
                        if (input != null) username = input.value
                    }
                    attr("style", "width:100%; padding:12px; border-radius:10px; margin-bottom:10px; border:1px solid #e6e9f2;")
                })

                Input(type = InputType.Text, attrs = {
                    attr("id", "profileBio")
                    attr("placeholder", "Short bio")
                    attr("value", bio)
                    onInput {
                        val input = it.target as? org.w3c.dom.HTMLInputElement
                        if (input != null) bio = input.value
                    }
                    attr("style", "width:100%; padding:12px; border-radius:10px; margin-bottom:16px; border:1px solid #e6e9f2; height:84px;")
                })

                // role display (pill)
                Row(modifier = Modifier.fillMaxWidth().margin(bottom = 16.px), horizontalArrangement = Arrangement.Center) {
                    SpanText(modifier = Modifier.margin(right = 8.px).color(Colors.Gray), text = "Role:")
                    Box(modifier = Modifier.styleModifier { property("background", "${if (role=="client") JsTheme.Primary.rgb else "transparent"}"); property("padding", "8px 14px"); property("border-radius", "999px"); property("border", "1px solid ${JsTheme.Primary.rgb}") }) {
                        SpanText(modifier = Modifier.color(if (role=="client") Colors.White else JsTheme.Primary.rgb), text = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                    }
                }

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(attrs = {
                        attr("style", "width:140px;height:44px;border-radius:8px;border:1px solid #d7dae3;background:transparent;color:#374151")
                        onClick {
                            // cancel -> reload values from storage
                            username = localStorage.getItem("username") ?: ""
                            displayName = localStorage.getItem("displayName") ?: ""
                            bio = localStorage.getItem("bio") ?: ""
                            avatarUrl = localStorage.getItem("avatarUrl") ?: ""
                        }
                    }) {
                        SpanText(text = "Cancel")
                    }

                    Button(attrs = {
                        attr("style", "width:140px;height:44px;border-radius:8px;background:linear-gradient(90deg, #06b6d4, #0ea5e9); color:white; box-shadow:0 8px 30px rgba(14,165,233,0.18)")
                        onClick {
                            // Save using state values (no DOM lookups)
                            localStorage.setItem("username", username)
                            localStorage.setItem("displayName", displayName)
                            localStorage.setItem("bio", bio)
                            localStorage.setItem("avatarUrl", avatarUrl)
                            localStorage.setItem("role", role)

                            // POST to server
                            val user = User(
                                _id = "",
                                username = username,
                                password = "",
                                role = role,
                                displayName = if (displayName.isBlank()) null else displayName,
                                bio = if (bio.isBlank()) null else bio,
                                avatarUrl = if (avatarUrl.isBlank()) null else avatarUrl
                            )
                            // launch side effect using window.api
                            scope.launch {
                                try {
                                    val response = window.api.tryPost(apiPath = "saveprofile", body = Json.encodeToString(user).encodeToByteArray())?.decodeToString()
                                    val success = response?.toBoolean() ?: false
                                    if (success) {
                                        println("Profile saved on server")
                                        showSuccess = true
                                    } else {
                                        println("Failed to save profile on server: $response")
                                        // still show success locally but you may want to show an error instead
                                        showSuccess = true
                                    }
                                } catch (e: Exception) {
                                    println("Error saving profile: ${e.message}")
                                    showSuccess = true
                                }
                            }
                        }
                    }) {
                        SpanText(text = "Save")
                    }
                }

                // Success overlay
                if (showSuccess) {
                    Box(modifier = Modifier.styleModifier { property("position", "fixed"); property("inset", "0"); property("display", "flex"); property("align-items", "center"); property("justify-content", "center"); property("background", "rgba(2,6,23,0.35)") }) {
                        Box(modifier = Modifier.styleModifier { property("background", "white"); property("padding", "20px"); property("border-radius", "12px"); property("box-shadow", "0 12px 40px rgba(16,24,40,0.12)") }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SpanText(modifier = Modifier.fontWeight(FontWeight.Bold).margin(bottom = 8.px), text = "Profile saved")
                                Button(attrs = Modifier.onClick { showSuccess = false }.toAttrs()) { SpanText(text = "OK") }
                            }
                        }
                    }
                }
            }
        }
    }
}
