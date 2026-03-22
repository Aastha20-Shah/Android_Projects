package com.example.coffeeshoponline

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.coffeeshoponline.Helper.ManagmentCart
import com.example.coffeeshoponline.activity.MainActivity
import com.example.coffeeshoponline.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var managementCart: ManagmentCart
    private lateinit var auth: FirebaseAuth

    private var userName: String? = null
    private var address: String? = null
    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        managementCart = ManagmentCart(this)
        // RECEIVE DATA
        userName = intent.getStringExtra("userName")
        address = intent.getStringExtra("address")

        val totalStr = intent.getStringExtra("total") ?: "0"
        totalAmount = totalStr.replace("₹", "").toDoubleOrNull() ?: 0.0

        binding.priceTxt.text = "₹%.2f".format(totalAmount)

        // PLACE ORDER BUTTON
        binding.placeOrderBtn.setOnClickListener {

            val paymentMethod = getSelectedPaymentMethod()

            placeOrder(paymentMethod)
        }


        binding.gpayOption.setOnClickListener {
            clearAllRadio()
            binding.gpayOption.isChecked = true
        }

        binding.phonepeOption.setOnClickListener {
            clearAllRadio()
            binding.phonepeOption.isChecked = true
        }

        binding.paytmOption.setOnClickListener {
            clearAllRadio()
            binding.paytmOption.isChecked = true
        }

        binding.codOption.setOnClickListener {
            clearAllRadio()
            binding.codOption.isChecked = true
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getSelectedPaymentMethod(): String {
        return when {
            binding.codOption.isChecked -> "COD"
            binding.gpayOption.isChecked -> "GPay"
            binding.phonepeOption.isChecked -> "PhonePe"
            binding.paytmOption.isChecked -> "Paytm"
            else -> "COD"
        }
    }
    private fun placeOrder(paymentMethod: String) {

        val uid = auth.currentUser?.uid ?: return

        val orderRef = FirebaseDatabase.getInstance()
            .reference
            .child("orders")
            .push()

        val orderData = hashMapOf(
            "orderId" to orderRef.key,
            "userId" to uid,
            "userName" to userName,
            "address" to address,
            "items" to managementCart.getListCart(),
            "totalAmount" to totalAmount,
            "paymentMethod" to paymentMethod,
            "status" to "Pending",
            "timestamp" to System.currentTimeMillis()
        )

        orderRef.setValue(orderData)
            .addOnSuccessListener {

                // ✅ CLEAR CART
                managementCart.clearCart()

                // ✅ MARK FIRST ORDER USED
                FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(uid)
                    .child("firstOrder")
                    .setValue(false)

                Toast.makeText(this, "☕ Order Placed Successfully!", Toast.LENGTH_LONG).show()

                // ✅ REDIRECT
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order Failed ❌", Toast.LENGTH_SHORT).show()
            }
    }
    private fun clearAllRadio() {
        binding.gpayOption.isChecked = false
        binding.phonepeOption.isChecked = false
        binding.paytmOption.isChecked = false
        binding.codOption.isChecked = false
    }
    }
