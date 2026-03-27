package com.example.coffeeshoponline.activity

import android.content.Intent
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
        binding.adminBottomNav.navHome.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            overridePendingTransition(0, 0)
            finish()
        }
        
        binding.adminBottomNav.navOrders.setOnClickListener {
            // Already here
        }
        
        binding.adminBottomNav.navUsers.setOnClickListener {
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        
        binding.adminBottomNav.navMenu.setOnClickListener {
            startActivity(Intent(this, AdminMenuActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        
        binding.adminBottomNav.navCoupons.setOnClickListener {
            startActivity(Intent(this, AdminCouponsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
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
                    orderList.reverse()
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
