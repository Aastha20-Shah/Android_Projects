package com.example.coffeeshoponline.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.activity.DetailActivity
import com.example.coffeeshoponline.databinding.ViewholderItemListBinding
import com.example.coffeeshoponline.model.ItemModel

class WishlistAdapter(
    private val items: MutableList<ItemModel>
) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewholderItemListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderItemListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Text data
        holder.binding.titleTxt.text = item.title
        holder.binding.subtitleTxt.text = item.extra
        holder.binding.priceTxt.text = "₹${item.priceSmall}"

        // Image (NO placeholder)
        if (item.picUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.picUrl[0])
                .into(holder.binding.pic)
        } else {
            holder.binding.pic.setImageDrawable(null)
        }

        // Item click → Detail page
        holder.binding.root.setOnClickListener {
            val intent = Intent(
                holder.itemView.context,
                DetailActivity::class.java
            )
            intent.putExtra("item", item)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}
