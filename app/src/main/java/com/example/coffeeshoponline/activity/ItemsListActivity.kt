package com.example.coffeeshoponline.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.util.Log
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeshoponline.Adapter.ItemListCategoryAdapter
import com.example.coffeeshoponline.R
import com.google.firebase.database.*
import com.example.coffeeshoponline.databinding.ActivityItemsListBinding
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ItemsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemsListBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ItemListCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getIntExtra("categoryId", -1)
        val categoryName = intent.getStringExtra("categoryName")

        binding.CategoryTxt.text = categoryName
        binding.backBtn.setOnClickListener { finish() }
        binding.imageView6.setOnClickListener { finish() }

        database = FirebaseDatabase
            .getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
            .getReference("items")

        if (categoryId != -1) {
            loadCategoryItems(categoryId)
        }
    }

    private fun loadCategoryItems(categoryId: Int) {
        binding.progressBar.visibility = View.VISIBLE

        Log.d("FIREBASE", "Requested categoryId: $categoryId")

        val query = database
            .orderByChild("categoryId")
            .equalTo(categoryId.toDouble()) // ✅ VERY IMPORTANT

        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val itemList = ArrayList<ItemModel>()

                Log.d("FIREBASE", "Items found: ${snapshot.childrenCount}")

                for (itemSnap in snapshot.children) {
                    val item = itemSnap.getValue(ItemModel::class.java)
                    item?.let { itemList.add(it) }
                }

                updateUI(itemList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", error.message)
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun updateUI(list: ArrayList<ItemModel>) {
        binding.ListView.layoutManager = GridLayoutManager(this, 2)
        adapter = ItemListCategoryAdapter(list)
        binding.ListView.adapter = adapter
        binding.progressBar.visibility = View.GONE
    }
}
