package com.aiverse.minicrm.ui.order

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.aiverse.minicrm.R
import com.aiverse.minicrm.data.repository.DatabaseProvider
import com.aiverse.minicrm.data.network.FirestoreHelper
import com.aiverse.minicrm.data.model.OrderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AddOrderActivity : AppCompatActivity() {

    private var orderId: Int? = null // ID of the order being edited, null if new
    private var customerId: Int = -1 // Customer ID associated with the order

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var etQty: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_order)

        // --- Initialize views ---
        etTitle = findViewById(R.id.etProduct)
        etAmount = findViewById(R.id.etPrice)
        etQty = findViewById(R.id.etQty)
        btnSave = findViewById(R.id.btnSaveOrder)

        // --- Get data from Intent ---
        customerId = intent.getIntExtra("customer_id", -1)
        orderId = intent.getIntExtra("order_id", -1).takeIf { it != -1 }

        if (customerId == -1) {
            Toast.makeText(this, "Customer not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- Load existing order if editing ---
        orderId?.let { loadOrderData(it) }

        // --- Save button click ---
        btnSave.setOnClickListener { saveOrder() }
    }

    // --- Save or update order ---
    private fun saveOrder() {
        val title = etTitle.text.toString().trim()
        val quantity = etQty.text.toString().toSafeInt()
        val amount = etAmount.text.toString().toSafeDouble()

        if (title.isEmpty() || amount <= 0.0) {
            Toast.makeText(this, "Title and Amount required", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(this)
        val now = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val order = OrderEntity(
                    id = orderId ?: 0, // 0 for new insert (autoGenerate must be true)
                    customerId = customerId,
                    orderTitle = title,
                    orderAmount = amount,
                    createdAt = now,
                    updatedAt = now,
                    orderDate = now,
                    isSynced = 0
                )

                if (orderId == null) {
                    db.orderDao().insert(order) // New order
                } else {
                    db.orderDao().update(order) // Update existing order
                }

                // --- Save to Firestore ---
                try {
                    FirestoreHelper.saveOrder(order)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // --- Schedule background sync ---
                scheduleOrderSync(this@AddOrderActivity)

                // --- Show Toast and finish on main thread ---
                runOnUiThread {
                    Toast.makeText(this@AddOrderActivity, "Order saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@AddOrderActivity, "Failed to save order", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Load existing order data into views ---
    private fun loadOrderData(id: Int) {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val order = db.orderDao().getById(id)
                order?.let {
                    runOnUiThread {
                        etTitle.setText(it.orderTitle)
                        etAmount.setText(it.orderAmount.toString())
                        etQty.setText("1") // Optional: default quantity
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Schedule WorkManager to sync orders ---
    private fun scheduleOrderSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<OrderSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}

// --- Helper extension functions ---
fun String?.toSafeDouble(default: Double = 0.0): Double = this?.toDoubleOrNull() ?: default
fun String?.toSafeInt(default: Int = 0): Int = this?.toIntOrNull() ?: default
