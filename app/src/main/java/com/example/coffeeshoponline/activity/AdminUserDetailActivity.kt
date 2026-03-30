package com.example.coffeeshoponline.activity

import android.os.Build
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

        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("user", UserModel::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("user") as UserModel
        }
        
        database = FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/").reference

        setupUI()
        fetchUserOrders()
        setupRatingBar()

        binding.backBtn.setOnClickListener { finish() }
        
        binding.editUserBtn.setOnClickListener { showEditUserDialog() }
        
        binding.deleteUserBtn.setOnClickListener { confirmDeleteUser() }
    }

    private fun setupUI() {
        binding.userNameTxt.text = user.name
        binding.userEmailTxt.text = user.email
        binding.userPhoneTxt.text = user.phone
        binding.userAddressTxt.text = formatAddress(user.address)
        binding.userRatingBar.rating = user.userRating
    }

    private fun setupRatingBar() {
        binding.userRatingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                database.child("users").child(user.id).child("userRating").setValue(rating)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User evaluation updated", Toast.LENGTH_SHORT).show()
                        user = user.copy(userRating = rating)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update evaluation", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun formatAddress(address: Any?): String {
        return when (address) {
            is String -> if (address.isEmpty()) "Not specified" else address
            is Map<*, *> -> {
                val house = address["house"]?.toString() ?: ""
                val area = address["area"]?.toString() ?: ""
                val landmark = address["landmark"]?.toString() ?: ""
                val city = address["city"]?.toString() ?: ""
                val state = address["state"]?.toString() ?: ""
                val pincode = address["pincode"]?.toString() ?: ""
                val country = address["country"]?.toString() ?: ""

                val parts = mutableListOf<String>()
                if (house.isNotEmpty()) parts.add(house)
                if (area.isNotEmpty()) parts.add(area)
                if (landmark.isNotEmpty()) parts.add(landmark)
                if (city.isNotEmpty()) parts.add(city)
                if (state.isNotEmpty()) parts.add(state)
                if (pincode.isNotEmpty()) parts.add(pincode)
                if (country.isNotEmpty()) parts.add(country)

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
                    binding.rvUserOrders.adapter = AdminOrderAdapter(orderList.reversed())
                }
                
                binding.userDetailScrollView.post {
                    binding.userDetailScrollView.fullScroll(View.FOCUS_UP)
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
        
        val currentAddr = formatAddress(user.address)
        val inputAddress = EditText(this).apply {
            hint = "Address"
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
                    val updates = mutableMapOf<String, Any>(
                        "name" to newName,
                        "phone" to newPhone,
                        "address" to newAddress 
                    )
                    
                    database.child("users").child(user.id).updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show()
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