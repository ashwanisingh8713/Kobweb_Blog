package com.example.blogmultiplatform.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.blogmultiplatform.sections.HeaderSection
import com.example.blogmultiplatform.util.Constants.PAGE_WIDTH
import com.stevdza.san.kotlinbs.components.BSButton
import com.varabyte.kobweb.silk.components.icons.fa.FaPencil
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.styleModifier
import org.jetbrains.compose.web.css.px
import kotlinx.browser.localStorage
import com.varabyte.kobweb.core.rememberPageContext

@Composable
fun AdminHeader() {
    val context = rememberPageContext()
    val isLoggedIn = localStorage.getItem("isLoggedIn") == "true"
    val userName = localStorage.getItem("userName") ?: ""
    val profileComplete = localStorage.getItem("profileComplete") == "true"
    Row(
        modifier = Modifier.margin(top = 16.px, right = 32.px),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isLoggedIn) {
            BSButton(
                text = "Sign in",
                onClick = { context.router.navigateTo(com.example.blogmultiplatform.navigation.Screen.AdminLogin.route) },
                modifier = Modifier.margin(right = 12.px)
            )
        } else {
            BSButton(
                text = userName,
                onClick = {},
                modifier = Modifier.margin(right = 8.px)
            )
            if (!profileComplete) {
                val role = localStorage.getItem("role") ?: ""
                val profileRoute = if (role.equals("Client", ignoreCase = true)) {
                    "/client/profile"
                } else {
                    "/developer/profile"
                }
                FaPencil(
                    modifier = Modifier
                        .cursor(com.varabyte.kobweb.compose.css.Cursor.Pointer)
                        .margin(right = 12.px)
                        .color(Colors.Black)
                        .onClick { context.router.navigateTo(profileRoute) },
                    size = IconSize.LG
                )
            } else {
                BSButton(
                    text = "Logout",
                    onClick = {
                        localStorage.clear()
                        context.router.navigateTo(com.example.blogmultiplatform.navigation.Screen.AdminLogin.route)
                    },
                    modifier = Modifier.margin(right = 12.px)
                )
            }
        }
    }
}

@Composable
fun AdminPageLayout(content: @Composable () -> Unit) {
    var overflowOpened by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        // Overlay header at top right
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .margin(top = 16.px, right = 32.px)
                    .fillMaxSize()
                    .styleModifier {
                        property("position", "fixed")
                        property("top", "0px")
                        property("right", "0px")
                        property("z-index", "1000")
                    },
                contentAlignment = Alignment.TopEnd
            ) {
                AdminHeader()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .maxWidth(PAGE_WIDTH.px)
            ) {
                SidePanel(onMenuClick = {
                    overflowOpened = true
                })
                if (overflowOpened) {
                    OverflowSidePanel(
                        onMenuClose = {
                            overflowOpened = false
                        },
                        content = {
                            NavigationItems()
                        }
                    )
                }
                content()
            }
        }
    }
}