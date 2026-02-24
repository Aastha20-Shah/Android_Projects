package com.example.coffeeshoponline.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeshoponline.Adapter.WishlistAdapter
import com.example.coffeeshoponline.Helper.WishlistManager
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.databinding.ActivityWishListBinding

class WishListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishListBinding
    private lateinit var adapter: WishlistAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.progressBar.visibility = View.VISIBLE
        val wishlistItems = WishlistManager(this).getWishlist()

        if (wishlistItems.isEmpty()) {
            binding.emptyLayout.visibility = View.VISIBLE
            binding.ListView.visibility = View.GONE
        } else {
            binding.emptyLayout.visibility = View.GONE
            binding.ListView.visibility = View.VISIBLE

            adapter = WishlistAdapter(wishlistItems)
            binding.ListView.layoutManager = GridLayoutManager(this, 2)
            binding.ListView.adapter = adapter
        }

        binding.progressBar.visibility = View.GONE
    }

}