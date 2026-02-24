package com.example.coffeeshoponline.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.activity.DetailActivity
import com.example.coffeeshoponline.databinding.ViewholderPopularBinding
import com.example.coffeeshoponline.model.ItemModel

class ItemAdapter(
    private val items: MutableList<ItemModel>
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class ViewHolder(
        val binding: ViewholderPopularBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderPopularBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.subtitleTxt.text = item.extra
        holder.binding.priceTxt.text = "₹${item.priceSmall}"

        // 🔥 Load image
        if (item.picUrl.isNotEmpty()) {
            Glide.with(context)
                .load(item.picUrl[0])
                .into(holder.binding.pic)
        } else {
            holder.binding.pic.setImageDrawable(null)
        }

        // 🔥 CLICK ITEM → OPEN DETAIL PAGE
        holder.binding.root.setOnClickListener {

            // selection (optional UI effect)
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)

            // open detail page
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("item", item) // 🔥 PASS FULL ITEM
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}