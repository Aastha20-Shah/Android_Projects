package com.example.coffeeshoponline.model

import java.io.Serializable

data class OrderItem(
    val title: String = "",
    val numberInCart: Int = 0,
    val selectedSize: String = "",
    val priceSmall: Double = 0.0,
    val priceMedium: Double = 0.0,
    val priceLarge: Double = 0.0,
    val picUrl: List<String>? = null
) : Serializable