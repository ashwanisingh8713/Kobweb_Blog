package com.example.blogmultiplatform.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.style.KobwebComposeStyleSheet.attr
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun DeveloperProfileScreen() {
    val context = rememberPageContext()
    val userId = localStorage.getItem("userId") ?: ""
    DeveloperProfileContent(userId = userId) {
        context.router.navigateTo(com.example.blogmultiplatform.navigation.Screen.HomePage.route)
    }
}

@Composable
private fun DeveloperProfileContent(userId: String, onProfileCompleted: () -> Unit) {
    var profile by remember { mutableStateOf(com.example.blogmultiplatform.repository.ProfileRepository.getProfile(userId) ?: com.example.blogmultiplatform.models.Profile(userId = userId)) }
    var name by remember { mutableStateOf(profile.name) }
    var email by remember { mutableStateOf(profile.email) }
    var bio by remember { mutableStateOf(profile.bio) }
    var error by remember { mutableStateOf("") }

    Div({ style { padding(32.px) } }) {
        H2 { Text("Developer Profile") }
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
                    val updatedProfile = com.example.blogmultiplatform.models.Profile(userId, name, email, bio)
                    com.example.blogmultiplatform.repository.ProfileRepository.saveProfile(updatedProfile)
                    error = ""
                    onProfileCompleted()
                }
            }
        }) { Text("Save Profile") }
        if (error.isNotBlank()) {
            P({ style { color(Color.red) } }) { Text(error) }
        }
    }
}
