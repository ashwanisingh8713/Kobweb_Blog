package com.example.blogmultiplatform.pages.admin

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.example.shared.JsTheme
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.px

import com.example.blogmultiplatform.navigation.admin_home_route

@Page(admin_home_route)
@Composable
fun AdminHomePage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpanText(
            modifier = Modifier.fontSize(32.px).fontFamily(FONT_FAMILY).color(JsTheme.Primary.rgb),
            text = "Profile Completed Successfully!"
        )
        SpanText(
            modifier = Modifier.fontSize(18.px).fontFamily(FONT_FAMILY).color(JsTheme.HalfBlack.rgb),
            text = "Welcome to your dashboard."
        )
    }
}

