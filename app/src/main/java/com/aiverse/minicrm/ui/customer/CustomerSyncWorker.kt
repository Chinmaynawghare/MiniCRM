package com.aiverse.minicrm.ui.customer

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aiverse.minicrm.data.repository.DatabaseProvider
import com.aiverse.minicrm.data.network.FirestoreHelper

// Worker to sync unsynced customers with Firestore in the background
class CustomerSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = DatabaseProvider.getDatabase(applicationContext)

        return try {
            // Get all customers that are not yet synced
            val unsyncedCustomers = db.customerDao().getUnsyncedCustomers()

            unsyncedCustomers.forEach { customer ->
                try {
                    // Push customer to Firestore
                    FirestoreHelper.saveCustomer(customer)
                    // Mark as synced in local DB
                    db.customerDao().markCustomerAsSynced(customer.id)
                } catch (e: Exception) {
                    // Stop processing and retry later if Firestore/network fails
                    return Result.retry()
                }
            }

            Result.success() // All customers synced successfully

        } catch (e: Exception) {
            // Retry if there is any database failure
            Result.retry()
        }
    }
}
