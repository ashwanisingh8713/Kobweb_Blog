package com.example.blogmultiplatform.pages.developer

import androidx.compose.runtime.*
import com.example.blogmultiplatform.models.Profile
import com.example.blogmultiplatform.repository.ProfileRepository
import com.example.shared.JsTheme
import com.example.blogmultiplatform.styles.LoginInputStyle
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.InputType
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.px
import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import com.example.blogmultiplatform.pages.ClientProfileContent

@Page("/developer/profile")
@Composable
fun DeveloperProfileScreen() {
    val context = rememberPageContext()
    val userId = localStorage.getItem("userId") ?: ""
    var profile by remember { mutableStateOf(ProfileRepository.getProfile(userId) ?: Profile(userId = userId)) }
    var name by remember { mutableStateOf(profile.name) }
    var email by remember { mutableStateOf(profile.email) }
    var bio by remember { mutableStateOf(profile.bio) }
    var error by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(leftRight = 50.px, top = 80.px, bottom = 24.px)
                .backgroundColor(JsTheme.LightGray.rgb),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpanText(
                modifier = Modifier
                    .margin(bottom = 50.px)
                    .fontSize(28.px)
                    .fontWeight(FontWeight.Bold)
                    .color(JsTheme.Primary.rgb)
                    .fontFamily(FONT_FAMILY),
                text = "Complete Developer Profile"
            )
            Input(
                type = InputType.Text,
                attrs = LoginInputStyle.toModifier()
                    .margin(bottom = 12.px)
                    .width(350.px)
                    .height(54.px)
                    .padding(leftRight = 20.px)
                    .backgroundColor(Colors.White)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .outline(width = 0.px, style = LineStyle.None, color = Colors.Transparent)
                    .toAttrs {
                        value(name)
                        onInput { name = it.value }
                        attr("placeholder", "Name")
                    }
            )
            Input(
                type = InputType.Email,
                attrs = LoginInputStyle.toModifier()
                    .margin(bottom = 12.px)
                    .width(350.px)
                    .height(54.px)
                    .padding(leftRight = 20.px)
                    .backgroundColor(Colors.White)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .outline(width = 0.px, style = LineStyle.None, color = Colors.Transparent)
                    .toAttrs {
                        value(email)
                        onInput { email = it.value }
                        attr("placeholder", "Email")
                    }
            )
            TextArea(
                attrs = LoginInputStyle.toModifier()
                    .margin(bottom = 20.px)
                    .width(350.px)
                    .height(80.px)
                    .padding(leftRight = 20.px, topBottom = 10.px)
                    .backgroundColor(Colors.White)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .outline(width = 0.px, style = LineStyle.None, color = Colors.Transparent)
                    .toAttrs {
                        value(bio)
                        onInput { bio = it.value }
                        attr("placeholder", "Bio")
                    }
            )
            Button(
                attrs = Modifier
                    .margin(bottom = 24.px)
                    .width(350.px)
                    .height(54.px)
                    .backgroundColor(JsTheme.Primary.rgb)
                    .color(Colors.White)
                    .borderRadius(r = 4.px)
                    .fontFamily(FONT_FAMILY)
                    .fontWeight(FontWeight.Medium)
                    .fontSize(14.px)
                    .cursor(Cursor.Pointer)
                    .toAttrs {
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
                                context.router.navigateTo("/")
                            }
                        }
                    }
            ) {
                SpanText(text = "Save Profile")
            }
            if (error.isNotBlank()) {
                SpanText(
                    modifier = Modifier
                        .width(350.px)
                        .color(Colors.Red)
                        .textAlign(TextAlign.Center)
                        .fontFamily(FONT_FAMILY),
                    text = error
                )
            }
        }
    }
}
