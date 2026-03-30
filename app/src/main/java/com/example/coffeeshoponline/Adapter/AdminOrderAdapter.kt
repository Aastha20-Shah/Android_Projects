package com.example.coffeeshoponline.Adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshoponline.activity.AdminOrderDetailsActivity
import com.example.coffeeshoponline.databinding.ViewholderAdminOrderBinding
import com.example.coffeeshoponline.model.OrderModel

class AdminOrderAdapter(
    private val orders: List<OrderModel>
) : RecyclerView.Adapter<AdminOrderAdapter.Viewholder>() {

    class Viewholder(val binding: ViewholderAdminOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderAdminOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val order = orders[position]

        holder.binding.tvAdminOrderUserName.text = order.userName
        holder.binding.tvAdminOrderId.text = "Order ID: ${order.orderId}"
        holder.binding.tvAdminOrderDate.text = "Date: ${order.getFormattedDate()}"
        holder.binding.tvAdminOrderTotal.text = "₹%.2f".format(order.totalAmount)
        
        holder.binding.tvAdminOrderMethod.text = order.paymentMethod
        holder.binding.tvAdminOrderStatus.text = order.status

        // Status coloring
        if (order.status.equals("Success", ignoreCase = true)) {
            holder.binding.tvAdminOrderStatus.setTextColor(Color.parseColor("#388E3C")) // Green
        } else {
            holder.binding.tvAdminOrderStatus.setTextColor(Color.parseColor("#E65100")) // Orange
        }

        // Click on item to see details
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AdminOrderDetailsActivity::class.java)
            intent.putExtra("order", order)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size
}