package com.aiverse.minicrm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiverse.minicrm.data.repository.DatabaseProvider
import com.aiverse.minicrm.data.network.FirestoreHelper
import com.aiverse.minicrm.ui.customer.AddCustomerActivity
import com.aiverse.minicrm.ui.customer.CustomerAdapter
import com.aiverse.minicrm.ui.customer.CustomerDetailsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var customerAdapter: CustomerAdapter // Adapter for RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvCustomers = findViewById<RecyclerView>(R.id.rvCustomers) // Customer list RecyclerView
        val fabAddCustomer = findViewById<FloatingActionButton>(R.id.fabAddCustomer) // FAB to add new customer

        // Setup RecyclerView with LinearLayoutManager and adapter
        rvCustomers.layoutManager = LinearLayoutManager(this)
        customerAdapter = CustomerAdapter(
            emptyList(),
            onItemClick = { customerId -> // On customer click → open details
                val intent = Intent(this, CustomerDetailsActivity::class.java)
                intent.putExtra("customer_id", customerId)
                startActivity(intent)
            },
            onDeleteClick = { customer -> // On delete click → remove from local DB
                val db = DatabaseProvider.getDatabase(this)
                CoroutineScope(Dispatchers.IO).launch {
                    db.customerDao().delete(customer)
                    runOnUiThread { loadCustomers() } // Refresh list
                }
            }
        )
        rvCustomers.adapter = customerAdapter

        // FAB click → open AddCustomerActivity
        fabAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        // Load initial customers from local database
        loadCustomers()

        // Start listening to Firestore realtime updates
        startRealtimeSync(this)
    }

    // Load customers from local DB and update RecyclerView
    private fun loadCustomers() {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val customers = db.customerDao().getAll()
            runOnUiThread {
                customerAdapter.updateData(customers)
            }
        }
    }

    // Listen for Firestore updates and sync with local DB
    private fun startRealtimeSync(context: Context) {
        val db = DatabaseProvider.getDatabase(context)

        // Listen to customer updates from Firestore
        FirestoreHelper.listenCustomersRealtime { remoteCustomer ->
            CoroutineScope(Dispatchers.IO).launch {
                val local = db.customerDao().getById(remoteCustomer.id)
                if (local == null) {
                    db.customerDao().insert(remoteCustomer.copy(isSynced = 1)) // Insert new customer
                } else if (remoteCustomer.updatedAt > local.updatedAt) {
                    db.customerDao().update(remoteCustomer.copy(isSynced = 1)) // Update if newer
                }
            }
        }

        // Listen to order updates from Firestore
        FirestoreHelper.listenOrdersRealtime { remoteOrder ->
            CoroutineScope(Dispatchers.IO).launch {
                val local = db.orderDao().getById(remoteOrder.id)
                if (local == null) {
                    db.orderDao().insert(remoteOrder.copy(isSynced = 1)) // Insert new order
                } else if (remoteOrder.updatedAt > local.updatedAt) {
                    db.orderDao().update(remoteOrder.copy(isSynced = 1)) // Update if newer
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCustomers() // Refresh customer list after returning from Add/Edit
    }
}
