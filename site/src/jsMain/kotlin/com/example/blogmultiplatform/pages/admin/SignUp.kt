package com.example.blogmultiplatform.pages.admin

import androidx.compose.runtime.*
import com.example.blogmultiplatform.navigation.admin_login_route
import com.example.blogmultiplatform.navigation.admin_signup_route
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
import com.varabyte.kobweb.core.Page
import org.jetbrains.compose.web.css.px
import com.varabyte.kobweb.core.PageContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.blogmultiplatform.models.User
import com.example.blogmultiplatform.util.Id
import com.varabyte.kobweb.compose.ui.modifiers.id
import kotlinx.browser.document
import com.example.blogmultiplatform.util.createUserAccount

@Page(admin_signup_route)
@Composable
fun SignUpScreen(ctx: PageContext) {
    val scope = rememberCoroutineScope()
    var errorText by remember { mutableStateOf(" ") }
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
                text = "Create Account"
            )
            Input(
                type = InputType.Text,
                attrs = LoginInputStyle.toModifier()
                    .id(Id.usernameInput)
                    .margin(bottom = 12.px)
                    .width(350.px)
                    .height(54.px)
                    .padding(leftRight = 20.px)
                    .backgroundColor(Colors.White)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .outline(width = 0.px, style = LineStyle.None, color = Colors.Transparent)
                    .toAttrs {
                        attr("placeholder", "User ID")
                    }
            )
            Input(
                type = InputType.Password,
                attrs = LoginInputStyle.toModifier()
                    .id(Id.passwordInput)
                    .margin(bottom = 20.px)
                    .width(350.px)
                    .height(54.px)
                    .padding(leftRight = 20.px)
                    .backgroundColor(Colors.White)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .outline(width = 0.px, style = LineStyle.None, color = Colors.Transparent)
                    .toAttrs {
                        attr("placeholder", "Password")
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
                            scope.launch {
                                val username = (document.getElementById(Id.usernameInput) as? org.w3c.dom.HTMLInputElement)?.value ?: ""
                                val password = (document.getElementById(Id.passwordInput) as? org.w3c.dom.HTMLInputElement)?.value ?: ""
                                if (username.isNotEmpty() && password.isNotEmpty()) {
                                    val user = User(username = username, password = password)
                                    val createdUser = createUserAccount(user)
                                    if (createdUser != null) {
                                        ctx.router.navigateTo(admin_login_route)
                                    } else {
                                        errorText = "Username already exists."
                                        delay(3000)
                                        errorText = " "
                                    }
                                } else {
                                    errorText = "Input fields are empty."
                                    delay(3000)
                                    errorText = " "
                                }
                            }
                        }
                    }
            ) {
                SpanText(text = "Create account")
            }
            Row(
                modifier = Modifier.width(350.px),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SpanText(
                    modifier = Modifier.color(Colors.Gray),
                    text = "Already have an account? "
                )
                SpanText(
                    modifier = Modifier
                        .color(JsTheme.Primary.rgb)
                        .fontWeight(FontWeight.Bold)
                        .margin(left = 4.px)
                        .cursor(Cursor.Pointer)
                        .onClick { ctx.router.navigateTo(admin_login_route) },
                    text = "Sign In"
                )
            }
            SpanText(
                modifier = Modifier
                    .width(350.px)
                    .color(Colors.Red)
                    .textAlign(TextAlign.Center)
                    .fontFamily(FONT_FAMILY),
                text = errorText
            )
        }
    }
}
