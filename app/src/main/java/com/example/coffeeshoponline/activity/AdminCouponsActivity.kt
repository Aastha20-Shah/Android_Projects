package com.example.coffeeshoponline.activity

import android.content.Intent
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
        // No alpha effect - keeping original dark color for all
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
        
        binding.adminBottomNav.navMenu.setOnClickListener {
            startActivity(Intent(this, AdminMenuActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        
        binding.adminBottomNav.navCoupons.setOnClickListener {
            // Already here
        }
    }
}
