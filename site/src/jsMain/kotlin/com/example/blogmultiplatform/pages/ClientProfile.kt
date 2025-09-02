package com.example.blogmultiplatform.pages

import androidx.compose.runtime.*
import com.example.blogmultiplatform.models.Profile
import com.example.blogmultiplatform.repository.ProfileRepository
import com.varabyte.kobweb.compose.style.KobwebComposeStyleSheet.attr
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.TextArea

@Page
@Composable
fun ClientProfileScreen() {
    val context = rememberPageContext()
    val userId = localStorage.getItem("userId") ?: ""
    ClientProfileContent(userId = userId) {
        context.router.navigateTo(com.example.blogmultiplatform.navigation.Screen.AdminHome.route)
    }
}

@Composable
fun ClientProfileContent(userId: String, onProfileCompleted: () -> Unit) {
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
        })
        Br()
        Input(type = InputType.Email, attrs = {
            value(email)
            onInput { email = it.value }
            attr("placeholder", "Email")
        })
        Br()
        TextArea(attrs = {
            value(bio)
            onInput { bio = it.value }
            attr("placeholder", "Bio")
        })
        Br()
        Button(attrs = {
            onClick {
                if (name.isBlank() || email.isBlank() || bio.isBlank()) {
                    error = "Please fill all fields."
                } else {
                    val updatedProfile = Profile(userId, name, email, bio)
                    ProfileRepository.saveProfile(updatedProfile)
                    error = ""
                    kotlinx.browser.localStorage.setItem("userName", name)
                    kotlinx.browser.localStorage.setItem("isLoggedIn", "true")
                    kotlinx.browser.localStorage.setItem("profileComplete", "true")
                    onProfileCompleted()
                }
            }
        }) { Text("Save Profile") }
        if (error.isNotBlank()) {
            P({ style { color(Color.red) } }) { Text(error) }
        }
    }
}
