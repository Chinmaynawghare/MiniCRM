package com.aiverse.minicrm.ui.order

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiverse.minicrm.R
import com.aiverse.minicrm.data.repository.DatabaseProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrdersActivity : AppCompatActivity() {

    private var customerId: Int = -1
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        // --- Get the customer ID from intent ---
        customerId = intent.getIntExtra("customer_id", -1)
        if (customerId == -1) { // Invalid customer ID, close activity
            finish()
            return
        }

        // --- Initialize RecyclerView ---
        val recyclerView = findViewById<RecyclerView>(R.id.rvOrders)
        adapter = OrderAdapter(emptyList(), customerId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // --- Load orders from local DB ---
        loadOrders()

        // --- FloatingActionButton: Add new order ---
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddOrder)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddOrderActivity::class.java).apply {
                putExtra("customer_id", customerId)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the order list when coming back from Add/Edit screen
        loadOrders()
    }

    private fun loadOrders() {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val orders = db.orderDao().getOrdersForCustomer(customerId)
            runOnUiThread {
                adapter.updateData(orders)
            }
        }
    }
}
