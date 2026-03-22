package com.example.coffeeshoponline.Helper
import android.content.Context
import android.widget.Toast
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WishlistManager(private val context: Context) {

    private val database = FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/").reference

    fun getWishlist(onComplete: (ArrayList<ItemModel>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onComplete(arrayListOf())
            return
        }

        database.child("users").child(uid).child("wishlist")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = arrayListOf<ItemModel>()
                    for (child in snapshot.children) {
                        child.getValue(ItemModel::class.java)?.let { list.add(it) }
                    }
                    onComplete(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(arrayListOf())
                }
            })
    }

    fun toggleWishlist(item: ItemModel, onComplete: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        val ref = database.child("users").child(uid).child("wishlist").child(item.id.toString())

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ref.removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            } else {
                item.isFavorite = true
                ref.setValue(item).addOnSuccessListener {
                    Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                }
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun isInWishlist(itemId: Int, onComplete: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onComplete(false)
            return
        }

        database.child("users").child(uid).child("wishlist").child(itemId.toString())
            .get().addOnSuccessListener { snapshot ->
                onComplete(snapshot.exists())
            }.addOnFailureListener {
                onComplete(false)
            }
    }
}
