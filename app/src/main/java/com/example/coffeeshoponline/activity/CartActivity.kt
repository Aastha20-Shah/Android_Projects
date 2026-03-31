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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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
        
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        managementCart = ManagmentCart(this)

        validateCartItems()
        initCart()
        calculateCart()

        val coupons = arrayOf("FIRST50", "SAVE20", "COFFEE100")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, coupons)
        binding.editTextText2.setAdapter(adapter)

        binding.editTextText2.setOnClickListener {
            binding.editTextText2.showDropDown()
        }

        binding.editTextText2.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.editTextText2.showDropDown()
            }
        }
        
        binding.button2.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editTextText2.windowToken, 0)
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
                            val isFirstOrder = snapshot.getValue(Boolean::class.java) ?: true
                            if (isFirstOrder == false) {
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

        binding.continueBtn.setOnClickListener {
            finish()
        }

        checkCheckout()
    }

    private fun validateCartItems() {
        val cartList = managementCart.getListCart()
        if (cartList.isEmpty()) return

        FirebaseDatabase.getInstance().reference.child("items")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val validItemIds = snapshot.children.mapNotNull { it.key?.toIntOrNull() }.toSet()
                    val updatedCartList = cartList.filter { it.id in validItemIds }
                    
                    if (updatedCartList.size != cartList.size) {
                        // Some items were deleted by admin
                        val updatedArrayList = ArrayList(updatedCartList)
                        val user = auth.currentUser
                        if (user != null) {
                            val tinyDB = com.example.coffeeshoponline.Helper.TinyDB(this@CartActivity)
                            tinyDB.putListObject("CartList_" + user.uid, updatedArrayList)
                            initCart()
                            calculateCart()
                            Toast.makeText(this@CartActivity, "Some items in your cart are no longer available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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
                }
            }
        )
    }

    private fun calculateCart() {
        val listCart = managementCart.getListCart()
        if (listCart.isEmpty()) {
            binding.emptyCartLayout.visibility = View.VISIBLE
            binding.listView.visibility = View.GONE
            binding.couponLayout.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE
        } else {
            binding.emptyCartLayout.visibility = View.GONE
            binding.listView.visibility = View.VISIBLE
            binding.couponLayout.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
            
            (binding.listView.adapter as? CartAdapter)?.let {
                it.listItems = listCart
                it.notifyDataSetChanged()
            }
        }

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
        val builder = AlertDialog.Builder(this)
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
            
            if (managementCart.getListCart().isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
        val fullAddress = listOfNotNull(
            savedAddress["house"]?.takeIf { it.isNotBlank() },
            savedAddress["area"]?.takeIf { it.isNotBlank() },
            savedAddress["landmark"]?.takeIf { it.isNotBlank() },
            savedAddress["city"]?.takeIf { it.isNotBlank() },
            savedAddress["state"]?.takeIf { it.isNotBlank() },
            savedAddress["pincode"]?.takeIf { it.isNotBlank() },
            savedAddress["country"]?.takeIf { it.isNotBlank() }
        ).joinToString(", ")

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

        dialogBinding.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.saveAddressBtn.text =
            if (isFirstTime) "Save & Order" else "Use for this Order"

        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        dialogBinding.saveAddressBtn.setOnClickListener {
            val addressMap = mapOf(
                "house" to dialogBinding.etHouse.text.toString().trim(),
                "area" to dialogBinding.etArea.text.toString().trim(),
                "landmark" to dialogBinding.etLandmark.text.toString().trim(),
                "city" to dialogBinding.etCity.text.toString().trim(),
                "state" to dialogBinding.etState.text.toString().trim(),
                "pincode" to dialogBinding.etPincode.text.toString().trim(),
                "country" to dialogBinding.etCountry.text.toString().trim()
            )

            if (addressMap.filterKeys { it != "landmark" }.values.any { it.isEmpty() }) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedAddress = addressMap

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

        summaryBinding.rvSummaryItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = CartAdapter(managementCart.getListCart(), managementCart, object : ChangeNumberItemsListener {
                override fun onChanged() {}
            })
        }

        val fullAddress = listOfNotNull(
            selectedAddress?.get("house")?.takeIf { it.isNotBlank() },
            selectedAddress?.get("area")?.takeIf { it.isNotBlank() },
            selectedAddress?.get("landmark")?.takeIf { it.isNotBlank() },
            selectedAddress?.get("city")?.takeIf { it.isNotBlank() },
            selectedAddress?.get("state")?.takeIf { it.isNotBlank() },
            selectedAddress?.get("pincode")?.takeIf { it.isNotBlank() },
            selectedAddress?.get("country")?.takeIf { it.isNotBlank() }
        ).joinToString(", ")
        summaryBinding.summaryAddressTxt.text = "$userName\n$fullAddress"

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

        summaryBinding.continueBtn.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, PaymentActivity::class.java)

            intent.putExtra("userName", userName)
            intent.putExtra("address", fullAddress)
            intent.putExtra("subtotal", subtotal)
            intent.putExtra("discount", discount)
            intent.putExtra("delivery", delivery)
            intent.putExtra("tax", tax)
            intent.putExtra("total", "₹%.2f".format(total))

            startActivity(intent)
        }

        dialog.show()
        val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }
}