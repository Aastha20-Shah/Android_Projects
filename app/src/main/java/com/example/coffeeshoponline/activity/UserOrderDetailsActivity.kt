package com.example.coffeeshoponline.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.AdminOrderItemAdapter
import com.example.coffeeshoponline.databinding.ActivityUserOrderDetailsBinding
import com.example.coffeeshoponline.model.OrderModel
import com.google.firebase.database.FirebaseDatabase

class UserOrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserOrderDetailsBinding
    private var order: OrderModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("order", OrderModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("order") as? OrderModel
        }

        if (order == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupDetails()
        setupItemsList()
        setupRating()
        
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupDetails() {
        order?.let {
            binding.tvOrderId.text = "Order #${it.orderId.takeLast(12)}"
            
            val coffeeList = it.items ?: emptyList()
            if (coffeeList.isNotEmpty()) {
                val firstCoffee = coffeeList[0]
                binding.tvOrderSummary.text = if (coffeeList.size > 1) "${firstCoffee.title} + ${coffeeList.size - 1} more" else firstCoffee.title
                
                if (!firstCoffee.picUrl.isNullOrEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                        .load(firstCoffee.picUrl[0])
                        .into(binding.ivOrderPic)
                }
            }

            binding.tvOrderPriceMethod.text = "Method: ${it.paymentMethod} • ₹${it.totalAmount}"
            binding.tvAddress.text = it.address
            binding.tvTotalAmount.text = "₹${it.totalAmount}"

            // Status visibility logic
            when (it.status.lowercase()) {
                "delivered", "success" -> {
                    binding.llDeliveryMsg.visibility = View.VISIBLE
                    binding.cvRating.visibility = View.VISIBLE
                    binding.llPendingMsg.visibility = View.GONE
                }
                "pending" -> {
                    binding.llDeliveryMsg.visibility = View.GONE
                    binding.cvRating.visibility = View.GONE
                    binding.llPendingMsg.visibility = View.VISIBLE
                }
                else -> {
                    binding.llDeliveryMsg.visibility = View.GONE
                    binding.cvRating.visibility = View.GONE
                    binding.llPendingMsg.visibility = View.GONE
                }
            }
        }
    }

    private fun setupItemsList() {
        order?.items?.let { items ->
            binding.rvOrderItems.layoutManager = LinearLayoutManager(this)
            binding.rvOrderItems.adapter = AdminOrderItemAdapter(items)
        }
    }

    private fun setupRating() {
        order?.let { currentOrder ->
            binding.ratingBar.rating = currentOrder.rating
            
            binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    saveRatingToDatabase(rating)
                }
            }
        }
    }

    private fun saveRatingToDatabase(rating: Float) {
        val orderId = order?.orderId ?: return
        val userId = order?.userId ?: return
        val database = FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
        
        val updates = HashMap<String, Any>()
        updates["/orders/$orderId/rating"] = rating
        updates["/users/$userId/ratings/$orderId"] = rating 

        database.reference.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Rating saved to your account!", Toast.LENGTH_SHORT).show()
            order = order?.copy(rating = rating)
        }
    }
}