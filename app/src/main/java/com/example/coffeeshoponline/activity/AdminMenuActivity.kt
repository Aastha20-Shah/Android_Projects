package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminCategoryAdapter
import com.example.coffeeshoponline.Adapter.AdminItemAdapter
import com.example.coffeeshoponline.databinding.ActivityAdminMenuBinding
import com.example.coffeeshoponline.databinding.DialogAddEditItemBinding
import com.example.coffeeshoponline.databinding.DialogManageCategoriesBinding
import com.example.coffeeshoponline.model.CategoryModel
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.database.*

class AdminMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMenuBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: AdminItemAdapter
    private val itemList = mutableListOf<ItemModel>()
    private val categoryList = mutableListOf<CategoryModel>()
    
    // Track the category adapter to notify it when data changes
    private var categoryAdapter: AdminCategoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            showManageCategoriesDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminItemAdapter(itemList, 
            onEditClick = { item -> showAddEditDialog(item) },
            onDeleteClick = { item -> confirmDeleteItem(item) }
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
            }
        })
    }

    private fun loadCategories() {
        database.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (child in snapshot.children) {
                    val category = child.getValue(CategoryModel::class.java)
                    category?.let { categoryList.add(it) }
                }
                // Notify the category adapter if the dialog is open
                categoryAdapter?.notifyDataSetChanged()
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

        dialogBinding.closeBtn.setOnClickListener { dialog.dismiss() }

        val categoryTitles = categoryList.map { it.title }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryTitles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = spinnerAdapter

        if (item != null) {
            dialogBinding.dialogTitle.text = "Update Product"
            dialogBinding.etTitle.setText(item.title)
            dialogBinding.etDescription.setText(item.description)
            dialogBinding.etExtra.setText(item.extra)
            dialogBinding.etPriceSmall.setText(item.priceSmall.toString())
            dialogBinding.etPriceMedium.setText(item.priceMedium.toString())
            dialogBinding.etPriceLarge.setText(item.priceLarge.toString())
            dialogBinding.etRating.setText(item.rating.toString())
            dialogBinding.etPicUrl.setText(item.picUrl.joinToString(","))
            dialogBinding.cbIsPopular.isChecked = item.isPopular == 1L
            
            val catIdValue = (item.categoryId as? Number)?.toInt() ?: 0
            val catIndex = categoryList.indexOfFirst { it.id == catIdValue }
            if (catIndex != -1) dialogBinding.spinnerCategory.setSelection(catIndex)
        }

        dialogBinding.saveBtn.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val desc = dialogBinding.etDescription.text.toString().trim()
            val extra = dialogBinding.etExtra.text.toString().trim()
            val pS = dialogBinding.etPriceSmall.text.toString().toDoubleOrNull() ?: 0.0
            val pM = dialogBinding.etPriceMedium.text.toString().toDoubleOrNull() ?: 0.0
            val pL = dialogBinding.etPriceLarge.text.toString().toDoubleOrNull() ?: 0.0
            val rating = dialogBinding.etRating.text.toString().toDoubleOrNull() ?: 0.0
            val picsStr = dialogBinding.etPicUrl.text.toString()
            val pics = picsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val isPop = if (dialogBinding.cbIsPopular.isChecked) 1L else 0L
            
            val selectedCatIndex = dialogBinding.spinnerCategory.selectedItemPosition
            val catId = if (selectedCatIndex != -1 && categoryList.isNotEmpty()) {
                categoryList[selectedCatIndex].id
            } else 0

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (pics.isEmpty()) {
                Toast.makeText(this, "At least one Picture URL is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = item?.id ?: ((itemList.maxOfOrNull { it.id } ?: 0) + 1)
            
            // Use named arguments to ensure correct mapping to ItemModel
            val newItem = ItemModel(
                id = id,
                title = title,
                description = desc,
                extra = extra,
                picUrl = pics,
                priceSmall = pS,
                priceMedium = pM,
                priceLarge = pL,
                rating = rating,
                isPopular = isPop,
                categoryId = catId
            )

            database.child("items").child(id.toString()).setValue(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
        }
        
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.show()
    }

    private fun showManageCategoriesDialog() {
        val dialogBinding = DialogManageCategoriesBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        categoryAdapter = AdminCategoryAdapter(categoryList, 
            onEditClick = { category -> showEditCategoryDialog(category) },
            onDeleteClick = { category -> confirmDeleteCategory(category) }
        )
        dialogBinding.rvCategories.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvCategories.adapter = categoryAdapter

        dialogBinding.addCategoryBtn.setOnClickListener {
            val title = dialogBinding.etNewCategory.text.toString().trim()
            if (title.isNotEmpty()) {
                val id = (categoryList.maxOfOrNull { it.id } ?: 0) + 1
                database.child("categories").child(id.toString()).setValue(CategoryModel(id, title))
                    .addOnSuccessListener {
                        dialogBinding.etNewCategory.setText("")
                    }
            }
        }

        dialogBinding.closeCategoryBtn.setOnClickListener { dialog.dismiss() }
        
        dialog.setOnDismissListener {
            categoryAdapter = null // Prevent crash by removing reference
        }
        
        dialog.show()
    }

    private fun showEditCategoryDialog(category: CategoryModel) {
        val et = EditText(this)
        et.setText(category.title)
        et.setPadding(60, 40, 60, 40)
        AlertDialog.Builder(this).setTitle("Edit Category").setView(et)
            .setPositiveButton("Update") { _, _ ->
                val newTitle = et.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    database.child("categories").child(category.id.toString()).child("title").setValue(newTitle)
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun confirmDeleteCategory(category: CategoryModel) {
        AlertDialog.Builder(this).setTitle("Delete Category")
            .setMessage("Delete '${category.title}'? Products in this category will remain but without a category reference.")
            .setPositiveButton("Delete") { _, _ ->
                database.child("categories").child(category.id.toString()).removeValue()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun confirmDeleteItem(item: ItemModel) {
        AlertDialog.Builder(this).setTitle("Delete Product").setMessage("Delete '${item.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                database.child("items").child(item.id.toString()).removeValue()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun setupBottomNavigation() {
        binding.adminBottomNav.navHome.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            finish()
        }
        binding.adminBottomNav.navOrders.setOnClickListener {
            startActivity(Intent(this, AdminManageOrdersActivity::class.java))
            finish()
        }
        binding.adminBottomNav.navUsers.setOnClickListener {
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            finish()
        }
        binding.adminBottomNav.navCoupons.setOnClickListener {
            startActivity(Intent(this, AdminCouponsActivity::class.java))
            finish()
        }
    }
}