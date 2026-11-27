package com.example.paymentdemo.payments

import org.junit.Assert.*
import org.junit.Test

class PaymentLogicTest {
    @Test fun acceptsOnlyDocumentedDemoQr() {
        val recipient = PaymentLogic.parseDemoQr("paydemo://pay?name=Rahul%20Sharma&upi=rahul%40demobank&bank=Demo%20Bank")
        assertEquals("Rahul Sharma", recipient?.name)
        assertEquals("rahul@demobank", recipient?.upiId)
        assertNull(PaymentLogic.parseDemoQr("upi://pay?pa=real@bank"))
    }
    @Test fun rejectsMalformedAndRealUpiIds() {
        assertNull(PaymentLogic.parseDemoQr("not-a-qr"))
        assertNull(PaymentLogic.parseDemoQr("paydemo://pay?name=Test&upi=person@realbank"))
    }
    @Test fun validatesAmounts() {
        assertEquals(10.5, PaymentLogic.validateAmount("10.5")!!, 0.0001)
        assertNull(PaymentLogic.validateAmount("0"))
        assertNull(PaymentLogic.validateAmount("-1"))
        assertNull(PaymentLogic.validateAmount("100001"))
        assertNull(PaymentLogic.validateAmount("abc"))
    }
}
