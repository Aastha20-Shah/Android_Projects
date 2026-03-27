package com.example.coffeeshoponline

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.Helper.ManagmentCart
import com.example.coffeeshoponline.activity.MainActivity
import com.example.coffeeshoponline.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var managementCart: ManagmentCart
    private lateinit var auth: FirebaseAuth

    private var userName: String? = null
    private var address: String? = null
    
    private var subtotal: Double = 0.0
    private var discount: Double = 0.0
    private var delivery: Double = 0.0
    private var tax: Double = 0.0
    private var totalAmount: Double = 0.0

    private val handler = Handler(Looper.getMainLooper())
    private var currentIconIndex = 0
    private val paymentIcons = intArrayOf(
        R.drawable.img,   // GPay
        R.drawable.img_2, // Paytm
        R.drawable.img_1  // PhonePe
    )

    private val iconRotationRunnable = object : Runnable {
        override fun run() {
            binding.imgOnline.animate()
                .translationX(-50f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.imgOnline.setImageResource(paymentIcons[currentIconIndex])
                    currentIconIndex = (currentIconIndex + 1) % paymentIcons.size
                    binding.imgOnline.translationX = 50f
                    binding.imgOnline.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
                .start()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Checkout.preload(applicationContext)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        managementCart = ManagmentCart(this)

        userName = intent.getStringExtra("userName")
        address = intent.getStringExtra("address")
        
        calculatePrices()

        handler.post(iconRotationRunnable)

        binding.priceDetailsHeaderLayout.setOnClickListener { togglePriceDetails() }
        binding.viewPriceDetailsBtn.setOnClickListener {
            if (binding.priceDetailsContentLayout.visibility == View.GONE) togglePriceDetails()
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, binding.priceDetailsHeaderLayout.top) }
        }

        binding.placeOrderBtn.setOnClickListener {
            val paymentMethod = getSelectedPaymentMethod()
            if (paymentMethod == "COD") {
                placeOrder(paymentMethod, "Pending")
            } else {
                startRazorpayPayment(paymentMethod)
            }
        }

        binding.payOnlineTitleLayout.setOnClickListener {
            clearMainRadio()
            binding.onlineOption.isChecked = true
            binding.onlineSubOptionsLayout.visibility = View.VISIBLE
            if (!binding.gpayOption.isChecked && !binding.phonepeOption.isChecked && 
                !binding.paytmOption.isChecked && !binding.cardOption.isChecked) {
                binding.gpayOption.isChecked = true
            }
        }

        binding.codTitleLayout.setOnClickListener {
            clearMainRadio()
            binding.codOption.isChecked = true
            binding.onlineSubOptionsLayout.visibility = View.GONE
        }

        binding.gpayLayout.setOnClickListener { clearNestedRadio(); binding.gpayOption.isChecked = true }
        binding.phonepeLayout.setOnClickListener { clearNestedRadio(); binding.phonepeOption.isChecked = true }
        binding.paytmLayout.setOnClickListener { clearNestedRadio(); binding.paytmOption.isChecked = true }
        binding.cardLayout.setOnClickListener { clearNestedRadio(); binding.cardOption.isChecked = true }
        binding.backBtn.setOnClickListener { finish() }
    }

    private fun togglePriceDetails() {
        if (binding.priceDetailsContentLayout.visibility == View.VISIBLE) {
            binding.priceDetailsContentLayout.visibility = View.GONE
            binding.ivPriceDetailsArrow.animate().rotation(270f).setDuration(200).start()
        } else {
            binding.priceDetailsContentLayout.visibility = View.VISIBLE
            binding.ivPriceDetailsArrow.animate().rotation(90f).setDuration(200).start()
        }
    }

    private fun calculatePrices() {
        subtotal = intent.getDoubleExtra("subtotal", 0.0)
        discount = intent.getDoubleExtra("discount", 0.0)
        delivery = intent.getDoubleExtra("delivery", 0.0)
        tax = intent.getDoubleExtra("tax", 0.0)
        totalAmount = intent.getStringExtra("total")?.replace("₹", "")?.toDoubleOrNull() ?: 0.0

        binding.subtotalTxt.text = "₹%.2f".format(subtotal)
        binding.discountTxt.text = "-₹%.2f".format(discount)
        binding.deliveryTxt.text = "₹%.2f".format(delivery)
        binding.taxTxt.text = "₹%.2f".format(tax)
        binding.totalPayableTxt.text = "₹%.2f".format(totalAmount)
        binding.priceTxt.text = "₹%.2f".format(totalAmount)
        
        val itemCount = managementCart.getListCart().size
        binding.priceDetailTitle.text = "Price Details ($itemCount Item${if(itemCount > 1) "s" else ""})"
    }

    private fun getSelectedPaymentMethod(): String {
        return when {
            binding.codOption.isChecked -> "COD"
            binding.gpayOption.isChecked -> "GPay"
            binding.phonepeOption.isChecked -> "PhonePe"
            binding.paytmOption.isChecked -> "Paytm"
            binding.cardOption.isChecked -> "Card"
            else -> "Online"
        }
    }

    private fun startRazorpayPayment(method: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_RJYHP1TteC3Y5A")

        try {
            val options = JSONObject()
            options.put("name", "Coffee Shop Online")
            options.put("description", "Order Payment")
            options.put("theme.color", "#673B25")
            options.put("currency", "INR")
            options.put("amount", (totalAmount * 100).roundToInt().toString())

            // Force specific payment method
            when (method) {
                "GPay" -> {
                    options.put("method", "upi")
                    val upi = JSONObject()
                    upi.put("flow", "intent")
                    upi.put("app_package", "com.google.android.apps.nbu.paisa.user")
                    options.put("upi", upi)
                }
                "PhonePe" -> {
                    options.put("method", "upi")
                    val upi = JSONObject()
                    upi.put("flow", "intent")
                    upi.put("app_package", "com.phonepe.app")
                    options.put("upi", upi)
                }
                "Paytm" -> {
                    options.put("method", "upi")
                    val upi = JSONObject()
                    upi.put("flow", "intent")
                    upi.put("app_package", "net.one97.paytm")
                    options.put("upi", upi)
                }
                "Card" -> {
                    options.put("method", "card")
                    
                    // Config to hide all other methods and show only card
                    val config = JSONObject()
                    val display = JSONObject()
                    val hide = JSONArray()
                    
                    val methodsToHide = arrayOf("upi", "netbanking", "wallet")
                    for (m in methodsToHide) {
                        val obj = JSONObject()
                        obj.put("method", m)
                        hide.put(obj)
                    }
                    
                    display.put("hide", hide)
                    
                    // Specific blocks config for card
                    val blocks = JSONObject()
                    val cardBlock = JSONObject()
                    cardBlock.put("name", "Pay using Card")
                    val instruments = JSONArray()
                    val cardInstrument = JSONObject()
                    cardInstrument.put("method", "card")
                    instruments.put(cardInstrument)
                    cardBlock.put("instruments", instruments)
                    blocks.put("card_payment", cardBlock)
                    
                    display.put("blocks", blocks)
                    display.put("sequence", JSONArray().put("block.card_payment"))
                    
                    val preferences = JSONObject()
                    preferences.put("show_default_blocks", false)
                    display.put("preferences", preferences)
                    
                    config.put("display", display)
                    options.put("config", config)
                }
            }

            val prefill = JSONObject()
            prefill.put("contact", "9313252046") 
            prefill.put("email", "customer@example.com")
            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        placeOrder(getSelectedPaymentMethod(), "Success")
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        Toast.makeText(this, "Payment failed.", Toast.LENGTH_LONG).show()
    }

    private fun placeOrder(paymentMethod: String, paymentStatus: String) {
        val uid = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
        val orderRef = database.reference.child("orders").push()

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

        orderRef.setValue(orderData).addOnSuccessListener {
            managementCart.clearCart()
            Toast.makeText(this, "☕ Order Placed Successfully!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
    }

    private fun clearMainRadio() {
        binding.onlineOption.isChecked = false
        binding.codOption.isChecked = false
    }

    private fun clearNestedRadio() {
        binding.gpayOption.isChecked = false
        binding.phonepeOption.isChecked = false
        binding.paytmOption.isChecked = false
        binding.cardOption.isChecked = false
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(iconRotationRunnable)
    }
}
