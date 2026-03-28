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
        holder.binding.apply {
            titleTxt.text = item.title
            priceTxt.text = "₹${item.priceSmall}"
            extraTxt.text = item.extra
            descTxt.text = item.description

            if (item.picUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.picUrl[0])
                    .into(itemPic)
            }

            editBtn.setOnClickListener { onEditClick(item) }
            deleteBtn.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<ItemModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}