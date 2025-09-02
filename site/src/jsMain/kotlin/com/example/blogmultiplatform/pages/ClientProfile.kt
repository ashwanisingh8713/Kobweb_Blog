package com.example.blogmultiplatform.pages

import androidx.compose.runtime.*
import com.example.blogmultiplatform.models.Profile
import com.example.blogmultiplatform.repository.ProfileRepository
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.example.shared.JsTheme
import com.varabyte.kobweb.compose.ui.graphics.Colors
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import com.varabyte.kobweb.core.Page
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.TextArea
import kotlinx.browser.window

@Page
@Composable
fun ClientProfileScreen() {
    val userId = localStorage.getItem("userId") ?: ""
    ClientProfileContent(userId = userId)
}

@Composable
fun ClientProfileContent(userId: String) {
    var profile by remember { mutableStateOf(ProfileRepository.getProfile(userId) ?: Profile(userId = userId)) }
    var name by remember { mutableStateOf(profile.name) }
    var email by remember { mutableStateOf(profile.email) }
    var bio by remember { mutableStateOf(profile.bio) }
    var error by remember { mutableStateOf("") }

    Div({ style { padding(32.px) } }) {
        H2 { Text("Client Profile") }
        Input(type = InputType.Text, attrs = {
            value(name)
            onInput { name = it.value }
            attr("placeholder", "Name")
            style {
                width(350.px)
                height(54.px)
                padding(0.px, 20.px) // top/bottom = 0, left/right = 20
                backgroundColor(Colors.White)
                fontFamily(FONT_FAMILY)
                fontSize(14.px)
                margin(0.px, 0.px, 12.px, 0.px) // bottom = 12
            }
        })
        Br()
        Input(type = InputType.Email, attrs = {
            value(email)
            onInput { email = it.value }
            attr("placeholder", "Email")
            style {
                width(350.px)
                height(54.px)
                padding(0.px, 20.px)
                backgroundColor(Colors.White)
                fontFamily(FONT_FAMILY)
                fontSize(14.px)
                margin(0.px, 0.px, 12.px, 0.px)
            }
        })
        Br()
        TextArea(attrs = {
            value(bio)
            onInput { bio = it.value }
            attr("placeholder", "Bio")
            style {
                width(350.px)
                height(80.px)
                padding(10.px, 20.px) // top/bottom = 10, left/right = 20
                backgroundColor(Colors.White)
                fontFamily(FONT_FAMILY)
                fontSize(14.px)
                margin(0.px, 0.px, 20.px, 0.px)
            }
        })
        Br()
        Button(
            attrs = {
                onClick {
                    if (name.isBlank() || email.isBlank() || bio.isBlank()) {
                        error = "Please fill all fields."
                    } else {
                        val updatedProfile = Profile(userId, name, email, bio)
                        ProfileRepository.saveProfile(updatedProfile)
                        error = ""
                        localStorage.setItem("userName", name)
                        localStorage.setItem("isLoggedIn", "true")
                        localStorage.setItem("profileComplete", "true")
                        window.location.href = "/admin" // Navigate to Admin Panel
                    }
                }
                style {
                    width(350.px)
                    height(54.px)
                    backgroundColor(JsTheme.Primary.rgb)
                    color(Colors.White)
                    borderRadius(4.px)
                    fontFamily(FONT_FAMILY)
                    fontWeight("500")
                    fontSize(14.px)
                    cursor("pointer")
                    margin(0.px, 0.px, 24.px, 0.px)
                }
            }
        ) {
            Text("Save Profile")
        }
        //after CLICKING THE save profile BUTTON it must stay on the admin page and show the error message if fields are empty
        if (error.isNotBlank()) {
            P({ style { color(Color.red) } }) { Text(error) }
        }
    }
}
