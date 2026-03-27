package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminUserAdapter
import com.example.coffeeshoponline.databinding.ActivityAdminManageUsersBinding
import com.example.coffeeshoponline.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminManageUsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManageUsersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtnUserAdmin.setOnClickListener { finish() }

        setupBottomNavigation()
        fetchAllUsers()
    }

    private fun setupBottomNavigation() {
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
            // Already here
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

    private fun fetchAllUsers() {
        binding.progressBarAdminUser.visibility = View.VISIBLE
        
        val database = FirebaseDatabase.getInstance().getReference("users")
        
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<UserModel>()
                for (child in snapshot.children) {
                    val user = child.getValue(UserModel::class.java)
                    if (user != null) {
                        user.id = child.key ?: ""
                        userList.add(user)
                    }
                }

                if (userList.isNotEmpty()) {
                    binding.rvAdminUsers.apply {
                        layoutManager = LinearLayoutManager(this@AdminManageUsersActivity)
                        adapter = AdminUserAdapter(userList)
                    }
                }
                binding.progressBarAdminUser.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarAdminUser.visibility = View.GONE
            }
        })
    }
}
