package com.example.blogmultiplatform.styles

import com.example.shared.JsTheme
import com.example.blogmultiplatform.util.Id
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle

val NavigationItemStyle = CssStyle {
    base {
        Modifier
            .styleModifier {
                property("transition", "color 300ms, stroke 300ms")
            }
    }
    cssRule(" > #${Id.svgParent} > #${Id.vectorIcon}") {
        Modifier.styleModifier {
            property("stroke", JsTheme.White.hex)
        }
    }
    cssRule(":hover > #${Id.svgParent} > #${Id.vectorIcon}") {
        Modifier.styleModifier {
            property("stroke", JsTheme.Primary.hex)
        }
    }
    cssRule(" > #${Id.navigationText}") {
        Modifier.color(JsTheme.White.rgb)
    }
    cssRule(":hover > #${Id.navigationText}") {
        Modifier.color(JsTheme.Primary.rgb)
    }
}