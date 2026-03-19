package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.coffeeshoponline.Adapter.CategoryAdapter
import com.example.coffeeshoponline.Adapter.ItemAdapter
import com.example.coffeeshoponline.databinding.ActivityMainBinding
import com.example.coffeeshoponline.databinding.DialogAddAddressBinding
import com.example.coffeeshoponline.model.BannerModel
import com.example.coffeeshoponline.model.CategoryModel
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference

    private lateinit var popularAdapter: ItemAdapter
    private lateinit var moreAdapter: ItemAdapter

    private var allItemsList = mutableListOf<ItemModel>()

    private var isPopularExpanded = false
    private var isMoreExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase
            .getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
            .reference
        loadUserData()
        checkUserAddress()
        popularAdapter = ItemAdapter(mutableListOf())
        moreAdapter = ItemAdapter(mutableListOf())

        // 🔥 FIX 2: Set adapters IMMEDIATELY (prevents "No adapter attached")
        binding.recyclerViewPopular.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = popularAdapter
            isNestedScrollingEnabled = false
        }
        binding.recyclerViewMoreCoffee.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = moreAdapter
            isNestedScrollingEnabled = false
        }
        setupRecyclerViews()
        setupSearch()
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

        binding.nestedScrollView.isSmoothScrollingEnabled = true
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.whishList.setOnClickListener {
            startActivity(Intent(this, WishListActivity::class.java))
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.myOrder.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

    }
    private fun checkUserAddress() {

        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        database.child("users")
            .child(uid)
            .child("address")
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {
                    showAddressPopup()
                }
            }
    }
    private fun showAddressPopup() {
        // 1. Inflate the dialog layout using its specific binding class
        val dialogBinding = DialogAddAddressBinding.inflate(layoutInflater)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        dialogBinding.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.saveAddressBtn.setOnClickListener {

            val house = dialogBinding.etHouse.text.toString().trim()
            val area = dialogBinding.etArea.text.toString().trim()
            val landmark = dialogBinding.etLandmark.text.toString().trim()
            val city = dialogBinding.etCity.text.toString().trim()
            val state = dialogBinding.etState.text.toString().trim()
            val pincode = dialogBinding.etPincode.text.toString().trim()
            val country = dialogBinding.etCountry.text.toString().trim()

            if (house.isEmpty() || city.isEmpty() || pincode.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val uid = user.uid

            val addressMap = HashMap<String, Any>()

            addressMap["house"] = house
            addressMap["area"] = area
            addressMap["landmark"] = landmark
            addressMap["city"] = city
            addressMap["state"] = state
            addressMap["pincode"] = pincode
            addressMap["country"] = country

            FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(uid)
                .child("address")
                .setValue(addressMap)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Address saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Failed to save address",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    private fun setupRecyclerViews() {
        popularAdapter = ItemAdapter(mutableListOf())
        moreAdapter = ItemAdapter(mutableListOf())

        binding.recyclerViewPopular.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = popularAdapter
            isNestedScrollingEnabled = false
        }
        binding.recyclerViewMoreCoffee.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = moreAdapter
            isNestedScrollingEnabled = false
        }
    }

    // ---------------- SEARCH LOGIC ----------------
    private fun setupSearch() {
        binding.editTextText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase(Locale.ROOT)
                filterItems(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterItems(query: String) {
        if (query.isEmpty()) {
            // Show everything again if search is empty
            binding.banner.visibility = View.VISIBLE
            binding.textView3.visibility = View.VISIBLE
            binding.categoryView.visibility = View.VISIBLE
            binding.recyclerViewPopular.visibility = View.VISIBLE
            // Reset "More Coffee" title
            binding.seeAllMore.parent.let { (it as View).visibility = View.VISIBLE }

            loadMoreCoffee() // Restore original list
        } else {
            // Hide other sections to show search results clearly
            binding.banner.visibility = View.GONE
            binding.textView3.visibility = View.GONE
            binding.categoryView.visibility = View.GONE
            binding.recyclerViewPopular.visibility = View.GONE

            val filteredList = allItemsList.filter {
                it.title?.lowercase(Locale.ROOT)?.contains(query) == true ||
                        it.description?.lowercase(Locale.ROOT)?.contains(query) == true
            }

            moreAdapter.apply {
                items.clear()
                items.addAll(filteredList)
                notifyDataSetChanged()
            }
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
            .orderByChild("isPopular")
            .equalTo(1.0) // 🔥 MUST BE DOUBLE
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ItemModel>()

                    for (child in snapshot.children) {
                        child.getValue(ItemModel::class.java)?.let {
                            list.add(it)
                            // Add to global list for search if not already there
                            if (!allItemsList.contains(it)) allItemsList.add(it)
                        }
                    }

                    val displayList =
                        if (isPopularExpanded) list else list.take(6)

                    popularAdapter.apply {
                        items.clear()
                        items.addAll(displayList)
                        notifyDataSetChanged()
                    }

                    binding.progressBarPopular.visibility = View.GONE

                    if (isPopularExpanded) {
                        scrollToPosition(binding.recyclerViewPopular, 6)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBarPopular.visibility = View.GONE
                }
            })
    }

    private fun loadMoreCoffee() {
        binding.progressBarMoreCoffee.visibility = View.VISIBLE

        database.child("items")
            .orderByChild("isPopular")
            .equalTo(0.0) // 🔥 MUST BE DOUBLE
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ItemModel>()

                    for (child in snapshot.children) {
                        child.getValue(ItemModel::class.java)?.let {
                            list.add(it)
                            // Add to global list for search if not already there
                            if (!allItemsList.contains(it)) allItemsList.add(it)
                        }
                    }

                    val displayList =
                        if (isMoreExpanded) list else list.take(10)

                    moreAdapter.apply {
                        items.clear()
                        items.addAll(displayList)
                        notifyDataSetChanged()
                    }

                    binding.progressBarMoreCoffee.visibility = View.GONE

                    if (isMoreExpanded) {
                        scrollToPosition(binding.recyclerViewMoreCoffee, 10)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBarMoreCoffee.visibility = View.GONE
                }
            })
    }

    private fun scrollToPosition(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        position: Int
    ) {
        binding.nestedScrollView.postDelayed({
            // 1. Get the view of the specific item
            val itemView = recyclerView.layoutManager?.findViewByPosition(position)

            if (itemView != null) {
                val rect = android.graphics.Rect()
                itemView.getDrawingRect(rect)

                // 2. Calculate coordinates relative to the NestedScrollView
                binding.nestedScrollView.offsetDescendantRectToMyCoords(itemView, rect)

                // 3. Smooth scroll so that specific item is at the top of the screen
                binding.nestedScrollView.smoothScrollTo(0, rect.top)
            } else {
                // Fallback: If view isn't ready, scroll to the bottom of the RecyclerView's current visible area
                binding.nestedScrollView.smoothScrollTo(0, recyclerView.bottom)
            }
        }, 100) // Small delay to ensure RecyclerView has bound the new items
    }
    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid

            // Reference to users -> uid -> name
            database.child("users").child(uid).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val name = snapshot.getValue(String::class.java)
                            binding.textView2.text = name
                        } else {
                            // Fallback if name isn't set in DB
                            binding.textView2.text = "Coffee Lover"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.textView2.text = "Welcome!"
                    }
                })
        }
    }
}