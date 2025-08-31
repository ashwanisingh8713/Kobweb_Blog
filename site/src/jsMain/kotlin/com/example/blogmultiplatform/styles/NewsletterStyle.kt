package com.example.blogmultiplatform.styles

import com.example.shared.JsTheme
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.outline
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px

val NewsletterInputStyle = CssStyle {
    base {
        Modifier
            .outline(
                width = 1.px,
                style = LineStyle.Solid,
                color = Colors.Transparent
            )
            .border(
                width = 1.px,
                style = LineStyle.Solid,
                color = Colors.Transparent
            )
            .styleModifier {
                property("transition", "border 300ms, outline 300ms")
            }
    }
    cssRule(":focus") {
        Modifier
            .outline(
                width = 1.px,
                style = LineStyle.Solid,
                color = JsTheme.Primary.rgb
            )
            .border(
                width = 1.px,
                style = LineStyle.Solid,
                color = JsTheme.Primary.rgb
            )
    }
}