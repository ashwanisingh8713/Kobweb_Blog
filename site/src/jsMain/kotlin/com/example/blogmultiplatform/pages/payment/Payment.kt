package com.example.blogmultiplatform.pages.payment

import androidx.compose.runtime.*
import com.example.blogmultiplatform.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.document
import kotlinx.browser.window
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

    data class PaymentEntry(val amount: Double, val crypto: String, val note: String?, val releasedBy: String, val ts: Long)

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
    // Confirmation / success UI state
    var confirmOpen by remember { mutableStateOf(false) }
    var successOpen by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf<Double?>(null) }
    var pendingCrypto by remember { mutableStateOf("ETH") }
    var pendingNote by remember { mutableStateOf<String?>(null) }

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
                    val crypto = data.crypto as? String ?: "ETH"
                    val entry = PaymentEntry(amount, crypto, note, from ?: "unknown", ts)
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

    // Perform the actual release given explicit values (used by confirmed flow)
    fun performRelease(amount: Double, crypto: String, note: String?) {
        val entry = PaymentEntry(amount, crypto, note, clientId, Date().getTime().toLong())
        payments = payments + entry
        lastReleased = entry
        // broadcast
        if (bcPayment != null) {
            try {
                val msg = js("({})")
                msg.type = "payment_released"
                msg.amount = amount
                msg.note = note ?: ""
                msg.from = clientId
                msg.time = js("Date.now()")
                msg.crypto = crypto
                bcPayment.postMessage(msg)
            } catch (e: dynamic) {
                console.log("failed to post payment message:", e)
            }
        }
        // clear form DOM inputs
        try {
            (document.getElementById("amountInput") as? org.w3c.dom.HTMLInputElement)?.value = ""
            (document.getElementById("noteInput") as? org.w3c.dom.HTMLInputElement)?.value = ""
            // reset select to default
            (document.getElementById("cryptoSelect") as? org.w3c.dom.HTMLSelectElement)?.value = "ETH"
        } catch (_: Throwable) {}
        println("Payment released locally: ${'$'}{entry.amount}")
        // show success toast
        successOpen = true
        try { window.setTimeout({ successOpen = false }, 2500) } catch (_: Throwable) {}
    }

    fun clearHistory() {
        payments = listOf()
        lastReleased = null
    }

    // Theme colors
    val primaryColor = "#6C5CE7" // violet
    val subtleBg = "#F7F8FB"

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.px)
            .styleModifier { property("background", "linear-gradient(180deg, #f6f8ff 0%, #f7fbf9 100%)") },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header card
        Box(modifier = Modifier.fillMaxWidth().maxWidth(1100.px).styleModifier { property("padding","18px"); property("border-radius","12px"); property("background","white"); property("box-shadow","0 8px 30px rgba(16,24,40,0.08)") }.padding(bottom = 12.px)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(24.px).margin(bottom = 6.px), text = "Payments")
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(14.px).color(Colors.Gray), text = "Release funds and notify other open tabs instantly.")
            }
        }

        Box(modifier = Modifier.height(16.px))

        Box(modifier = Modifier.fillMaxWidth().maxWidth(1100.px).styleModifier { property("background", subtleBg); property("border-radius","12px"); property("padding","18px"); property("box-shadow","inset 0 -1px 0 rgba(16,24,40,0.02)") }) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left form
                Column(modifier = Modifier.width(48.percent).padding(12.px)) {
                    SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(16.px).margin(bottom = 10.px), text = "Release Payment")
                    // Crypto selector + amount on separate rows
                    // Crypto selector
                    Box {
                        // container for crypto select (injected as innerHTML)
                        Input(type = InputType.Text, attrs = { attr("hidden", "true") }) // spacer hack for layout
                    }
                    // Use a raw select via attrs on a native element (compose doesn't provide a higher-level Select here)
                    // We'll output a select using innerHTML on a container div for simplicity
                    val selectHtml = "<select id=\"cryptoSelect\" style=\"width:100%;padding:10px;border-radius:10px;border:1px solid #e6e9f2;font-size:14px;\">" +
                            "<option value=\"ETH\">ETH</option>" +
                            "<option value=\"BTC\">BTC</option>" +
                            "<option value=\"USDT\">USDT</option>" +
                            "<option value=\"USDC\">USDC</option>" +
                            "<option value=\"BNB\">BNB</option>" +
                            "<option value=\"MATIC\">MATIC</option>" +
                            "<option value=\"SOL\">SOL</option>" +
                            "</select>"
                    // inject select markup inside a container div
                    val selectContainerId = "cryptoSelectContainer"
                    // ensure container exists and set innerHTML
                    LaunchedEffect(Unit) {
                        try {
                            val c = document.getElementById(selectContainerId)
                            if (c != null) c.innerHTML = selectHtml
                        } catch (_: Throwable) {}
                    }
                    Box(modifier = Modifier.id(selectContainerId).fillMaxWidth())
                    Box(modifier = Modifier.height(8.px))
                    Input(type = InputType.Number, attrs = { attr("id", "amountInput"); attr("placeholder", "Amount") ; attr("style", "width:100%; padding:12px; border-radius:10px; border:1px solid #e6e9f2; font-size:14px;") })
                    Box(modifier = Modifier.height(10.px))
                    Input(type = InputType.Text, attrs = { attr("id", "noteInput"); attr("placeholder", "Note (optional)"); attr("style","width:100%; padding:12px; border-radius:10px; border:1px solid #e6e9f2; font-size:14px;") })
                    Box(modifier = Modifier.height(14.px))
                    Row(horizontalArrangement = Arrangement.Start) {
                        Button(attrs = { onClick {
                                // capture pending values and open confirm
                                try {
                                    val amountEl = document.getElementById("amountInput") as? org.w3c.dom.HTMLInputElement
                                    val noteEl = document.getElementById("noteInput") as? org.w3c.dom.HTMLInputElement
                                    val cryptoEl = document.getElementById("cryptoSelect") as? org.w3c.dom.HTMLSelectElement
                                    val amount = amountEl?.value?.toDoubleOrNull() ?: 0.0
                                    if (amount <= 0.0) return@onClick
                                    pendingAmount = amount
                                    pendingCrypto = cryptoEl?.value ?: "ETH"
                                    pendingNote = noteEl?.value?.ifBlank { null }
                                    confirmOpen = true
                                } catch (_: Throwable) {}
                            }; attr("style","background:${primaryColor}; color:white; border:none; padding:10px 14px; border-radius:10px; cursor:pointer; font-weight:600;") }) { Text("Release Payment") }
                        Box(modifier = Modifier.width(12.px))
                        Button(attrs = { onClick { clearHistory() }; attr("style","background:transparent; color:${primaryColor}; border:1px solid ${primaryColor}; padding:10px 14px; border-radius:10px; cursor:pointer; font-weight:600;") }) { Text("Clear History") }
                    }
                    Box(modifier = Modifier.height(12.px))
                    SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(12.px).color(Colors.Gray), text = "When a payment is released in one tab, other tabs will receive a broadcast notification and display it.")
                }

                Box(modifier = Modifier.width(24.px))

                // Right activity/history
                Column(modifier = Modifier.width(48.percent).padding(12.px)) {
                    SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(16.px).margin(bottom = 8.px), text = "Activity")
                    Box(modifier = Modifier.fillMaxWidth().height(160.px).styleModifier { property("background","white"); property("border-radius","10px"); property("padding","12px"); property("box-shadow","0 8px 24px rgba(16,24,40,0.04)"); property("overflow-y","auto") }) {
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
                    Box(modifier = Modifier.id(historyContainerId).fillMaxWidth().height(160.px).styleModifier { property("overflow-y", "auto"); property("padding", "8px"); property("border-radius","8px"); property("background","#fbfbfb"); property("box-shadow","inset 0 -1px 0 rgba(16,24,40,0.04)") }) {
                        Column {
                            for (p in payments.reversed()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.px).styleModifier{ property("padding","10px"); property("border-radius","8px"); property("background-color","white"); property("box-shadow","0 6px 18px rgba(16,24,40,0.04)") }) {
                                    SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(13.px), text = p.amount.toString() + " " + p.crypto + " — by " + (if (p.releasedBy == clientId) "You" else p.releasedBy) + " at " + Date(p.ts).toLocaleString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation modal
    if (confirmOpen) {
        Box(modifier = Modifier.styleModifier { property("position","fixed"); property("inset","0"); property("display","flex"); property("align-items","center"); property("justify-content","center"); property("background","rgba(2,6,23,0.6)") }) {
            Box(modifier = Modifier.styleModifier { property("width","420px"); property("background","white"); property("border-radius","12px"); property("padding","18px"); property("box-shadow","0 18px 50px rgba(2,6,23,0.36)") }) {
                Column {
                    SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(18.px).margin(bottom = 8.px), text = "Confirm Payment")
                    val amtText = pendingAmount?.toString() ?: "0"
                    SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(14.px).margin(bottom = 12.px), text = "You are about to release $amtText ${'$'}{pendingCrypto}. Proceed?")
                    Row {
                        Button(attrs = { onClick {
                                // proceed
                                confirmOpen = false
                                pendingAmount?.let { performRelease(it, pendingCrypto, pendingNote) }
                                // reset pending
                                pendingAmount = null
                                pendingCrypto = "ETH"
                                pendingNote = null
                            } }) { Text("Proceed") }
                        Box(modifier = Modifier.width(12.px))
                        Button(attrs = { onClick { confirmOpen = false } }) { Text("Cancel") }
                    }
                }
            }
        }
    }

    // Success toast
    if (successOpen) {
        Box(modifier = Modifier.styleModifier { property("position","fixed"); property("right","24px"); property("top","24px"); property("z-index","9999") }) {
            Box(modifier = Modifier.styleModifier { property("background","#10B981"); property("color","white"); property("padding","10px 14px"); property("border-radius","8px"); property("box-shadow","0 8px 30px rgba(16,24,40,0.12)") }) {
                SpanText(modifier = Modifier.fontFamily(FONT_FAMILY).fontSize(14.px), text = "Payment successfully done")
            }
        }
    }
}
