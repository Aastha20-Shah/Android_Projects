package com.example.coffeeshoponline.model

import java.io.Serializable

data class OrderModel(
    val orderId: String = "",
    val status: String = "",
    val paymentMethod: String = "",
    val totalAmount: Double = 0.0, // Changed from Long to Double to match pricing
    val timestamp: Long = 0,
    val userId: String = "",
    val userName: String = "",
    val address: String = "",
    val items: List<OrderItem>? = null
) : Serializable {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}