package com.example.coffeeshoponline.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.databinding.ViewholderAdminOrderItemBinding
import com.example.coffeeshoponline.model.OrderItem

class AdminOrderItemAdapter(private val items: List<OrderItem>) :
    RecyclerView.Adapter<AdminOrderItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderAdminOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderAdminOrderItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvItemTitle.text = item.title
        holder.binding.tvItemDetails.text = "Size: ${item.selectedSize} | Qty: ${item.numberInCart}"
        
        val unitPrice = when (item.selectedSize.uppercase()) {
            "MEDIUM" -> item.priceMedium
            "LARGE" -> item.priceLarge
            else -> item.priceSmall
        }
        holder.binding.tvItemPrice.text = "₹${unitPrice * item.numberInCart}"

        if (item.picUrl != null && item.picUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.picUrl[0])
                .into(holder.binding.ivItemPic)
        }
    }

    override fun getItemCount(): Int = items.size
}
