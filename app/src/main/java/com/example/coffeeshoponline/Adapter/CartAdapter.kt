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
    var listItems: ArrayList<ItemModel>,
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
        val item = listItems[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.numberInCartTxt.text = item.numberInCart.toString()

        // 1. Function to calculate and update prices based on selection
        fun updatePriceUI() {
            val unitPrice = when (item.selectedSize.uppercase()) {
                "MEDIUM" -> item.priceMedium
                "LARGE" -> item.priceLarge
                else -> item.priceSmall
            }
            holder.binding.feeEachItem.text = "₹$unitPrice"
            holder.binding.totalEachItem.text = "₹${unitPrice * item.numberInCart}"

            // Update visual selection state
            updateSizeButtonStyles(holder, item.selectedSize)
        }

        // 2. Initial UI setup
        updatePriceUI()

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .into(holder.binding.picCart)


        holder.binding.sizeS.setOnClickListener {
            item.selectedSize = "SMALL"
            updatePriceUI()

        }

        holder.binding.sizeM.setOnClickListener {
            item.selectedSize = "MEDIUM"
            updatePriceUI()
        }

        holder.binding.sizeL.setOnClickListener {
            item.selectedSize = "LARGE"
            updatePriceUI()
        }

        // Existing buttons
        holder.binding.plusBtn.setOnClickListener {
            managementCart.plusItem(listItems, position, listener)
        }

        holder.binding.minusBtn.setOnClickListener {
            managementCart.minusItem(listItems, position, listener)
        }

        holder.binding.removeItemBtn.setOnClickListener {
            managementCart.romveItem(listItems, position, listener)
        }
    }


    private fun updateSizeButtonStyles(holder: ViewHolder, selectedSize: String) {
        val context = holder.itemView.context
        val darkBrown = context.getColor(com.example.coffeeshoponline.R.color.darkBrown)
        val white = context.getColor(android.R.color.white)
        val strokeBg = com.example.coffeeshoponline.R.drawable.brown_storke_bg

        val sizes = mapOf(
            "SMALL" to holder.binding.sizeS,
            "MEDIUM" to holder.binding.sizeM,
            "LARGE" to holder.binding.sizeL
        )

        sizes.forEach { (sizeName, textView) ->
            if (sizeName == selectedSize.uppercase()) {
                // Selected Style: Solid Brown background, White text
                textView.setBackgroundColor(darkBrown)
                textView.setTextColor(white)
            } else {
                // Unselected Style: Stroke background, Brown text
                textView.setBackgroundResource(strokeBg)
                textView.setTextColor(darkBrown)
            }
        }
    }

    override fun getItemCount(): Int = listItems.size
}