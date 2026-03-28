package com.example.coffeeshoponline.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshoponline.databinding.ViewholderAdminCategoryBinding
import com.example.coffeeshoponline.model.CategoryModel

class AdminCategoryAdapter(
    private var categories: List<CategoryModel>,
    private val onEditClick: (CategoryModel) -> Unit,
    private val onDeleteClick: (CategoryModel) -> Unit
) : RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderAdminCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderAdminCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryTitleTxt.text = category.title

        holder.binding.editCategoryBtn.setOnClickListener { onEditClick(category) }
        holder.binding.deleteCategoryBtn.setOnClickListener { onDeleteClick(category) }
    }

    override fun getItemCount(): Int = categories.size

    fun updateData(newList: List<CategoryModel>) {
        this.categories = newList.toList() // Store a copy to avoid concurrency issues
        notifyDataSetChanged()
    }
}