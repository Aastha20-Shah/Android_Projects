package com.example.coffeeshoponline.Helper

import android.content.Context
import android.widget.Toast
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.collections.indexOfFirst

class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    private fun getCartKey(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) {
            "CartList_" + user.uid
        } else {
            "CartList_Guest"
        }
    }

    fun insertItems(item: ItemModel) {
        val listItem = getListCart()
        val existAlready = listItem.any { it.title == item.title }
        val index = listItem.indexOfFirst { it.title == item.title }

        if (existAlready) {
            listItem[index].numberInCart = item.numberInCart
        } else {
            listItem.add(item)
        }
        tinyDB.putListObject(getCartKey(), listItem)
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<ItemModel> {
        return tinyDB.getListObject(getCartKey()) ?: arrayListOf()
    }

    fun minusItem(listItems: ArrayList<ItemModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (listItems[position].numberInCart == 1) {
            listItems.removeAt(position)
        } else {
            listItems[position].numberInCart--
        }
        tinyDB.putListObject(getCartKey(), listItems)
        listener.onChanged()
    }

    fun romveItem(listItems: ArrayList<ItemModel>, position: Int, listener: ChangeNumberItemsListener) {
        listItems.removeAt(position)
        tinyDB.putListObject(getCartKey(), listItems)
        listener.onChanged()
    }

    fun plusItem(listItems: ArrayList<ItemModel>, position: Int, listener: ChangeNumberItemsListener) {
        listItems[position].numberInCart++
        tinyDB.putListObject(getCartKey(), listItems)
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listItem = getListCart()
        var fee = 0.0
        for (item in listItem) {
            val price = when (item.selectedSize.uppercase()) {
                "MEDIUM" -> item.priceMedium
                "LARGE" -> item.priceLarge
                else -> item.priceSmall
            }
            fee += price * item.numberInCart
        }
        return fee
    }

    fun clearCart() {
        tinyDB.putListObject(getCartKey(), ArrayList())
    }
}