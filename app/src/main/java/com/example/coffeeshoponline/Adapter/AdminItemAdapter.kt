package com.example.coffeeshoponline.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.databinding.ViewholderAdminItemBinding
import com.example.coffeeshoponline.model.ItemModel

class AdminItemAdapter(
    private val items: MutableList<ItemModel>,
    private val onEditClick: (ItemModel) -> Unit,
    private val onDeleteClick: (ItemModel) -> Unit
) : RecyclerView.Adapter<AdminItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderAdminItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderAdminItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.priceTxt.text = "₹${item.priceSmall}"

        if (item.picUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.picUrl[0])
                .into(holder.binding.itemPic)
        }

        holder.binding.editBtn.setOnClickListener { onEditClick(item) }
        holder.binding.deleteBtn.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<ItemModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}