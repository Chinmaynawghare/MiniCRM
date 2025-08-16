package com.aiverse.minicrm.ui.order

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aiverse.minicrm.data.repository.DatabaseProvider
import com.aiverse.minicrm.data.network.FirestoreHelper
class OrderSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = DatabaseProvider.getDatabase(applicationContext)

        // 1️⃣ Fetch all orders that haven't been synced yet
        val unsyncedOrders = db.orderDao().getUnsyncedOrders()

        // 2️⃣ Try syncing each order
        unsyncedOrders.forEach { order ->
            try {
                // Upload to Firestore
                FirestoreHelper.saveOrder(order)

                // Mark as synced in local Room DB
                db.orderDao().markOrderAsSynced(order.id)
            } catch (e: Exception) {
                // If any error occurs, retry the work later
                return Result.retry()
            }
        }

        // 3️⃣ All orders synced successfully
        return Result.success()
    }
}
