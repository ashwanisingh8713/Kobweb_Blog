package com.example.blogmultiplatform.styles

import com.example.shared.JsTheme
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle

val EditorKeyStyle = CssStyle {
    base {
        Modifier
            .backgroundColor(Colors.Transparent)
            .styleModifier {
                property("transition", "background-color 300ms")
            }
    }
    cssRule(":hover") {
        Modifier.backgroundColor(JsTheme.Primary.rgb)
    }
}