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
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import kotlin.math.roundToInt

class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var managementCart: ManagmentCart
    private lateinit var auth: FirebaseAuth

    private var userName: String? = null
    private var address: String? = null
    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Preload Razorpay Checkout
        Checkout.preload(applicationContext)

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
            if (paymentMethod == "COD") {
                placeOrder(paymentMethod, "Pending")
            } else {
                startRazorpayPayment()
            }
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

        binding.cardOption.setOnClickListener {
            clearAllRadio()
            binding.cardOption.isChecked = true
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
            binding.cardOption.isChecked -> "Card"
            else -> "COD"
        }
    }

    private fun startRazorpayPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_RJYHP1TteC3Y5A")

        try {
            val options = JSONObject()
            options.put("name", "Coffee Shop Online")
            options.put("description", "Order Payment")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#673B25")
            options.put("currency", "INR")
            
            val amountInPaise = (totalAmount * 100).roundToInt()
            options.put("amount", amountInPaise.toString())

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        val paymentMethod = getSelectedPaymentMethod()
        placeOrder(paymentMethod, "Success")
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        Toast.makeText(this, "Payment failed. Please try again.", Toast.LENGTH_LONG).show()
    }

    private fun placeOrder(paymentMethod: String, paymentStatus: String) {

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
            "status" to paymentStatus,
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
        binding.cardOption.isChecked = false
    }
    }
