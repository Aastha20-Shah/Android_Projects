package com.example.coffeeshoponline.activity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminOrderAdapter
import com.example.coffeeshoponline.databinding.ActivityAdminUserDetailBinding
import com.example.coffeeshoponline.model.OrderModel
import com.example.coffeeshoponline.model.UserModel
import com.google.firebase.database.*

class AdminUserDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUserDetailBinding
    private lateinit var user: UserModel
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getSerializableExtra("user") as UserModel
        database = FirebaseDatabase.getInstance().reference

        setupUI()
        fetchUserOrders()

        binding.backBtn.setOnClickListener { finish() }
        
        binding.editUserBtn.setOnClickListener { showEditUserDialog() }
        
        binding.deleteUserBtn.setOnClickListener { confirmDeleteUser() }
    }

    private fun setupUI() {
        binding.userNameTxt.text = user.name
        binding.userEmailTxt.text = user.email
        binding.userPhoneTxt.text = user.phone
        binding.userAddressTxt.text = formatAddress(user.address)
    }

    private fun formatAddress(address: Any?): String {
        return when (address) {
            is String -> if (address.isEmpty()) "Not specified" else address
            is Map<*, *> -> {
                // If it's a map (structured address), try to join its values
                val parts = address.values.filterIsInstance<String>().filter { it.isNotEmpty() }
                if (parts.isEmpty()) "Not specified" else parts.joinToString(", ")
            }
            else -> "Not specified"
        }
    }

    private fun fetchUserOrders() {
        binding.progressBar.visibility = View.VISIBLE
        val ordersQuery = database.child("orders").orderByChild("userId").equalTo(user.id)
        
        ordersQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    order?.let { orderList.add(it) }
                }

                binding.progressBar.visibility = View.GONE
                binding.orderCountTxt.text = "(${orderList.size} Orders)"

                if (orderList.isEmpty()) {
                    binding.noOrdersTxt.visibility = View.VISIBLE
                    binding.rvUserOrders.visibility = View.GONE
                } else {
                    binding.noOrdersTxt.visibility = View.GONE
                    binding.rvUserOrders.visibility = View.VISIBLE
                    binding.rvUserOrders.layoutManager = LinearLayoutManager(this@AdminUserDetailActivity)
                    binding.rvUserOrders.adapter = AdminOrderAdapter(orderList.reversed(), canUpdateStatus = false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AdminUserDetailActivity, "Failed to load orders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditUserDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        val inputName = EditText(this).apply {
            hint = "Name"
            setText(user.name)
        }
        val inputPhone = EditText(this).apply {
            hint = "Phone Number"
            setText(user.phone)
        }
        val inputAddress = EditText(this).apply {
            hint = "Address"
            val currentAddr = formatAddress(user.address)
            setText(if (currentAddr == "Not specified") "" else currentAddr)
            minLines = 2
            gravity = android.view.Gravity.TOP
        }

        layout.addView(inputName)
        layout.addView(inputPhone)
        layout.addView(inputAddress)

        AlertDialog.Builder(this)
            .setTitle("Edit User Details")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = inputName.text.toString().trim()
                val newPhone = inputPhone.text.toString().trim()
                val newAddress = inputAddress.text.toString().trim()

                if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                    val updates = mapOf(
                        "name" to newName,
                        "phone" to newPhone,
                        "address" to newAddress
                    )
                    database.child("users").child(user.id).updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show()
                            // Update local object and UI
                            user = user.copy(name = newName, phone = newPhone, address = newAddress)
                            setupUI()
                        }
                } else {
                    Toast.makeText(this, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteUser() {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete ${user.name}?")
            .setPositiveButton("Delete") { _, _ ->
                database.child("users").child(user.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
