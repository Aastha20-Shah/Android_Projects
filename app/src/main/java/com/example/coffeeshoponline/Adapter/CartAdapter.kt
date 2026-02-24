package com.example.coffeeshoponline.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.Helper.ChangeNumberItemsListener
import com.example.coffeeshoponline.Helper.ManagmentCart
import com.example.coffeeshoponline.databinding.ViewholderCartBinding
import com.example.coffeeshoponline.model.ItemModel

class CartAdapter(
    private val listItem: ArrayList<ItemModel>,
    private val managementCart: ManagmentCart,
    private val listener: ChangeNumberItemsListener
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewholderCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.numberInCartTxt.text = item.numberInCart.toString()

        val unitPrice = when (item.selectedSize.uppercase()) {
            "MEDIUM" -> item.priceMedium
            "LARGE" -> item.priceLarge
            else -> item.priceSmall // Default to small
        }

        holder.binding.feeEachItem.text = "₹$unitPrice"
        holder.binding.totalEachItem.text =
            "₹${unitPrice * item.numberInCart}"

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .into(holder.binding.picCart)

        holder.binding.plusBtn.setOnClickListener {
            managementCart.plusItem(listItem, position, listener)
        }

        holder.binding.minusBtn.setOnClickListener {
            managementCart.minusItem(listItem, position, listener)
        }

        holder.binding.removeItemBtn.setOnClickListener {
            managementCart.romveItem(listItem, position, listener)
        }
    }

    override fun getItemCount(): Int = listItem.size
}