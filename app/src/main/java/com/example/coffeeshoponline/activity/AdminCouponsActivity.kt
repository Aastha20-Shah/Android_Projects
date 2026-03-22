package com.example.coffeeshoponline.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.databinding.ActivityAdminCouponsBinding

class AdminCouponsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCouponsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCouponsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.adminBottomNav.selectedItemId = com.example.coffeeshoponline.R.id.nav_coupons
        
        binding.adminBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.coffeeshoponline.R.id.nav_coupons -> return@setOnItemSelectedListener true
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
                com.example.coffeeshoponline.R.id.nav_menu -> {
                    startActivity(android.content.Intent(this, AdminMenuActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            true
        }
    }
}
