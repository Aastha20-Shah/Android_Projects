package com.example.coffeeshoponline.model

import java.io.Serializable

data class ItemModel(
    val id: Int=0,
    val title: String = "",
    val description: String = "",
    val extra: String = "",
    val picUrl: List<String> = emptyList(),
    val priceSmall: Double = 0.0,
    val priceMedium: Double = 0.0,
    val priceLarge: Double = 0.0,
    val rating: Double = 0.0,
    val categoryId: Any? = 0,
    val isPopular: Int = 0,
    var selectedSize: String = "SMALL",
    var numberInCart: Int = 1,
    var isFavorite: Boolean = false
): Serializable

