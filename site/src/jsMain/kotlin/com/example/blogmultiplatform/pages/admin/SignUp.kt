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
import com.example.blogmultiplatform.models.User
import com.example.blogmultiplatform.util.Id
import com.varabyte.kobweb.compose.ui.modifiers.id
import kotlinx.browser.document
import com.example.blogmultiplatform.util.createUserAccount
import com.example.blogmultiplatform.util.SignUpResult

@Page(admin_signup_route)
@Composable
fun SignUpScreen(ctx: PageContext) {
    val scope = rememberCoroutineScope()
    var signUpResult by remember { mutableStateOf<SignUpResult?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }
    // New: role state
    var selectedRole by remember { mutableStateOf("client") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Always render the form
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
            // Role selection
            Row(
                modifier = Modifier
                    .width(350.px)
                    .margin(bottom = 20.px),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                SpanText(modifier = Modifier.margin(right = 12.px), text = "Role:")
                // Client radio
                Input(type = InputType.Radio, attrs = Modifier.toAttrs {
                    attr("name", "role")
                    attr("value", "client")
                    if (selectedRole == "client") attr("checked", "")
                    onClick {
                        selectedRole = "client"
                    }
                })
                // Increased spacing after the client label
                SpanText(modifier = Modifier.margin(left = 8.px, right = 32.px), text = "Client")
                // Developer radio with left margin to create more space
                Input(type = InputType.Radio, attrs = Modifier.margin(left = 24.px).toAttrs {
                    attr("name", "role")
                    attr("value", "developer")
                    if (selectedRole == "developer") attr("checked", "")
                    onClick {
                        selectedRole = "developer"
                    }
                })
                SpanText(modifier = Modifier.margin(left = 8.px), text = "Developer")
            }
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
                        if (signUpResult is SignUpResult.Loading) attr("disabled", "")
                        onClick {
                            scope.launch {
                                val username = (document.getElementById(Id.usernameInput) as? org.w3c.dom.HTMLInputElement)?.value ?: ""
                                val password = (document.getElementById(Id.passwordInput) as? org.w3c.dom.HTMLInputElement)?.value ?: ""
                                if (username.isNotEmpty() && password.isNotEmpty()) {
                                    signUpResult = SignUpResult.Loading(true)
                                    val user = User(username = username, password = password, role = selectedRole)
                                    when (val result = createUserAccount(user)) {
                                        is SignUpResult.Success -> {
                                            signUpResult = null
                                            showSuccessDialog = true
                                        }
                                        is SignUpResult.Error -> {
                                            signUpResult = result
                                            errorDialogMessage = result.message
                                            showErrorDialog = true
                                        }
                                        else -> {}
                                    }
                                } else {
                                    signUpResult = SignUpResult.Error("Input fields are empty.")
                                    errorDialogMessage = "Input fields are empty."
                                    showErrorDialog = true
                                }
                            }
                        }
                    }
            ) {
                if (signUpResult is SignUpResult.Loading) {
                    SpanText(text = "Creating account...")
                } else {
                    SpanText(text = "Create account")
                }
            }
            if (signUpResult is SignUpResult.Error && !showErrorDialog) {
                val errorText = when ((signUpResult as SignUpResult.Error).message) {
                    "Username already exists." -> "Username already exists."
                    "Input fields are empty." -> "Input fields are empty."
                    else -> "Server error. Please try again later."
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
        }
        // Overlay error dialog
        if (showErrorDialog) {
            Box(
                modifier = Modifier
                    .position(Position.Fixed)
                    .top(0.px)
                    .left(0.px)
                    .width(100.vw)
                    .height(100.vh)
                    .backgroundColor(Colors.Black.copy(alpha = 200))
                    .zIndex(9999),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .backgroundColor(Colors.White)
                        .padding(32.px)
                        .borderRadius(8.px),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontSize(18.px)
                            .fontWeight(FontWeight.Bold)
                            .margin(bottom = 16.px),
                        text = errorDialogMessage
                    )
                    Button(
                        attrs = Modifier
                            .backgroundColor(JsTheme.Primary.rgb)
                            .color(Colors.White)
                            .borderRadius(r = 4.px)
                            .padding(leftRight = 16.px, topBottom = 8.px)
                            .cursor(Cursor.Pointer)
                            .toAttrs {
                                onClick {
                                    showErrorDialog = false
                                    if (signUpResult is SignUpResult.Error) signUpResult = null
                                }
                            },
                    ) {
                        SpanText(text = "OK")
                    }
                }
            }
        }
        // Overlay success dialog
        if (showSuccessDialog) {
            Box(
                modifier = Modifier
                    .position(Position.Fixed)
                    .top(0.px)
                    .left(0.px)
                    .width(100.vw)
                    .height(100.vh)
                    .backgroundColor(Colors.Black.copy(alpha = 200))
                    .zIndex(9999),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .backgroundColor(Colors.White)
                        .padding(32.px)
                        .borderRadius(8.px),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontSize(18.px)
                            .fontWeight(FontWeight.Bold)
                            .margin(bottom = 16.px),
                        text = "Account created successfully!"
                    )
                    Button(
                        attrs = Modifier
                            .backgroundColor(JsTheme.Primary.rgb)
                            .color(Colors.White)
                            .borderRadius(r = 4.px)
                            .padding(leftRight = 16.px, topBottom = 8.px)
                            .cursor(Cursor.Pointer)
                            .toAttrs {
                                onClick {
                                    showSuccessDialog = false
                                    ctx.router.navigateTo(admin_login_route)
                                }
                            },
                    ) {
                        SpanText(text = "Go to Login")
                    }
                }
            }
        }
    }
}
