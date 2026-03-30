package com.example.coffeeshoponline.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshoponline.R
import com.bumptech.glide.Glide
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.coffeeshoponline.Helper.ManagmentCart
import com.example.coffeeshoponline.Helper.WishlistManager
import com.example.coffeeshoponline.databinding.ActivityDetailBinding
import com.example.coffeeshoponline.model.ItemModel
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.app.AlertDialog

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemModel
    private lateinit var cart: ManagmentCart
    private lateinit var wishlistManager: WishlistManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cart = ManagmentCart(this)
        wishlistManager = WishlistManager(this)

        getBundle()
        initSizeList()
        setListeners()
        updateFavIcon()
    }

    private fun getBundle() {
        item = intent.getSerializableExtra("item") as ItemModel

        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description
        binding.ratingTxt.text = item.rating.toString()
        binding.numberInCartTxt.text = item.numberInCart.toString()

        Glide.with(this)
            .load(item.picUrl.firstOrNull())
            .into(binding.picMain)

        // Default size
        item.selectedSize = "SMALL"
        binding.smallBtn.setBackgroundResource(R.drawable.brown_storke_bg)

        updatePrice()
    }

    private fun initSizeList() {
        binding.apply {

            smallBtn.setOnClickListener {
                selectSize("SMALL")
            }

            mediumBtn.setOnClickListener {
                selectSize("MEDIUM")
            }

            largeBtn.setOnClickListener {
                selectSize("LARGE")
            }
        }
    }

    private fun selectSize(size: String) {
        item.selectedSize = size

        binding.smallBtn.setBackgroundResource(
            if (size == "SMALL") R.drawable.brown_storke_bg else 0
        )
        binding.mediumBtn.setBackgroundResource(
            if (size == "MEDIUM") R.drawable.brown_storke_bg else 0
        )
        binding.largeBtn.setBackgroundResource(
            if (size == "LARGE") R.drawable.brown_storke_bg else 0
        )

        updatePrice()
    }

    private fun updateFavIcon() {
        wishlistManager.isInWishlist(item.id) { isFav ->
            if (isFav) {
                binding.favBtn.setImageResource(R.drawable.ic_heart_filled)
            } else {
                binding.favBtn.setImageResource(R.drawable.ic_heart_outline)
            }
        }
    }

    private fun updatePrice() {
        val unitPrice = when (item.selectedSize) {
            "MEDIUM" -> item.priceMedium
            "LARGE" -> item.priceLarge
            else -> item.priceSmall
        }

        val totalPrice = unitPrice * item.numberInCart
        binding.priceTxt.text = "₹$totalPrice"
    }

    private fun setListeners() {

        binding.plusBtn.setOnClickListener {
            item.numberInCart++
            binding.numberInCartTxt.text = item.numberInCart.toString()
            updatePrice()
        }

        binding.minusBtn.setOnClickListener {
            if (item.numberInCart > 1) {
                item.numberInCart--
                binding.numberInCartTxt.text = item.numberInCart.toString()
                updatePrice()
            }
        }

        binding.addToCartBtn.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                cart.insertItems(item)
            } else {
                showLoginRequiredDialog("add items to your cart")
            }
        }

        binding.favBtn.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                wishlistManager.toggleWishlist(item) { isFav ->
                    if (isFav) {
                        binding.favBtn.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        binding.favBtn.setImageResource(R.drawable.ic_heart_outline)
                    }
                }
            } else {
                showLoginRequiredDialog("add items to your wishlist")
            }
        }

        binding.Backbtn.setOnClickListener {
            finish()
        }
    }

    private fun showLoginRequiredDialog(action: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login Required")
        builder.setMessage("You must be logged in to $action.")
        builder.setPositiveButton("Login") { _, _ ->
            startActivity(Intent(this, LoginActivity::class.java))
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}