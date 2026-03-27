package com.example.coffeeshoponline.model

import java.io.Serializable

data class OrderItem(
    val title: String = "",
    val numberInCart: Int = 0,
    val selectedSize: String = "",
    val priceSmall: Int = 0,
    val priceMedium: Int = 0,
    val priceLarge: Int = 0,
    val picUrl: List<String>? = null
) : Serializable