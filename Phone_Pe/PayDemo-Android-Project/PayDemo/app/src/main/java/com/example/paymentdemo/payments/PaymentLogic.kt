package com.example.paymentdemo.payments

import com.example.paymentdemo.model.Recipient
import java.net.URI
import java.util.UUID

object PaymentLogic {
    fun parseDemoQr(raw: String): Recipient? = runCatching {
        val uri = URI(raw.trim())
        require(uri.scheme == "paydemo" && uri.host == "pay")
        val params = uri.rawQuery.orEmpty().split('&').mapNotNull {
            val pair = it.split('=', limit = 2)
            if (pair.size == 2) pair[0] to java.net.URLDecoder.decode(pair[1], "UTF-8") else null
        }.toMap()
        val name = params["name"].orEmpty()
        val upi = params["upi"].orEmpty()
        require(name.isNotBlank() && upi.matches(Regex("[a-zA-Z0-9._-]+@demobank")))
        Recipient(name, upi, params["bank"]?.takeIf { it.isNotBlank() } ?: "Demo Bank")
    }.getOrNull()

    fun validateAmount(value: String): Double? = value.trim().toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 && it <= 100000.0 }
    fun transactionId(): String = "DEMO-${UUID.randomUUID().toString().take(8).uppercase()}"
}
