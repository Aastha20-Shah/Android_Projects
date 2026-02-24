package com.example.coffeeshoponline.Helper
import android.content.Context
import android.widget.Toast
import com.example.coffeeshoponline.model.ItemModel
class WishlistManager (private val context: Context) {

    private val tinyDB = TinyDB(context)

    fun getWishlist(): ArrayList<ItemModel> {
        return tinyDB.getListObject("Wishlist") ?: arrayListOf()
    }

    fun toggleWishlist(item: ItemModel): Boolean {
        val list = getWishlist()
        val index = list.indexOfFirst { it.id == item.id }

        return if (index >= 0) {
            list.removeAt(index)
            item.isFavorite = false
            tinyDB.putListObject("Wishlist", list)

            Toast.makeText(
                context,
                "Removed from wishlist",
                Toast.LENGTH_SHORT
            ).show()

            false
        } else {
            item.isFavorite = true
            list.add(item)
            tinyDB.putListObject("Wishlist", list)

            Toast.makeText(
                context,
                "Added to wishlist",
                Toast.LENGTH_SHORT
            ).show()

            true
        }
    }

    fun isInWishlist(itemId: Int): Boolean {
        return getWishlist().any { it.id == itemId }
    }
}
