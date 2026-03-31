package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.OrderAdapter
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
        
        binding.startShoppingBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }

        initOrderHistory()
    }

    private fun initOrderHistory() {

        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user == null) {
            return
        }
        val database = FirebaseDatabase.getInstance().getReference("orders")
        binding.orderProgressBar.visibility = View.VISIBLE

        val query = database.orderByChild("userId").equalTo(user.uid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }

                binding.orderProgressBar.visibility = View.GONE

                if (orderList.isNotEmpty()) {
                    binding.emptyLayout.visibility = View.GONE
                    binding.orderRecyclerView.visibility = View.VISIBLE
                    orderList.reverse() 
                    binding.orderRecyclerView.apply {
                        layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
                        adapter = OrderAdapter(orderList)
                    }
                } else {
                    binding.emptyLayout.visibility = View.VISIBLE
                    binding.orderRecyclerView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.orderProgressBar.visibility = View.GONE
            }
        })
    }
}
