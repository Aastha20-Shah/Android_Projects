package com.example.coffeeshoponline.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.CartAdapter
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.example.coffeeshoponline.Helper.ManagmentCart
import com.example.coffeeshoponline.Helper.ChangeNumberItemsListener
import com.example.coffeeshoponline.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var managementCart: ManagmentCart

    private lateinit var auth: FirebaseAuth
    private var discount = 0.0
    private var couponApplied = false
    private var appliedCouponCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        managementCart = ManagmentCart(this)

        initCart()
        calculateCart()
        binding.button2.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 2. Hide the keyboard using the EditText's window token
            imm.hideSoftInputFromWindow(binding.editTextText2.windowToken, 0)
            // 3. Clear focus so the cursor disappears
            binding.editTextText2.clearFocus()
            val user = auth.currentUser

            if (user == null) {
                showMessage("Please login to apply coupons.")
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            val couponCode = binding.editTextText2.text.toString().trim().uppercase()
            val subtotal = managementCart.getTotalFee()
            if (couponCode.isEmpty()) {
                showMessage("Please enter a coupon code")
                return@setOnClickListener
            }
            when (couponCode) {

                "FIRST50" -> {

                    val uid = user.uid

                    FirebaseDatabase.getInstance()
                        .reference
                        .child("users")
                        .child(uid)
                        .child("firstOrder")
                        .get()
                        .addOnSuccessListener { snapshot ->

                            val isFirstOrder = snapshot.getValue(Boolean::class.java) ?: false

                            if (!isFirstOrder) {
                                showMessage("FIRST50 coupon is only valid for your first order.")
                                return@addOnSuccessListener
                            }

                            appliedCouponCode = couponCode
                            couponApplied = true
                            showMessage("🎉 FIRST50 applied! You got 50% discount.")
                            calculateCart()
                        }

                    return@setOnClickListener
                }

                "SAVE20" -> {

                    appliedCouponCode = couponCode
                    couponApplied = true
                    showMessage("SAVE20 applied successfully!")

                }

                "COFFEE100" -> {

                    if (subtotal < 500) {
                        showMessage("Minimum order of ₹500 required for COFFEE100 coupon.")
                        return@setOnClickListener
                    }

                    appliedCouponCode = couponCode
                    couponApplied = true
                    showMessage("COFFEE100 applied! ₹100 discount added.")

                }

                else -> {
                    showMessage("Invalid coupon code")
                    return@setOnClickListener
                }
            }

            calculateCart()
        }
        binding.Backbtn.setOnClickListener {
            finish()
        }
        checkCheckout()
    }

    private fun initCart() {
        binding.listView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.listView.adapter = CartAdapter(
            managementCart.getListCart(),
            managementCart,
            object : ChangeNumberItemsListener {
                override fun onChanged() {
                    calculateCart()
                    binding.listView.adapter?.notifyDataSetChanged()
                }
            }
        )
    }

    private fun calculateCart() {
        val subtotal = managementCart.getTotalFee()
        discount = 0.0
        if (couponApplied && appliedCouponCode != null) {

            when (appliedCouponCode) {

                "FIRST50" -> {
                    discount = subtotal * 0.50
                }

                "SAVE20" -> {
                    discount = subtotal * 0.20
                }

                "COFFEE100" -> {
                    if (subtotal >= 500) {
                        discount = 100.0
                    }
                }
            }
        }
        val discountedSubtotal = subtotal - discount
        val tax = discountedSubtotal * 0.05
        val delivery = if (discountedSubtotal > 0) 40.0 else 0.0
        val total = discountedSubtotal + tax + delivery

        binding.totalFeeTxt.text = "₹%.2f".format(subtotal)
        binding.discountTxt.text = "-₹%.2f".format(discount)
        binding.totalTaxTxt.text = "₹%.2f".format(tax)
        binding.deliveryTxt.text = "₹%.2f".format(delivery)
        binding.totalTxt.text = "₹%.2f".format(total)
    }
    private fun showMessage(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Coupon")
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        builder.show()
    }
    private fun checkCheckout() {

        binding.button.setOnClickListener {

            val user = auth.currentUser

            if (user == null) {

                // User NOT logged in
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

            } else {

                val uid = user.uid

                // Mark first order as completed
                if (appliedCouponCode == "FIRST50") {
                    FirebaseDatabase.getInstance()
                        .reference
                        .child("users")
                        .child(uid)
                        .child("firstOrder")
                        .setValue(false)
                }
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }
        }
    }

}