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
import com.example.coffeeshoponline.activity.ThankYouActivity
import com.example.coffeeshoponline.databinding.ActivityPaymentBinding
import com.example.coffeeshoponline.model.ItemModel
import com.example.coffeeshoponline.model.OrderItem
import com.example.coffeeshoponline.model.OrderModel
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
    
    private var subtotal: Double = 0.0
    private var discount: Double = 0.0
    private var delivery: Double = 0.0
    private var tax: Double = 0.0
    private var totalAmount: Double = 0.0
    private var appliedCouponCode: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private var currentIconIndex = 0
    private val paymentIcons = intArrayOf(
        R.drawable.img,   // GPay
        R.drawable.img_2, // Paytm
        R.drawable.img_1  // PhonePe
    )

    private val iconRotationRunnable = object : Runnable {
        override fun run() {
            if (::binding.isInitialized && binding.imgOnline != null) {
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
            }
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
        appliedCouponCode = intent.getStringExtra("appliedCouponCode")
        
        calculatePrices()

        handler.post(iconRotationRunnable)

        binding.priceDetailsHeaderLayout.setOnClickListener { togglePriceDetails() }
        binding.viewPriceDetailsBtn.setOnClickListener {
            if (binding.priceDetailsContentLayout.visibility == View.GONE) togglePriceDetails()
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, binding.priceDetailsHeaderLayout.top) }
        }

        binding.placeOrderBtn.setOnClickListener {
            if (binding.codOption.isChecked) {
                placeOrder("COD")
            } else if (binding.onlineOption.isChecked) {
                startRazorpayPayment()
            } else {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            }
        }

        binding.payOnlineTitleLayout.setOnClickListener {
            binding.onlineOption.isChecked = true
            binding.codOption.isChecked = false
        }

        binding.codTitleLayout.setOnClickListener {
            binding.codOption.isChecked = true
            binding.onlineOption.isChecked = false
        }

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

    private fun startRazorpayPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_RJYHP1TteC3Y5A")

        try {
            val options = JSONObject()
            options.put("name", "Coffee Shop Online")
            options.put("description", "Order Payment")
            options.put("theme.color", "#673B25")
            options.put("currency", "INR")
            options.put("amount", (totalAmount * 100).roundToInt().toString())

            val prefill = JSONObject()
            prefill.put("contact", "9313252046") 
            prefill.put("email", auth.currentUser?.email ?: "customer@example.com")
            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        placeOrder("Online")
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        Toast.makeText(this, "Payment failed.", Toast.LENGTH_LONG).show()
    }

    private fun placeOrder(paymentMethod: String) {
        val uid = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
        val orderRef = database.reference.child("orders").push()

        val cartItems: ArrayList<ItemModel> = managementCart.getListCart()
        
        // Fix for type mismatch: explicitly map ItemModel to OrderItem
        val orderItems: List<OrderItem> = cartItems.map { item ->
            OrderItem(
                title = item.title,
                numberInCart = item.numberInCart,
                selectedSize = item.selectedSize,
                priceSmall = item.priceSmall,
                priceMedium = item.priceMedium,
                priceLarge = item.priceLarge,
                picUrl = item.picUrl
            )
        }

        val orderId = orderRef.key ?: ""
        val timestamp = System.currentTimeMillis()
        val paymentStatus = if (paymentMethod == "Online") "Paid" else "Unpaid"

        val orderModel = OrderModel(
            orderId = orderId,
            status = "Pending",
            paymentStatus = paymentStatus,
            paymentMethod = paymentMethod,
            totalAmount = totalAmount,
            timestamp = timestamp,
            userId = uid,
            userName = userName ?: "",
            address = address ?: "",
            items = orderItems
        )

        orderRef.setValue(orderModel).addOnSuccessListener {
            if (appliedCouponCode == "FIRST50") {
                FirebaseDatabase.getInstance("https://coffeeshoponline-cc40a-default-rtdb.firebaseio.com/")
                    .reference
                    .child("users")
                    .child(uid)
                    .child("firstOrder")
                    .setValue(false)
            }
            
            managementCart.clearCart()
            val intent = Intent(this, ThankYouActivity::class.java)
            intent.putExtra("order", orderModel)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(iconRotationRunnable)
    }
}