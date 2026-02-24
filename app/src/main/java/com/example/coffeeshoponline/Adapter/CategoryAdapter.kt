package com.example.coffeeshoponline.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.coffeeshoponline.R
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshoponline.databinding.ViewholderCategoryBinding
import com.example.coffeeshoponline.model.CategoryModel

class CategoryAdapter(val  items: MutableList<CategoryModel>,private val onCategoryClick: (CategoryModel) -> Unit): RecyclerView.Adapter<CategoryAdapter.Viewholder>(){

    private lateinit var context: Context
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class Viewholder(val binding: ViewholderCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderCategoryBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]

        holder.binding.titleCat.text = item.title

        holder.binding.root.setOnClickListener {
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)

            onCategoryClick(item)
        }

        if (selectedPosition == position) {
            holder.binding.titleCat.setBackgroundResource(R.drawable.brown_full_corner_bg)
            holder.binding.titleCat.setTextColor(context.getColor(R.color.white))
        } else {
            holder.binding.titleCat.setBackgroundResource(R.drawable.white_full_corner_bg)
            holder.binding.titleCat.setTextColor(context.getColor(R.color.darkBrown))
        }
    }

    override fun getItemCount(): Int = items.size
}