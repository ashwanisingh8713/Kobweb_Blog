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
import com.varabyte.kobweb.compose.ui.styleModifier
import kotlinx.browser.document
import kotlinx.browser.window
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
    var showPassword by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize().styleModifier { property("min-height", "100vh"); property("display","flex"); property("align-items","center"); property("justify-content","center") }, contentAlignment = Alignment.Center) {
        // Auto-width card constrained by max-width so it centers and fits content
        Box(modifier = Modifier.styleModifier { property("width","auto"); property("max-width","900px"); property("background","linear-gradient(180deg,#ffffff,#fbfbff)"); property("border-radius","14px"); property("box-shadow","0 20px 60px rgba(16,24,40,0.08)"); property("padding","28px"); property("margin","0 auto") }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.styleModifier { property("width","100%") }) {
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(22.px).fontWeight(FontWeight.Bold).color(JsTheme.Primary.rgb).margin(bottom = 8.px), text = "Create Account")

                // Inputs
                Input(type = InputType.Text, attrs = LoginInputStyle.toModifier().id(Id.usernameInput).margin(bottom = 12.px).width(100.percent).height(50.px).padding(leftRight = 16.px).styleModifier { property("border-radius","10px"); property("box-shadow","inset 0 1px 2px rgba(16,24,40,0.04)") }.backgroundColor(Colors.White).fontFamily(FONT_FAMILY).fontSize(14.px).outline(width = 0.px, style = LineStyle.None, color = Colors.Transparent).toAttrs { attr("placeholder","User ID") })
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.margin(bottom = 12.px)) {
                    val pwType = if (showPassword) InputType.Text else InputType.Password
                    Input(type = pwType, attrs = LoginInputStyle.toModifier().id(Id.passwordInput).width(100.percent).height(50.px).padding(leftRight = 16.px).styleModifier { property("border-radius","10px"); property("box-shadow","inset 0 1px 2px rgba(16,24,40,0.04)") }.backgroundColor(Colors.White).fontFamily(FONT_FAMILY).fontSize(14.px).outline(width = 0.px, style = LineStyle.None, color = Colors.Transparent).toAttrs { attr("placeholder","Password") })
                    Box(modifier = Modifier.width(8.px))
                    Button(attrs = Modifier.onClick { showPassword = !showPassword }.toAttrs { attr("style", "background:transparent; border:none; color:${JsTheme.Primary.rgb}; cursor:pointer; padding:6px; display:flex; align-items:center; justify-content:center;") }) {
                        Box(modifier = Modifier.id("signupPwIcon").width(18.px).height(18.px))
                    }
                }

                // Role pill toggle
                Row(modifier = Modifier.margin(bottom = 14.px), horizontalArrangement = Arrangement.Center) {
                    Button(attrs = { onClick { selectedRole = "client" }; attr("style","background:${if (selectedRole=="client") JsTheme.Primary.rgb else "transparent"}; color:${if (selectedRole=="client") "white" else JsTheme.Primary.rgb}; border:1px solid ${JsTheme.Primary.rgb}; padding:8px 14px; border-radius:999px; margin-right:8px;") }) { SpanText(text = "Client") }
                    Button(attrs = { onClick { selectedRole = "developer" }; attr("style","background:${if (selectedRole=="developer") JsTheme.Primary.rgb else "transparent"}; color:${if (selectedRole=="developer") "white" else JsTheme.Primary.rgb}; border:1px solid ${JsTheme.Primary.rgb}; padding:8px 14px; border-radius:999px;") }) { SpanText(text = "Developer") }
                }

                Button(attrs = Modifier.margin(bottom = 12.px).width(100.percent).height(48.px).backgroundColor(JsTheme.Primary.rgb).color(Colors.White).borderRadius(r = 8.px).fontFamily(FONT_FAMILY).fontWeight(FontWeight.Medium).fontSize(15.px).cursor(Cursor.Pointer).toAttrs {
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
                }) {
                    if (signUpResult is SignUpResult.Loading) SpanText(text = "Creating account...") else SpanText(text = "Create account")
                }

                if (signUpResult is SignUpResult.Error && !showErrorDialog) {
                    val errorText = when ((signUpResult as SignUpResult.Error).message) {
                        "Username already exists." -> "Username already exists."
                        "Input fields are empty." -> "Input fields are empty."
                        else -> "Server error. Please try again later."
                    }
                    SpanText(modifier = Modifier.width(100.percent).color(Colors.Red).textAlign(TextAlign.Center).fontFamily(FONT_FAMILY), text = errorText)
                }

                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    SpanText(modifier = Modifier.color(Colors.Gray), text = "Already have an account? ")
                    SpanText(modifier = Modifier.color(JsTheme.Primary.rgb).fontWeight(FontWeight.Bold).cursor(Cursor.Pointer).onClick { ctx.router.navigateTo(admin_login_route) }, text = "Sign In")
                }
            }
        }

        // Small helper used by the success dialog button to navigate to login
        val goToLogin = {
            showSuccessDialog = false
            ctx.router.navigateTo(admin_login_route)
        }

        // Overlay error and success dialogs kept unchanged but restyled slightly
        if (showErrorDialog) {
            Box(modifier = Modifier.styleModifier { property("position","fixed"); property("inset","0"); property("display","flex"); property("align-items","center"); property("justify-content","center"); property("background","rgba(2,6,23,0.6)") }) {
                Box(modifier = Modifier.styleModifier { property("background","white"); property("padding","28px"); property("border-radius","10px"); property("box-shadow","0 12px 40px rgba(16,24,40,0.12)") }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SpanText(modifier = Modifier.fontSize(18.px).fontWeight(FontWeight.Bold).margin(bottom = 12.px), text = errorDialogMessage)
                        Button(attrs = Modifier.backgroundColor(JsTheme.Primary.rgb).color(Colors.White).borderRadius(r = 8.px).padding(leftRight = 16.px, topBottom = 8.px).cursor(Cursor.Pointer).onClick { showErrorDialog = false; if (signUpResult is SignUpResult.Error) signUpResult = null }.toAttrs()) { SpanText(text = "OK") }
                     }
                 }
             }
         }
        if (showSuccessDialog) {
            Box(modifier = Modifier.styleModifier { property("position","fixed"); property("inset","0"); property("display","flex"); property("align-items","center"); property("justify-content","center"); property("background","rgba(2,6,23,0.6)") }) {
                Box(modifier = Modifier.styleModifier { property("background","white"); property("padding","28px"); property("border-radius","10px"); property("box-shadow","0 12px 40px rgba(16,24,40,0.12)") }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SpanText(modifier = Modifier.fontSize(18.px).fontWeight(FontWeight.Bold).margin(bottom = 12.px), text = "Account created successfully!")
                        Button(attrs = Modifier.backgroundColor(JsTheme.Primary.rgb).color(Colors.White).borderRadius(r = 8.px).padding(leftRight = 16.px, topBottom = 8.px).cursor(Cursor.Pointer).onClick { goToLogin() }.toAttrs()) { SpanText(text = "Go to Login") }
                      }
                  }
              }
          }
      }

    // Inject SVG icon & animate on toggle for signup password show/hide
    LaunchedEffect(showPassword) {
        try {
            val id = "signupPwIcon"
            val el = document.getElementById(id)
            if (el != null) {
                val svgOpen = """
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z" stroke="${JsTheme.Primary.rgb}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                      <circle cx="12" cy="12" r="3" stroke="${JsTheme.Primary.rgb}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                """.trimIndent()
                val svgOff = """
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a21.38 21.38 0 0 1 5.06-4.94" stroke="${JsTheme.Primary.rgb}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M1 1l22 22" stroke="${JsTheme.Primary.rgb}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                """.trimIndent()
                el.innerHTML = if (showPassword) svgOff else svgOpen
                try {
                    val ed = el.asDynamic().style
                    ed.transition = "transform 160ms ease"
                    ed.transform = "scale(1.12)"
                    window.setTimeout({ ed.transform = "scale(1)" }, 160)
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }
}
