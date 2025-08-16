package com.aiverse.minicrm.ui.customer

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiverse.minicrm.ui.order.AddOrderActivity
import com.aiverse.minicrm.R
import com.aiverse.minicrm.data.repository.DatabaseProvider
import com.aiverse.minicrm.ui.order.OrderAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Displays customer details and associated orders
class CustomerDetailsActivity : AppCompatActivity() {

    private var customerId: Int = -1
    private lateinit var ordersAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_details)

        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvEmail = findViewById<TextView>(R.id.tvDetailEmail)
        val tvPhone = findViewById<TextView>(R.id.tvDetailPhone)
        val tvCompany = findViewById<TextView>(R.id.tvDetailCompany)
        val rvOrders = findViewById<RecyclerView>(R.id.rvOrders)
        val fabAddOrder = findViewById<FloatingActionButton>(R.id.fabAddOrder)

        // Get customer ID from Intent
        customerId = intent.getIntExtra("customer_id", -1)
        if (customerId == -1) {
            finish() // Close if invalid ID
            return
        }

        // RecyclerView setup for orders
        rvOrders.layoutManager = LinearLayoutManager(this)
        ordersAdapter = OrderAdapter(emptyList(), customerId)
        rvOrders.adapter = ordersAdapter

        // Load customer info and orders
        loadCustomerDetails(tvName, tvEmail, tvPhone, tvCompany)
        loadOrders()

        // FAB → Add new order
        fabAddOrder.setOnClickListener {
            val intent = Intent(this, AddOrderActivity::class.java).apply {
                putExtra("customer_id", customerId)
            }
            startActivity(intent)
        }
    }

    // Fetch and display customer details
    private fun loadCustomerDetails(
        tvName: TextView,
        tvEmail: TextView,
        tvPhone: TextView,
        tvCompany: TextView
    ) {
        val db = DatabaseProvider.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val customer = db.customerDao().getById(customerId)
            customer?.let {
                launch(Dispatchers.Main) {
                    tvName.text = it.name
                    tvEmail.text = it.email
                    tvPhone.text = it.phone
                    tvCompany.text = it.company
                }
            } ?: launch(Dispatchers.Main) {
                finish() // Close if customer not found
            }
        }
    }

    // Fetch and display orders for this customer
    private fun loadOrders() {
        val db = DatabaseProvider.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val orders = db.orderDao().getOrdersForCustomer(customerId)
            launch(Dispatchers.Main) {
                ordersAdapter.updateData(orders)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadOrders() // Refresh orders when returning from Add/Edit
    }
}
