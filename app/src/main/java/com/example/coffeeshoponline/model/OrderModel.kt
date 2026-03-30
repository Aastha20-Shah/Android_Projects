package com.example.coffeeshoponline.model

import java.io.Serializable

data class OrderModel(
    val orderId: String = "",
    val status: String = "",
    val paymentStatus: String = "Unpaid",
    val paymentMethod: String = "",
    val totalAmount: Double = 0.0, 
    val timestamp: Long = 0,
    val userId: String = "",
    val userName: String = "",
    val address: String = "",
    val items: List<OrderItem>? = null,
    val rating: Float = 0f
) : Serializable {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}