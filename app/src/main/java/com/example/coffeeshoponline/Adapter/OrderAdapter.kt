package com.example.coffeeshoponline.Adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.databinding.ViewholderOrderBinding
import com.example.coffeeshoponline.model.OrderModel

class OrderAdapter (private val items: List<OrderModel>) :
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = items[position]

        holder.binding.apply {

            orderDateTxt.text = order.getFormattedDate()
            orderPriceTxt.text = "₹${order.totalAmount}"
            orderStatusTxt.text = order.status
            orderItemsTxt.text = "Order #${order.orderId.takeLast(8)}"
            paymentMethodTxt.text = "Method: ${order.paymentMethod}"

            val coffeeList = order.items ?: emptyList()
            if (coffeeList.isNotEmpty()) {
                val firstCoffee = coffeeList[0]


                holder.binding.itemSummaryTxt.text = if (coffeeList.size > 1) "${firstCoffee.title} + ${coffeeList.size - 1} more" else firstCoffee.title
                holder.binding.sizeQtyTxt.text = "Size: ${firstCoffee.selectedSize} | Total Qty: ${coffeeList.sumOf { it.numberInCart }}"


                if (!firstCoffee.picUrl.isNullOrEmpty()) {
                    val imageUrl = firstCoffee.picUrl[0]

                    com.bumptech.glide.Glide.with(holder.itemView.context)
                        .load(imageUrl)
                        .into(holder.binding.pic)
                }
            }

            // Status Styling
            when (order.status) {
                "Pending" -> {
                    orderStatusTxt.setTextColor(Color.parseColor("#FFA500"))
                    orderStatusTxt.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
                }
                "Success", "Received" -> {
                    orderStatusTxt.setTextColor(Color.parseColor("#388E3C"))
                    orderStatusTxt.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                }
                "Delivered" -> {
                    orderStatusTxt.setTextColor(Color.parseColor("#4CAF50"))
                    orderStatusTxt.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}