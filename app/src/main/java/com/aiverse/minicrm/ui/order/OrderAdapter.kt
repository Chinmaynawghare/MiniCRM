package com.aiverse.minicrm.ui.order

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aiverse.minicrm.R
import com.aiverse.minicrm.data.model.OrderEntity
import java.text.SimpleDateFormat
import java.util.*


class OrderAdapter(
    private var orders: List<OrderEntity>,
    private val customerId: Int
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvOrderTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvOrderAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvOrderDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        // Bind order data to views
        holder.tvTitle.text = order.orderTitle
        holder.tvAmount.text = "₹${order.orderAmount}"
        holder.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(order.orderDate))

        // On item click, open AddOrderActivity for editing
        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, AddOrderActivity::class.java).apply {
                putExtra("customer_id", customerId)
                putExtra("order_id", order.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size


    fun updateData(newOrders: List<OrderEntity>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
