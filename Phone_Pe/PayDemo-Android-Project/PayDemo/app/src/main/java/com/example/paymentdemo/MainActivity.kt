package com.example.paymentdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.paymentdemo.database.AppDatabase
import com.example.paymentdemo.model.NotificationEntity
import com.example.paymentdemo.model.Recipient
import com.example.paymentdemo.model.TransactionEntity
import com.example.paymentdemo.payments.PaymentLogic
import com.example.paymentdemo.repository.PaymentRepository
import com.example.paymentdemo.ui.PayDemoApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PaymentRepository(AppDatabase.get(this))
        val vm = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = PayViewModel(repository) as T
        })[PayViewModel::class.java]
        setContent { PayDemoApp(vm) }
    }
}

class PayViewModel(private val repository: PaymentRepository) : ViewModel() {
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    init { viewModelScope.launch { repository.seedIfEmpty(); if (notifications.value.isEmpty()) repository.addNotification(NotificationEntity(title="Welcome to PayDemo", description="This is a simulated payment environment. No money moves.", timestamp=System.currentTimeMillis())) } }
    fun pay(recipient: Recipient, amount: Double, success: Boolean, onComplete: (TransactionEntity) -> Unit) {
        viewModelScope.launch {
            delay(900)
            val id = PaymentLogic.transactionId()
            val tx = TransactionEntity(id, recipient.name, recipient.upiId, amount, "SENT", if (success) "SUCCESS" else "FAILED", System.currentTimeMillis(), "REF-${id.removePrefix("DEMO-")}")
            repository.saveTransaction(tx)
            repository.addNotification(NotificationEntity(title=if (success) "Demo payment successful" else "Demo payment failed", description="₹${"%.2f".format(amount)} · ${recipient.name}", timestamp=System.currentTimeMillis()))
            onComplete(tx)
        }
    }
    fun markRead(item: NotificationEntity) { viewModelScope.launch { repository.markRead(item) } }
}
