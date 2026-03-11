package com.example.coffeeshoponline.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshoponline.Adapter.CartAdapter
import com.example.coffeeshoponline.Helper.ManagmentCart
import com.example.coffeeshoponline.Helper.ChangeNumberItemsListener
import com.example.coffeeshoponline.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var managementCart: ManagmentCart

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        managementCart = ManagmentCart(this)

        initCart()
        calculateCart()


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
        val tax = subtotal * 0.05
        val delivery = if (subtotal > 0) 40.0 else 0.0
        val total = subtotal + tax + delivery

        binding.totalFeeTxt.text = "₹$subtotal"
        binding.totalTaxTxt.text = "₹$tax"
        binding.deliveryTxt.text = "₹$delivery"
        binding.totalTxt.text = "₹$total"
    }
    private fun checkCheckout() {

        binding.button.setOnClickListener {

            val user = auth.currentUser

            if (user == null) {

                // User NOT logged in
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

            } else {

                // User logged in
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }
        }
    }

}