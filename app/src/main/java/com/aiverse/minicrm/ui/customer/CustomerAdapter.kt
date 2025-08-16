package com.aiverse.minicrm.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aiverse.minicrm.R
import com.aiverse.minicrm.data.model.CustomerEntity

// RecyclerView Adapter to display customers
class CustomerAdapter(
    private var customers: List<CustomerEntity>,
    private val onItemClick: (Int) -> Unit,         // Callback when item clicked
    private val onDeleteClick: (CustomerEntity) -> Unit // Callback when delete clicked
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    // ViewHolder for customer item
    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCompany: TextView = itemView.findViewById(R.id.tvCompany)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        holder.tvName.text = customer.name
        holder.tvCompany.text = customer.company
        holder.tvPhone.text = customer.phone
        holder.tvEmail.text = customer.email

        // Item click opens details
        holder.itemView.setOnClickListener {
            onItemClick(customer.id)
        }

        // Delete button click
        holder.btnDelete.setOnClickListener {
            onDeleteClick(customer)
        }
    }

    override fun getItemCount() = customers.size

    // Update adapter data
    fun updateData(newList: List<CustomerEntity>) {
        customers = newList
        notifyDataSetChanged()
    }
}
