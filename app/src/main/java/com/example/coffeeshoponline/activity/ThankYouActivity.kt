package com.example.coffeeshoponline.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.databinding.ActivityThankYouBinding
import com.example.coffeeshoponline.model.OrderModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThankYouActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThankYouBinding
    private var order: OrderModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThankYouBinding.inflate(layoutInflater)
        setContentView(binding.root)

        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("order", OrderModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("order") as? OrderModel
        }

        if (order == null) {
            finish()
            return
        }

        setupViews()

        binding.closeBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }

        binding.viewStatusBtn.setOnClickListener {
            val intent = Intent(this, UserOrderDetailsActivity::class.java)
            intent.putExtra("order", order)
            startActivity(intent)
            finish()
        }
    }

    private fun setupViews() {
        order?.let {
            binding.tvThankYouOrderId.text = "Order Number: #${it.orderId}"
            binding.tvThankYouShortId.text = "#${it.orderId.takeLast(8)}"
            binding.tvThankYouTotal.text = "₹%.2f".format(it.totalAmount)
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvThankYouDate.text = sdf.format(Date(it.timestamp))

            val items = it.items
            if (!items.isNullOrEmpty() && !items[0].picUrl.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(items[0].picUrl?.get(0))
                    .into(binding.ivThankYouItem)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
}