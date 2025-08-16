package com.aiverse.minicrm.ui.customer

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.aiverse.minicrm.R
import com.aiverse.minicrm.data.model.CustomerEntity
import com.aiverse.minicrm.data.repository.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

// Activity for adding/editing/deleting a customer
class AddCustomerActivity : AppCompatActivity() {

    private var customerId: Int? = null // Stores current customer ID if editing
    private var existingCustomer: CustomerEntity? = null // Holds existing customer data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_customer)

        // Input fields
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etCompany = findViewById<EditText>(R.id.etCompany)

        // Buttons
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnFetchRandom = findViewById<Button>(R.id.btnFetchRandom)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        // Always show Delete button
        btnDelete.visibility = View.VISIBLE

        // Get customer ID if editing an existing customer
        customerId = intent.getIntExtra("customer_id", -1).takeIf { it != -1 }
        customerId?.let { loadCustomerData(it, etName, etEmail, etPhone, etCompany) }

        // Fetch random company info from API
        btnFetchRandom.setOnClickListener {
            fetchRandomCompany(etName, etEmail, etPhone, etCompany)
        }

        // Save customer data
        btnSave.setOnClickListener {
            saveCustomer(etName, etEmail, etPhone, etCompany)
        }

        // Delete customer
        btnDelete.setOnClickListener {
            deleteCustomer()
        }
    }

    // Load customer data from database if editing
    private fun loadCustomerData(
        id: Int,
        etName: EditText,
        etEmail: EditText,
        etPhone: EditText,
        etCompany: EditText
    ) {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val customer = db.customerDao().getById(id)
            existingCustomer = customer
            customer?.let {
                withContext(Dispatchers.Main) {
                    etName.setText(it.name)
                    etEmail.setText(it.email)
                    etPhone.setText(it.phone)
                    etCompany.setText(it.company)
                }
            }
        }
    }

    // Save a new customer or update existing one
    private fun saveCustomer(
        etName: EditText,
        etEmail: EditText,
        etPhone: EditText,
        etCompany: EditText
    ) {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val company = etCompany.text.toString().trim()

        // Validate required fields
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            if (customerId == null) {
                // Insert new customer
                db.customerDao().insert(
                    CustomerEntity(
                        name = name,
                        email = email,
                        phone = phone,
                        company = company,
                        isSynced = 0, // Mark as unsynced
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                // Update existing customer
                existingCustomer?.let { existing ->
                    db.customerDao().update(
                        CustomerEntity(
                            id = customerId!!,
                            name = name,
                            email = email,
                            phone = phone,
                            company = company,
                            isSynced = 0, // Mark as unsynced
                            createdAt = existing.createdAt,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // Schedule sync with server
            scheduleCustomerSync(this@AddCustomerActivity)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddCustomerActivity, "Customer saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Delete the current customer
    private fun deleteCustomer() {
        if (customerId == null || existingCustomer == null) {
            Toast.makeText(this, "No customer to delete", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            existingCustomer?.let { db.customerDao().delete(it) }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddCustomerActivity, "Customer deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Schedule background sync using WorkManager
    private fun scheduleCustomerSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<CustomerSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    // Fetch random company details from API and populate fields
    private fun fetchRandomCompany(
        etName: EditText,
        etEmail: EditText,
        etPhone: EditText,
        etCompany: EditText
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiUrl = "https://fakerapi.it/api/v1/companies?_locale=en_US&_quantity=1"
                val response = URL(apiUrl).readText()
                val json = JSONObject(response)
                val companyArray = json.getJSONArray("data")
                val companyObj = companyArray.getJSONObject(0)

                val name = companyObj.optString("name", "John Doe")
                val email = companyObj.optString("email", "john@example.com")
                val phone = companyObj.optString("phone", "0000000000")
                val company = companyObj.optString("type", "Independent Company") // safe fallback

                withContext(Dispatchers.Main) {
                    etName.setText(name)
                    etEmail.setText(email)
                    etPhone.setText(phone)
                    etCompany.setText(company)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddCustomerActivity,
                        "Failed to fetch company, using fallback",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Fallback values
                    etName.setText("John Doe")
                    etEmail.setText("john@example.com")
                    etPhone.setText("0000000000")
                    etCompany.setText("Independent Company")
                }
            }
        }
    }
}
