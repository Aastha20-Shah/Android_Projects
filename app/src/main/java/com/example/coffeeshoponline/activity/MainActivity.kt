package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
    private val suggestionsList = mutableListOf<String>()
    private lateinit var suggestionsAdapter: ArrayAdapter<String>

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
        if (intent.getBooleanExtra("showAddressPopup", false)) {
            checkUserAddress()
        }
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
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                startActivity(Intent(this, CartActivity::class.java))
            } else {
                showLoginRequiredDialog("view your cart")
            }
        }

        binding.whishList.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                startActivity(Intent(this, WishListActivity::class.java))
            } else {
                showLoginRequiredDialog("view your wishlist")
            }
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.myOrder.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                startActivity(Intent(this, OrderHistoryActivity::class.java))
            } else {
                showLoginRequiredDialog("view your order history")
            }
        }

    }

    private fun showLoginRequiredDialog(action: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Login Required")
        builder.setMessage("You must be logged in to $action.")
        builder.setPositiveButton("Login") { _, _ ->
            startActivity(Intent(this, LoginActivity::class.java))
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
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
        val dialogBinding = DialogAddAddressBinding.inflate(layoutInflater)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

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

    private fun setupSearch() {
        suggestionsAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestionsList)
        (binding.editTextText as? AutoCompleteTextView)?.apply {
            setAdapter(suggestionsAdapter)
            threshold = 2
            setOnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                filterItems(selectedItem.lowercase(Locale.ROOT))
            }
        }

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
            binding.banner.visibility = View.VISIBLE
            binding.textView3.visibility = View.VISIBLE
            binding.categoryView.visibility = View.VISIBLE
            binding.recyclerViewPopular.visibility = View.VISIBLE
            binding.textViewPopular.visibility = View.VISIBLE
            binding.seeAllPopular.visibility = View.VISIBLE
            binding.seeAllMore.parent.let { (it as View).visibility = View.VISIBLE }

            loadMoreCoffee() 
        } else {
            binding.banner.visibility = View.GONE
            binding.textView3.visibility = View.GONE
            binding.categoryView.visibility = View.GONE
            binding.recyclerViewPopular.visibility = View.GONE
            binding.textViewPopular.visibility = View.GONE
            binding.seeAllPopular.visibility = View.GONE

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


    private fun loadPopularItems() {
        binding.progressBarPopular.visibility = View.VISIBLE

        database.child("items")
            .orderByChild("isPopular")
            .equalTo(1.0) 
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ItemModel>()

                    for (child in snapshot.children) {
                        child.getValue(ItemModel::class.java)?.let {
                            list.add(it)
                            if (!allItemsList.any { item -> item.id == it.id }) {
                                allItemsList.add(it)
                                it.title?.let { title -> 
                                    if (!suggestionsList.contains(title)) {
                                        suggestionsList.add(title)
                                    }
                                }
                            }
                        }
                    }
                    suggestionsAdapter.notifyDataSetChanged()

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
            .equalTo(0.0) 
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ItemModel>()

                    for (child in snapshot.children) {
                        child.getValue(ItemModel::class.java)?.let {
                            list.add(it)
                            if (!allItemsList.any { item -> item.id == it.id }) {
                                allItemsList.add(it)
                                it.title?.let { title -> 
                                    if (!suggestionsList.contains(title)) {
                                        suggestionsList.add(title)
                                    }
                                }
                            }
                        }
                    }
                    suggestionsAdapter.notifyDataSetChanged()

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
            val itemView = recyclerView.layoutManager?.findViewByPosition(position)

            if (itemView != null) {
                val rect = android.graphics.Rect()
                itemView.getDrawingRect(rect)

                binding.nestedScrollView.offsetDescendantRectToMyCoords(itemView, rect)

                binding.nestedScrollView.smoothScrollTo(0, rect.top)
            } else {
                binding.nestedScrollView.smoothScrollTo(0, recyclerView.bottom)
            }
        }, 100) 
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid

            database.child("users").child(uid).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val name = snapshot.getValue(String::class.java)
                            binding.textView2.text = name
                        } else {
                            binding.textView2.text = "Coffee Lover"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.textView2.text = "Welcome!"
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
    }

    private fun updateCartBadge() {
        val cartHelper = com.example.coffeeshoponline.Helper.ManagmentCart(this)
        val cartList = cartHelper.getListCart()
        val count = cartList.sumOf { it.numberInCart }
        
        val badge = findViewById<android.widget.TextView>(com.example.coffeeshoponline.R.id.cartBadge)
        if (badge != null) {
            if (count > 0) {
                badge.visibility = android.view.View.VISIBLE
                badge.text = count.toString()
            } else {
                badge.visibility = android.view.View.GONE
            }
        }
    }
}