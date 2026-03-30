package com.example.coffeeshoponline.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminOrderItemAdapter
import com.example.coffeeshoponline.databinding.ActivityAdminOrderDetailsBinding
import com.example.coffeeshoponline.model.OrderModel
import com.google.firebase.database.FirebaseDatabase

class AdminOrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminOrderDetailsBinding
    private var order: OrderModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("order", OrderModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("order") as? OrderModel
        }

        if (order == null) {
            Toast.makeText(this, "Order data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupDetails()
        setupItemsList()
        
        binding.backBtn.setOnClickListener { finish() }
        
        binding.btnMarkStatus.setOnClickListener {
            updateOrderStatus()
        }
    }

    private fun setupDetails() {
        order?.let {
            binding.tvDetailOrderId.text = "Order #${it.orderId}"
            binding.tvDetailDate.text = "Date: ${it.getFormattedDate()}"
            binding.tvDetailUserName.text = "Name: ${it.userName}"
            binding.tvDetailAddress.text = "Address: ${it.address}"
            binding.tvDetailPaymentMethod.text = it.paymentMethod
            binding.tvDetailTotal.text = "₹%.2f".format(it.totalAmount.toDouble())
            binding.tvDetailStatus.text = it.status

            if (it.status.equals("Success", ignoreCase = true)) {
                binding.tvDetailStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"))
                binding.btnMarkStatus.visibility = View.GONE
            } else {
                binding.tvDetailStatus.setTextColor(android.graphics.Color.parseColor("#E65100"))
                binding.btnMarkStatus.visibility = View.VISIBLE
                binding.btnMarkStatus.text = "Mark as Delivered"
            }
        }
    }

    private fun setupItemsList() {
        order?.items?.let { items ->
            binding.rvOrderItems.layoutManager = LinearLayoutManager(this)
            binding.rvOrderItems.adapter = AdminOrderItemAdapter(items)
        }
    }

    private fun updateOrderStatus() {
        order?.let {
            val dbRef = FirebaseDatabase.getInstance().reference.child("orders").child(it.orderId)
            
            val updates = HashMap<String, Any>()
            updates["status"] = "Success"
            updates["paymentStatus"] = "Paid" 

            dbRef.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(this, "Order marked as Success", Toast.LENGTH_SHORT).show()
                binding.tvDetailStatus.text = "Success"
                binding.tvDetailStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"))
                binding.btnMarkStatus.visibility = View.GONE
            }
        }
    }
}