package com.example.blogmultiplatform.pages.payment

import androidx.compose.runtime.*
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.example.shared.JsTheme
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.document
import kotlin.js.Date
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import kotlin.random.Random

@Page("/payment")
@Composable
fun PaymentScreen() {
    js("console.log('PaymentScreen mounted')")

    data class PaymentEntry(val amount: Double, val note: String?, val releasedBy: String, val ts: Long)

    // local client id for ignoring own broadcast messages
    val clientId = remember { Random.nextInt(0, 1_000_000_000).toString() }

    // BroadcastChannel for payment notifications
    val bcPayment = remember {
        try {
            val ch = js("new BroadcastChannel('webrtc-payment')")
            js("console.log('BroadcastChannel payment created')")
            ch
        } catch (_: Throwable) {
            js("console.log('BroadcastChannel payment not available')")
            null
        }
    }

    // UI state
    var payments by remember { mutableStateOf(listOf<PaymentEntry>()) }
    var lastReleased by remember { mutableStateOf<PaymentEntry?>(null) }
    // DOM id for history container so we can auto-scroll
    val historyContainerId = "paymentHistoryContainer"

    // Auto-scroll the history container to bottom when payments change
    LaunchedEffect(payments) {
        try {
            val el = document.getElementById(historyContainerId)
            if (el != null) {
                try {
                    val ed = el.asDynamic()
                    ed.scrollTop = ed.scrollHeight
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    // Listen for payment broadcasts from other tabs
    DisposableEffect(bcPayment) {
        val handler = fun(ev: dynamic) {
            try {
                val data = ev.data
                if (data == null) return
                val type = data.type as? String
                if (type == "payment_released") {
                    val from = data.from as? String
                    if (from == clientId) return // ignore our own message
                    val amount = (data.amount as? Number)?.toDouble() ?: 0.0
                    val note = data.note as? String
                    val ts = (data.time as? Number)?.toLong() ?: Date().getTime().toLong()
                    val entry = PaymentEntry(amount, note, from ?: "unknown", ts)
                    payments = payments + entry
                    lastReleased = entry
                }
            } catch (e: Throwable) {
                console.log("bcPayment handler error:", e)
            }
        }
        if (bcPayment != null) bcPayment.onmessage = handler
        onDispose { try { bcPayment?.close() } catch (_: Throwable) {} }
    }

    fun releasePayment() {
        // Read values from inputs (avoid relying on Input value callback signatures)
        val amountEl = document.getElementById("amountInput") as? org.w3c.dom.HTMLInputElement
        val noteEl = document.getElementById("noteInput") as? org.w3c.dom.HTMLInputElement
        val amount = amountEl?.value?.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) return
        val note = noteEl?.value?.ifBlank { null }
        val entry = PaymentEntry(amount, note, clientId, Date().getTime().toLong())
        payments = payments + entry
        lastReleased = entry
        // broadcast
        if (bcPayment != null) {
            try {
                val msg = js("({})")
                msg.type = "payment_released"
                msg.amount = amount
                msg.note = entry.note ?: ""
                msg.from = clientId
                msg.time = js("Date.now()")
                bcPayment.postMessage(msg)
            } catch (e: dynamic) {
                console.log("failed to post payment message:", e)
            }
        }
        // clear form DOM inputs
        try { amountEl?.value = ""; noteEl?.value = "" } catch (_: Throwable) {}
        println("Payment released locally: ${'$'}{entry.amount}")
    }

    fun clearHistory() {
        payments = listOf()
        lastReleased = null
    }

    // UI
    Column(
        modifier = Modifier.fillMaxSize().padding(24.px).backgroundColor(JsTheme.LightGray.rgb),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(22.px).margin(bottom = 16.px), text = "Payments")

        Row(modifier = Modifier.fillMaxWidth().height(260.px).backgroundColor(Colors.White).borderRadius(r = 10.px).padding(12.px)) {
            // Left: form
            Column(modifier = Modifier.width(48.percent).padding(12.px)) {
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(16.px).margin(bottom = 8.px), text = "Release Payment")
                Input(type = InputType.Number, attrs = { attr("id", "amountInput"); attr("placeholder", "Amount") })
                Box(modifier = Modifier.height(8.px))
                Input(type = InputType.Text, attrs = { attr("id", "noteInput"); attr("placeholder", "Note (optional)") })
                Box(modifier = Modifier.height(12.px))
                Row(horizontalArrangement = Arrangement.Start) {
                    Button(attrs = { onClick { releasePayment() } }) { Text("Release Payment") }
                    Box(modifier = Modifier.width(12.px))
                    Button(attrs = { onClick { clearHistory() } }) { Text("Clear History") }
                }
                Box(modifier = Modifier.height(12.px))
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(12.px).color(Colors.Gray), text = "When a payment is released in one tab, other tabs will receive a broadcast notification and display it.")
            }

            Box(modifier = Modifier.width(24.px))

            // Right: activity / last released
            Column(modifier = Modifier.width(48.percent).padding(12.px)) {
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(16.px).margin(bottom = 8.px), text = "Activity")
                Box(modifier = Modifier.fillMaxWidth().height(160.px).backgroundColor(JsTheme.LightGray.rgb).borderRadius(r = 10.px).padding(12.px).styleModifier { property("overflow-y", "auto"); property("box-shadow", "0 6px 18px rgba(16,24,40,0.06)") }) {
                    Column {
                        if (lastReleased != null) {
                            val lr = lastReleased!!
                            SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(14.px).margin(bottom = 6.px), text = "Last released: " + lr.amount.toString() + " by " + (if (lr.releasedBy == clientId) "You" else lr.releasedBy))
                            SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(12.px).color(Colors.Gray), text = "Note: " + (lr.note ?: "-"))
                        } else {
                            SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(14.px), text = "No payments released yet")
                        }
                    }
                }

                Box(modifier = Modifier.height(12.px))

                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(14.px).margin(bottom = 8.px), text = "History")
                Box(modifier = Modifier.id(historyContainerId).fillMaxWidth().height(120.px).styleModifier { property("overflow-y", "auto"); property("padding", "8px"); property("border-radius","8px"); property("background","#fbfbfb"); property("box-shadow","inset 0 -1px 0 rgba(16,24,40,0.04)") }) {
                    Column {
                        for (p in payments.reversed()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 6.px).styleModifier{ property("padding","8px"); property("border-radius","6px"); property("background-color","white"); property("box-shadow","0 2px 6px rgba(16,24,40,0.04)") }) {
                                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(13.px), text = p.amount.toString() + " — by " + (if (p.releasedBy == clientId) "You" else p.releasedBy) + " at " + Date(p.ts).toLocaleString())
                            }
                        }
                    }
                }
            }
        }

        //SpanText(modifier = Modifier.margin(top = 12.px), text = "Open multiple tabs to test broadcasted payment releases.")
    }
}
