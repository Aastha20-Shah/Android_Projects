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
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.coffeeshoponline.PaymentActivity
import com.example.coffeeshoponline.databinding.DialogAddAddressBinding
import com.example.coffeeshoponline.databinding.LayoutOrderSummaryBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var managementCart: ManagmentCart

    private lateinit var auth: FirebaseAuth
    private var discount = 0.0
    private var couponApplied = false
    private var appliedCouponCode: String? = null

    private var selectedAddress: Map<String, String>? = null

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

                            if (isFirstOrder==true) {
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
            val user = auth.currentUser ?: run {
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            // Corrected reference: use .child(uid)
            val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)

            dbRef.get().addOnSuccessListener { snapshot ->
                val userName = snapshot.child("name").getValue(String::class.java) ?: "Customer"
                val savedAddress = snapshot.child("address").value as? Map<String, String>

                if (savedAddress != null) {
                    showAddressSelectionDialog(userName, savedAddress)
                } else {
                    showAddAddressDialog(userName, isFirstTime = true)
                }
            }
        }
    }

    private fun showAddressSelectionDialog(userName: String, savedAddress: Map<String, String>) {
        val fullAddress =
            "${savedAddress["house"]}, ${savedAddress["area"]}, ${savedAddress["city"]}"

        AlertDialog.Builder(this)
            .setTitle("Confirm Delivery")
            .setMessage("Deliver to saved address?\n\n$fullAddress")
            .setPositiveButton("Yes, Deliver here") { _, _ ->
                selectedAddress = savedAddress
                confirmOrder(userName)
            }
            .setNegativeButton("Change Address") { _, _ ->
                showAddAddressDialog(userName, isFirstTime = false)
            }
            .show()
    }

    private fun showAddAddressDialog(userName: String, isFirstTime: Boolean) {
        val dialogBinding = DialogAddAddressBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.saveAddressBtn.text =
            if (isFirstTime) "Save & Order" else "Use for this Order"

        dialogBinding.saveAddressBtn.setOnClickListener {
            val addressMap = mapOf(
                "house" to dialogBinding.etHouse.text.toString().trim(),
                "area" to dialogBinding.etArea.text.toString().trim(),
                "city" to dialogBinding.etCity.text.toString().trim(),
                "pincode" to dialogBinding.etPincode.text.toString().trim()
            )

            if (addressMap.values.any { it.isEmpty() }) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedAddress = addressMap

            // Only save to Firebase Profile if it's their first time
            if (isFirstTime) {
                FirebaseDatabase.getInstance().reference
                    .child("users").child(auth.currentUser?.uid ?: "")
                    .child("address").setValue(addressMap)
            }

            dialog.dismiss()
            confirmOrder(userName)
        }
        dialog.show()
    }

    private fun confirmOrder(userName: String) {
        val dialog = BottomSheetDialog(this)
        val summaryBinding = LayoutOrderSummaryBinding.inflate(layoutInflater)
        dialog.setContentView(summaryBinding.root)

        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )

        bottomSheet?.layoutParams?.height =
            android.view.ViewGroup.LayoutParams.MATCH_PARENT

        // 2. Setup RecyclerView with your viewholder_cart
        summaryBinding.rvSummaryItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            // We set listener to null or empty to prevent editing in summary
            adapter = CartAdapter(managementCart.getListCart(), managementCart, object : ChangeNumberItemsListener {
                override fun onChanged() {}
            })
        }

        // 3. Dynamic Address
        val fullAddress = "${selectedAddress?.get("house")}, ${selectedAddress?.get("area")}, ${selectedAddress?.get("city")}"
        summaryBinding.summaryAddressTxt.text = "$userName\n$fullAddress"

        // 4. Dynamic Prices (Matching your calculation logic)
        val subtotal = managementCart.getTotalFee()
        val discountedSubtotal = subtotal - discount
        val tax = discountedSubtotal * 0.05
        val delivery = if (discountedSubtotal > 0) 40.0 else 0.0
        val total = discountedSubtotal + tax + delivery

        summaryBinding.subtotalSum.text = "₹%.2f".format(subtotal)
        summaryBinding.discountSum.text = "-₹%.2f".format(discount)
        summaryBinding.deliverySum.text = "₹%.2f".format(delivery)
        summaryBinding.totalTaxTxt.text = "₹%.2f".format(tax)
        summaryBinding.totalSum.text = "₹%.2f".format(total)

        // 5. Handle Final Checkout
        summaryBinding.continueBtn.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, PaymentActivity::class.java)

            intent.putExtra("userName", userName)
            intent.putExtra("total", binding.totalTxt.text.toString())
            intent.putExtra("address", selectedAddress.toString())

            startActivity(intent)
        }

        dialog.show()
        val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }
}