package com.example.blogmultiplatform.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import com.example.blogmultiplatform.components.CategoryNavigationItems
import com.example.blogmultiplatform.components.SearchBar
import com.example.shared.Category
import com.example.shared.JsTheme
import com.example.blogmultiplatform.navigation.Screen
import com.example.blogmultiplatform.util.Constants.HEADER_HEIGHT
import com.example.blogmultiplatform.util.Constants.PAGE_WIDTH
import com.example.blogmultiplatform.util.Id
import com.example.blogmultiplatform.util.Res
import com.stevdza.san.kotlinbs.components.BSButton
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaBars
import com.varabyte.kobweb.silk.components.icons.fa.FaXmark
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.browser.localStorage
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.get
import org.w3c.dom.set
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.StorageEvent
import org.w3c.dom.events.Event
//import org.w3c.dom.events.StorageEvent

// New: simple visual switch component
@Composable
fun ToggleSwitch(isOn: Boolean, onToggle: (Boolean) -> Unit) {
    // track
    Box(
        modifier = Modifier
            .width(46.px)
            .height(24.px)
            .borderRadius(r = 12.px)
            .backgroundColor(if (isOn) JsTheme.Primary.rgb else Colors.Gray)
            .cursor(Cursor.Pointer)
            .onClick { onToggle(!isOn) }
            .margin(right = 8.px),
        contentAlignment = Alignment.CenterStart
    ) {
        // knob
        Box(
            modifier = Modifier
                .width(20.px)
                .height(20.px)
                .borderRadius(r = 10.px)
                .backgroundColor(Colors.White)
                .margin(left = if (isOn) 24.px else 2.px)
        )
    }
}

@Composable
fun HeaderSection(
    breakpoint: Breakpoint,
    selectedCategory: Category? = null,
    logo: String = Res.Image.logoHome,
    onMenuOpen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .backgroundColor(JsTheme.Secondary.rgb),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .backgroundColor(JsTheme.Secondary.rgb)
                .maxWidth(PAGE_WIDTH.px),
            contentAlignment = Alignment.TopCenter
        ) {
            Header(
                breakpoint = breakpoint,
                logo = logo,
                selectedCategory = selectedCategory,
                onMenuOpen = onMenuOpen
            )
        }
    }
}

