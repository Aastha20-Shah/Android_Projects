package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.databinding.ActivityAdminDashboardBinding
import com.example.coffeeshoponline.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNavigation()
        loadDashboardStats()
    }
    
    private fun setupBottomNavigation() {
        binding.adminBottomNav.selectedItemId = com.example.coffeeshoponline.R.id.nav_home
        
        binding.adminBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.coffeeshoponline.R.id.nav_home -> return@setOnItemSelectedListener true
                com.example.coffeeshoponline.R.id.nav_orders -> {
                    startActivity(Intent(this, AdminManageOrdersActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_users -> {
                    startActivity(Intent(this, AdminManageUsersActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_menu -> {
                    startActivity(Intent(this, AdminMenuActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_coupons -> {
                    startActivity(Intent(this, AdminCouponsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            true
        }
    }
    
    private fun loadDashboardStats() {
        // Load User Count
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount
                binding.tvTotalUsersCount.text = userCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        
        // Load Order Stats (Total Orders, Pending Orders, Total Revenue)
        FirebaseDatabase.getInstance().getReference("orders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRevenue = 0.0
                var pendingCount = 0
                val totalOrders = snapshot.childrenCount
                
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        totalRevenue += order.totalAmount.toDouble()
                        if (order.status.equals("Pending", ignoreCase = true)) {
                            pendingCount++
                        }
                    }
                }
                
                binding.tvTotalOrders.text = totalOrders.toString()
                binding.tvPendingOrders.text = pendingCount.toString()
                binding.tvTotalRevenue.text = "₹%.0f".format(totalRevenue)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
