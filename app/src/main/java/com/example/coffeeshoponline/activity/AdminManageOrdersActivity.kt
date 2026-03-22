package com.example.coffeeshoponline.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminOrderAdapter
import com.example.coffeeshoponline.databinding.ActivityAdminManageOrdersBinding
import com.example.coffeeshoponline.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminManageOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManageOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManageOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtnAdmin.setOnClickListener { finish() }

        setupBottomNavigation()
        fetchAllOrders()
    }

    private fun setupBottomNavigation() {
        binding.adminBottomNav.selectedItemId = com.example.coffeeshoponline.R.id.nav_orders
        
        binding.adminBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.coffeeshoponline.R.id.nav_orders -> return@setOnItemSelectedListener true
                com.example.coffeeshoponline.R.id.nav_home -> {
                    startActivity(android.content.Intent(this, AdminDashboardActivity::class.java).apply { flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP })
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_users -> {
                    startActivity(android.content.Intent(this, AdminManageUsersActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_menu -> {
                    startActivity(android.content.Intent(this, AdminMenuActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_coupons -> {
                    startActivity(android.content.Intent(this, AdminCouponsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            true
        }
    }

    private fun fetchAllOrders() {
        binding.progressBarAdmin.visibility = View.VISIBLE
        
        val database = FirebaseDatabase.getInstance().getReference("orders")
        
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }

                if (orderList.isNotEmpty()) {
                    orderList.reverse() // Newest orders first
                    binding.rvAdminOrders.apply {
                        layoutManager = LinearLayoutManager(this@AdminManageOrdersActivity)
                        adapter = AdminOrderAdapter(orderList)
                    }
                }
                binding.progressBarAdmin.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarAdmin.visibility = View.GONE
            }
        })
    }
}
