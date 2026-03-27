package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.OrderAdapter
import com.example.coffeeshoponline.databinding.ActivityAdminDashboardBinding
import com.example.coffeeshoponline.model.OrderModel
import com.google.firebase.auth.FirebaseAuth
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
        setupRecentOrders()
        setupLogout()
    }
    
    private fun setupLogout() {
        binding.btnSettings.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, AdminLoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
    
    private fun setupBottomNavigation() {
        binding.adminBottomNav.navHome.setOnClickListener {
            // Already here
        }
        
        binding.adminBottomNav.navOrders.setOnClickListener {
            startActivity(Intent(this, AdminManageOrdersActivity::class.java))
            overridePendingTransition(0, 0)
        }
        
        binding.adminBottomNav.navUsers.setOnClickListener {
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            overridePendingTransition(0, 0)
        }
        
        binding.adminBottomNav.navMenu.setOnClickListener {
            startActivity(Intent(this, AdminMenuActivity::class.java))
            overridePendingTransition(0, 0)
        }
        
        binding.adminBottomNav.navCoupons.setOnClickListener {
            startActivity(Intent(this, AdminCouponsActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }
    
    private fun loadDashboardStats() {
        val database = FirebaseDatabase.getInstance()
        
        // 1. Total Products
        database.getReference("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount
                binding.tvTotalProductsCount.text = count.toString()
                binding.progressProducts.progress = (count * 100 / 500).toInt().coerceIn(0, 100)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Total Users
        database.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount
                binding.tvTotalUsersCount.text = userCount.toString()
                binding.progressClients.progress = (userCount * 100 / 1000).toInt().coerceIn(0, 100)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        
        // 3. Orders and Revenue
        database.getReference("orders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRevenue = 0.0
                val totalOrders = snapshot.childrenCount
                
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        totalRevenue += order.totalAmount
                    }
                }
                
                binding.tvTotalOrders.text = totalOrders.toString()
                binding.tvTotalRevenue.text = "₹%.0f".format(totalRevenue)
                
                binding.progressOrders.progress = (totalOrders * 100 / 2000).toInt().coerceIn(0, 100)
                binding.progressRevenue.progress = (totalRevenue * 100 / 100000).toInt().coerceIn(0, 100)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupRecentOrders() {
        binding.rvRecentOrders.layoutManager = LinearLayoutManager(this)
        
        FirebaseDatabase.getInstance().getReference("orders")
            .limitToLast(10)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<OrderModel>()
                    for (child in snapshot.children) {
                        child.getValue(OrderModel::class.java)?.let { list.add(it) }
                    }
                    list.reverse()
                    binding.rvRecentOrders.adapter = OrderAdapter(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
