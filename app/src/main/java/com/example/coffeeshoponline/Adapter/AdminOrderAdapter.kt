package com.example.coffeeshoponline.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshoponline.activity.AdminOrderDetailsActivity
import com.example.coffeeshoponline.databinding.ViewholderAdminOrderBinding
import com.example.coffeeshoponline.model.OrderModel
import com.google.firebase.database.FirebaseDatabase

class AdminOrderAdapter(private val orders: List<OrderModel>) :
    RecyclerView.Adapter<AdminOrderAdapter.Viewholder>() {

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
        holder.binding.tvAdminOrderTotal.text = "₹%.2f".format(order.totalAmount.toDouble())
        
        holder.binding.tvAdminOrderMethod.text = order.paymentMethod
        holder.binding.tvAdminOrderStatus.text = order.status

        // Status coloring
        if (order.status.equals("Success", ignoreCase = true) || order.status.equals("Received", ignoreCase = true)) {
            holder.binding.tvAdminOrderStatus.setTextColor(android.graphics.Color.parseColor("#388E3C")) // Green
            holder.binding.btnMarkReceived.visibility = View.GONE
        } else {
            holder.binding.tvAdminOrderStatus.setTextColor(android.graphics.Color.parseColor("#E65100")) // Orange
            
            // Show Mark Received button ONLY if COD and not yet Success
            if (order.paymentMethod.equals("COD", ignoreCase = true)) {
                holder.binding.btnMarkReceived.visibility = View.VISIBLE
            } else {
                holder.binding.btnMarkReceived.visibility = View.GONE
            }
        }

        // Handle Mark as Received Button
        holder.binding.btnMarkReceived.setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance().reference.child("orders").child(order.orderId)
            dbRef.child("status").setValue("Success").addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Order marked as Success", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Failed to update order", Toast.LENGTH_SHORT).show()
            }
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