@Composable
fun Header(
    breakpoint: Breakpoint,
    logo: String,
    selectedCategory: Category?,
    onMenuOpen: () -> Unit
) {
    val context = rememberPageContext()
    var fullSearchBarOpened by remember { mutableStateOf(false) }
    // Toggle state persisted in localStorage
    var headerToggleOn by remember { mutableStateOf(localStorage["headerToggle"]?.toBoolean() ?: false) }

    // Read display name from localStorage and react to storage events
    var displayName by remember { mutableStateOf(localStorage["displayName"] ?: "") }
    // Read avatarUrl from localStorage so we can show the avatar in the header
    var avatarUrl by remember { mutableStateOf(localStorage["avatarUrl"] ?: "") }

    DisposableEffect(Unit) {
        val handler = { e: Event ->
            val se = e as? StorageEvent
            if (se != null) {
                // Update individual keys if provided
                if (se.key == "displayName") displayName = se.newValue ?: ""
                if (se.key == "avatarUrl") avatarUrl = se.newValue ?: ""
                // If key is null (e.g., storage.clear), reload both to be safe
                if (se.key == null) {
                    displayName = localStorage["displayName"] ?: ""
                    avatarUrl = localStorage["avatarUrl"] ?: ""
                }
            }
        }
        val profileUpdatedHandler = { e: Event ->
            try {
                // The Profile page dispatches a simple Event('profileUpdated')
                // Read fresh values from localStorage so the header updates in this tab
                displayName = localStorage["displayName"] ?: ""
                avatarUrl = localStorage["avatarUrl"] ?: ""
            } catch (_: Throwable) {
                // ignore
            }
        }
        window.addEventListener("storage", handler)
        window.addEventListener("profileUpdated", profileUpdatedHandler)
        onDispose {
            window.removeEventListener("storage", handler)
            window.removeEventListener("profileUpdated", profileUpdatedHandler)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(if (breakpoint > Breakpoint.MD) 80.percent else 90.percent)
            .height(HEADER_HEIGHT.px),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (breakpoint <= Breakpoint.MD) {
            if (fullSearchBarOpened) {
                FaXmark(
                    modifier = Modifier
                        .margin(right = 24.px)
                        .color(Colors.White)
                        .cursor(Cursor.Pointer)
                        .onClick { fullSearchBarOpened = false },
                    size = IconSize.XL
                )
            }
            if (!fullSearchBarOpened) {
                FaBars(
                    modifier = Modifier
                        .margin(right = 24.px)
                        .color(Colors.White)
                        .cursor(Cursor.Pointer)
                        .onClick { onMenuOpen() },
                    size = IconSize.XL
                )
            }
        }
        if (!fullSearchBarOpened) {
            Image(
                modifier = Modifier
                    .margin(right = 50.px)
                    .width(if (breakpoint >= Breakpoint.SM) 100.px else 70.px)
                    .cursor(Cursor.Pointer)
                    .onClick { context.router.navigateTo(Screen.HomePage.route) },
                src = logo,
                alt = "Logo Image"
            )
        }
        if (breakpoint >= Breakpoint.LG) {
            CategoryNavigationItems(selectedCategory = selectedCategory)
        }
        Spacer()
        SearchBar(
            breakpoint = breakpoint,
            fullWidth = fullSearchBarOpened,
            darkTheme = true,
            onEnterClick = {
                val query = (document.getElementById(Id.adminSearchBar) as HTMLInputElement).value
                context.router.navigateTo(Screen.SearchPage.searchByTitle(query = query))
            },
            onSearchIconClick = { fullSearchBarOpened = it }
        )
        Spacer()
        // Simple inline nav items: Chat and Profile
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Chat: client-side route
            SpanText(modifier = Modifier.cursor(Cursor.Pointer).margin(right = 12.px).color(Colors.White).onClick { context.router.navigateTo(Screen.ChatPage.route) }, text = "Chat")
            // Profile: use a plain anchor to ensure the route is reachable even without JS router firing
            A(href = Screen.ProfilePage.route, attrs = {
                style { property("text-decoration", "none") }
            }) {
                SpanText(modifier = Modifier.cursor(Cursor.Pointer).color(Colors.White), text = "Profile")
            }
        }
        Box(modifier = Modifier.width(8.px))
        ToggleSwitch(isOn = headerToggleOn, onToggle = {
            headerToggleOn = it
            localStorage["headerToggle"] = headerToggleOn.toString()
        })
        // small spacer using Box
        Box(modifier = Modifier.width(12.px))

        if (displayName.isNotBlank()) {
            // show display name instead of Sign in
            SpanText(modifier = Modifier.cursor(Cursor.Pointer).margin(right = 8.px).color(Colors.White).onClick { context.router.navigateTo(Screen.ProfilePage.route) }, text = "Hi, $displayName")
        } else {
            BSButton(
                text = "Sign in",
                onClick = {context.router.navigateTo(Screen.AdminLogin.route)}
            )
        }
        // Avatar: show a small circular avatar if available; clicking it goes to the profile page
        Box(modifier = Modifier.width(8.px))
        if (avatarUrl.isNotBlank()) {
            Box(modifier = Modifier.width(36.px).height(36.px).borderRadius(r = 18.px).backgroundColor(Colors.White).cursor(Cursor.Pointer).onClick { context.router.navigateTo(Screen.ProfilePage.route) }) {
                Image(modifier = Modifier.width(36.px).height(36.px).borderRadius(r = 18.px), src = avatarUrl, alt = "Avatar")
            }
        }
    }
}