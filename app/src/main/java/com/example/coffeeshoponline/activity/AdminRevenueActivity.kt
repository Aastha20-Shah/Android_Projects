package com.example.coffeeshoponline.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.databinding.ActivityAdminRevenueBinding

class AdminRevenueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminRevenueBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRevenueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }
    }
}
