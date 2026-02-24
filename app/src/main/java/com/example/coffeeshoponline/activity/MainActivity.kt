package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.Adapter.CategoryAdapter
import com.example.coffeeshoponline.Adapter.ItemAdapter
import com.example.coffeeshoponline.databinding.ActivityMainBinding
import com.example.coffeeshoponline.model.BannerModel
import com.example.coffeeshoponline.model.CategoryModel
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference

    private lateinit var popularAdapter: ItemAdapter
    private lateinit var moreAdapter: ItemAdapter

    private var isPopularExpanded = false
    private var isMoreExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase
            .getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
            .reference

        loadBanner()
        loadCategories()
        loadPopularItems()
        loadMoreCoffee()
        binding.seeAllPopular.setOnClickListener {
            isPopularExpanded = true
            loadPopularItems()
        }

        binding.seeAllMore.setOnClickListener {
            isMoreExpanded = true
            loadMoreCoffee()
        }

        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.whishList.setOnClickListener {
            startActivity(Intent(this, WishListActivity::class.java))
        }
    }

    // ---------------- BANNER ----------------
    private fun loadBanner() {
        binding.progressBarBanner.visibility = View.VISIBLE

        database.child("banners").child("banner1")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val url = snapshot.child("url").getValue(String::class.java)

                    Glide.with(this@MainActivity)
                        .load(url)
                        .into(binding.banner)

                    binding.progressBarBanner.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBarBanner.visibility = View.GONE
                }
            })
    }

    // ---------------- CATEGORY ----------------
    private fun loadCategories() {
        binding.progressBarCategory.visibility = View.VISIBLE

        database.child("categories")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<CategoryModel>()

                    for (child in snapshot.children) {
                        val category = child.getValue(CategoryModel::class.java)
                        category?.let { list.add(it) }
                    }

                    binding.categoryView.layoutManager =
                        LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )

                    binding.categoryView.adapter =
                        CategoryAdapter(list) { category ->
                            val intent = Intent(this@MainActivity, ItemsListActivity::class.java)
                            intent.putExtra("categoryId", category.id)
                            intent.putExtra("categoryName", category.title)
                            startActivity(intent)
                        }

                    binding.progressBarCategory.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBarCategory.visibility = View.GONE
                }
            })
    }

    // ---------------- POPULAR ITEMS ----------------
    private fun loadPopularItems() {
        binding.progressBarPopular.visibility = View.VISIBLE

        database.child("items")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ItemModel>()

                    for (child in snapshot.children) {
                        val item = child.getValue(ItemModel::class.java)
                        if (item?.isPopular == 1) {
                            list.add(item)
                        }
                    }

                    val displayList = if (isPopularExpanded) list else list.take(4)

                    binding.recyclerViewPopular.layoutManager =
                        GridLayoutManager(this@MainActivity, 2)
                    binding.recyclerViewPopular.adapter = ItemAdapter(displayList.toMutableList())

                    binding.progressBarPopular.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBarPopular.visibility = View.GONE
                }
            })
    }

    // ---------------- MORE COFFEE ----------------
    private fun loadMoreCoffee() {
        binding.progressBarMoreCoffee.visibility = View.VISIBLE

        database.child("items")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ItemModel>()

                    for (child in snapshot.children) {
                        val item = child.getValue(ItemModel::class.java)
                        if (item?.isPopular == 0) {
                            list.add(item)
                        }
                    }

                    val displayList = if (isMoreExpanded) list else list.take(10)

                    binding.recyclerViewMoreCoffee.layoutManager =
                        GridLayoutManager(this@MainActivity, 2)

                    binding.recyclerViewMoreCoffee.adapter =
                        ItemAdapter(displayList.toMutableList())

                    binding.progressBarMoreCoffee.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBarMoreCoffee.visibility = View.GONE
                }
            })
    }
}