package com.example.paymentdemo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TxStatus { SUCCESS, FAILED }
enum class TxType { SENT, RECEIVED }

data class Recipient(val name: String, val upiId: String, val bank: String = "Demo Bank") : java.io.Serializable

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val recipientName: String,
    val recipientUpiId: String,
    val amount: Double,
    val transactionType: String,
    val transactionStatus: String,
    val timestamp: Long,
    val referenceNumber: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val notificationId: Long = 0,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

data class Offer(val title: String, val description: String, val tag: String)
