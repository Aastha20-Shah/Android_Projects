package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminItemAdapter
import com.example.coffeeshoponline.databinding.ActivityAdminMenuBinding
import com.example.coffeeshoponline.databinding.DialogAddEditItemBinding
import com.example.coffeeshoponline.model.CategoryModel
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.database.*

class AdminMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMenuBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: AdminItemAdapter
    private val itemList = mutableListOf<ItemModel>()
    private val categoryList = mutableListOf<CategoryModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Using the specific database URL provided by the user
        database = FirebaseDatabase
            .getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
            .reference
        
        setupRecyclerView()
        setupBottomNavigation()
        loadItems()
        loadCategories()

        binding.backBtn.setOnClickListener { finish() }
        
        binding.fabAddItem.setOnClickListener {
            showAddEditDialog(null)
        }

        binding.addCategoryBtn.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminItemAdapter(itemList, 
            onEditClick = { item -> showAddEditDialog(item) },
            onDeleteClick = { item -> confirmDelete(item) }
        )
        binding.rvAdminItems.layoutManager = LinearLayoutManager(this)
        binding.rvAdminItems.adapter = adapter
    }

    private fun loadItems() {
        binding.progressBarMenu.visibility = View.VISIBLE
        database.child("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (child in snapshot.children) {
                    val item = child.getValue(ItemModel::class.java)
                    item?.let { itemList.add(it) }
                }
                adapter.notifyDataSetChanged()
                binding.progressBarMenu.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarMenu.visibility = View.GONE
                Toast.makeText(this@AdminMenuActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCategories() {
        database.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (child in snapshot.children) {
                    val category = child.getValue(CategoryModel::class.java)
                    category?.let { categoryList.add(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showAddEditDialog(item: ItemModel?) {
        val dialogBinding = DialogAddEditItemBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
        val dialog = builder.create()

        dialogBinding.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        // Setup Category Spinner
        val categoryTitles = categoryList.map { it.title }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryTitles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = spinnerAdapter

        if (item != null) {
            dialogBinding.dialogTitle.text = "Update Product"
            dialogBinding.etTitle.setText(item.title)
            dialogBinding.etDescription.setText(item.description)
            dialogBinding.etPriceSmall.setText(item.priceSmall.toString())
            dialogBinding.etPriceMedium.setText(item.priceMedium.toString())
            dialogBinding.etPriceLarge.setText(item.priceLarge.toString())
            dialogBinding.etPicUrl.setText(item.picUrl.joinToString(","))
            dialogBinding.cbIsPopular.isChecked = item.isPopular == 1L
            
            // Note: categoryId in model is Any?, we try to match it as Int or Double/Long
            val catIdValue = (item.categoryId as? Number)?.toInt() ?: 0
            val catIndex = categoryList.indexOfFirst { it.id == catIdValue }
            if (catIndex != -1) dialogBinding.spinnerCategory.setSelection(catIndex)
        }

        dialogBinding.saveBtn.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val desc = dialogBinding.etDescription.text.toString().trim()
            val pS = dialogBinding.etPriceSmall.text.toString().toDoubleOrNull() ?: 0.0
            val pM = dialogBinding.etPriceMedium.text.toString().toDoubleOrNull() ?: 0.0
            val pL = dialogBinding.etPriceLarge.text.toString().toDoubleOrNull() ?: 0.0
            val pics = dialogBinding.etPicUrl.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val isPop = if (dialogBinding.cbIsPopular.isChecked) 1L else 0L
            
            val selectedCatIndex = dialogBinding.spinnerCategory.selectedItemPosition
            val catId = if (selectedCatIndex != -1 && categoryList.isNotEmpty()) {
                categoryList[selectedCatIndex].id
            } else 0

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If updating, keep old ID, else generate new one
            val id = item?.id ?: ((itemList.maxOfOrNull { it.id } ?: 0) + 1)
            
            val newItem = ItemModel(
                id = id,
                title = title,
                description = desc,
                priceSmall = pS,
                priceMedium = pM,
                priceLarge = pL,
                picUrl = pics,
                isPopular = isPop,
                categoryId = catId
            )

            database.child("items").child(id.toString()).setValue(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show()
                }
        }
        dialog.show()
    }

    private fun confirmDelete(item: ItemModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete '${item.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                database.child("items").child(item.id.toString()).removeValue()
                    .addOnSuccessListener { 
                        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show() 
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddCategoryDialog() {
        val et = android.widget.EditText(this)
        et.hint = "Category Name"
        et.setPadding(50, 40, 50, 40)
        
        AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(et)
            .setPositiveButton("Add") { _, _ ->
                val title = et.text.toString().trim()
                if (title.isNotEmpty()) {
                    val id = (categoryList.maxOfOrNull { it.id } ?: 0) + 1
                    val newCat = CategoryModel(id, title)
                    database.child("categories").child(id.toString()).setValue(newCat)
                        .addOnSuccessListener { 
                            Toast.makeText(this, "Category '$title' added", Toast.LENGTH_SHORT).show()
                            loadCategories() // Refresh local list
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.adminBottomNav.navHome.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            overridePendingTransition(0, 0)
            finish()
        }
        binding.adminBottomNav.navOrders.setOnClickListener {
            startActivity(Intent(this, AdminManageOrdersActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.adminBottomNav.navUsers.setOnClickListener {
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.adminBottomNav.navCoupons.setOnClickListener {
            startActivity(Intent(this, AdminCouponsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}