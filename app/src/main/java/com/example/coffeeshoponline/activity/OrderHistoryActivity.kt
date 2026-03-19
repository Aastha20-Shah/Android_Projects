package com.example.coffeeshoponline.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.OrderAdapter
import com.example.coffeeshoponline.R
import com.example.coffeeshoponline.databinding.ActivityOrderHistoryBinding
import com.example.coffeeshoponline.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderHistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageView9.setOnClickListener { finish() }
        binding.Backbtn.setOnClickListener { finish() }

        initOrderHistory()
    }

    private fun initOrderHistory() {
        val database = FirebaseDatabase.getInstance().getReference("orders")
        binding.orderProgressBar.visibility = View.VISIBLE

        // Use your specific UserID from the database
        val query = database.orderByChild("userId").equalTo("UgdlANOAyTbifCTFUgmNJehqOxG3")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }

                if (orderList.isNotEmpty()) {
                    orderList.reverse() // Show newest orders first
                    binding.orderRecyclerView.apply {
                        layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
                        adapter = OrderAdapter(orderList)
                    }
                }
                binding.orderProgressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.orderProgressBar.visibility = View.GONE
            }
        })
    }
}
