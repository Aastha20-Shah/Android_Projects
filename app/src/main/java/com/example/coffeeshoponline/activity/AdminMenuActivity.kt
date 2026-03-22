package com.example.coffeeshoponline.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.databinding.ActivityAdminMenuBinding

class AdminMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.adminBottomNav.selectedItemId = com.example.coffeeshoponline.R.id.nav_menu
        
        binding.adminBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.coffeeshoponline.R.id.nav_menu -> return@setOnItemSelectedListener true
                com.example.coffeeshoponline.R.id.nav_home -> {
                    startActivity(android.content.Intent(this, AdminDashboardActivity::class.java).apply { flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP })
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_orders -> {
                    startActivity(android.content.Intent(this, AdminManageOrdersActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                com.example.coffeeshoponline.R.id.nav_users -> {
                    startActivity(android.content.Intent(this, AdminManageUsersActivity::class.java))
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
}
