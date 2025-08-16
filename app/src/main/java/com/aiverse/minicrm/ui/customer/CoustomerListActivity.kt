package com.aiverse.minicrm.ui.customer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiverse.minicrm.R
import com.aiverse.minicrm.data.model.CustomerEntity
import com.aiverse.minicrm.data.repository.DatabaseProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Activity to show a list of customers
class CustomerListActivity : AppCompatActivity() {

    private lateinit var adapter: CustomerAdapter // RecyclerView adapter
    private val customerList = mutableListOf<CustomerEntity>() // Stores customers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        val recyclerView = findViewById<RecyclerView>(R.id.rvCustomers)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddCustomer)

        // Initialize adapter with click listeners
        adapter = CustomerAdapter(
            customerList,
            onItemClick = { customerId ->
                // Open customer details when item clicked
                val intent = Intent(this, CustomerDetailsActivity::class.java)
                intent.putExtra("customer_id", customerId)
                startActivity(intent)
            },
            onDeleteClick = { customer ->
                // Delete customer when delete button clicked
                deleteCustomer(customer)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Floating Action Button to add new customer
        fab.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }
    }

    // Reload customer list when returning to this activity
    override fun onResume() {
        super.onResume()
        loadCustomers()
    }

    // Load all customers from the database
    private fun loadCustomers() {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val customers = db.customerDao().getAll()
            withContext(Dispatchers.Main) {
                customerList.clear()
                customerList.addAll(customers)
                adapter.notifyDataSetChanged()
            }
        }
    }

    // Delete a customer from database and update UI
    private fun deleteCustomer(customer: CustomerEntity) {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.customerDao().delete(customer)
            withContext(Dispatchers.Main) {
                customerList.remove(customer)
                adapter.notifyDataSetChanged()
                Toast.makeText(this@CustomerListActivity, "Customer deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
