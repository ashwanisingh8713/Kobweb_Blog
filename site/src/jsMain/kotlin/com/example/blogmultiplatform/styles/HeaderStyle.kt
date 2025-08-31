package com.example.blogmultiplatform.styles

import com.example.shared.JsTheme
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle

val CategoryItemStyle = CssStyle {
    base {
        Modifier
            .color(Colors.White)
            .styleModifier {
                property("transition", "color 200ms")
            }
    }
    cssRule(":any-link") {
        Modifier.color(Colors.White)
    }
    cssRule(":hover") {
        Modifier.color(JsTheme.Primary.rgb)
    }
}