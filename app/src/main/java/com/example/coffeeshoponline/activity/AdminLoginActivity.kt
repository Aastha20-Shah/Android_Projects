package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.databinding.ActivityAdminLoginBinding

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, java.lang.Math.max(systemBars.bottom, ime.bottom))
            insets
        }

        binding.btnAdminLogin.setOnClickListener {
            val username = binding.etAdminUsername.text.toString().trim()
            val password = binding.etAdminPassword.text.toString().trim()

            if (username == "Admin" && password == "admin123") {
                Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBackToUserLogin.setOnClickListener {
            finish()
        }
    }
}
