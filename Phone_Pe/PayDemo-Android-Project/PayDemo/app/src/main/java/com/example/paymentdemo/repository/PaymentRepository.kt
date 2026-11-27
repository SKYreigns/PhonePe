package com.example.paymentdemo.repository

import com.example.paymentdemo.database.AppDatabase
import com.example.paymentdemo.model.NotificationEntity
import com.example.paymentdemo.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val db: AppDatabase) {
    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()
    val notifications: Flow<List<NotificationEntity>> = db.notificationDao().observeAll()
    suspend fun saveTransaction(tx: TransactionEntity) = db.transactionDao().insert(tx)
    suspend fun addNotification(item: NotificationEntity) = db.notificationDao().insert(item)
    suspend fun markRead(item: NotificationEntity) = db.notificationDao().update(item.copy(isRead = true))
    suspend fun seedIfEmpty() {
        if (db.transactionDao().count() == 0) {
            val now = System.currentTimeMillis()
            db.transactionDao().insert(TransactionEntity("DEMO-INIT-001", "Aarav Mehta", "aarav@demobank", 245.0, "SENT", "SUCCESS", now - 86_400_000, "REF-DEMO-001"))
            db.transactionDao().insert(TransactionEntity("DEMO-INIT-002", "Mira Kapoor", "mira@demobank", 1200.0, "RECEIVED", "SUCCESS", now - 172_800_000, "REF-DEMO-002"))
            db.transactionDao().insert(TransactionEntity("DEMO-INIT-003", "Rohan Das", "rohan@demobank", 89.0, "SENT", "FAILED", now - 259_200_000, "REF-DEMO-003"))
        }
    }
}
