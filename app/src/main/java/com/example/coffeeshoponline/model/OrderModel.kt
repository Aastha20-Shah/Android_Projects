package com.example.coffeeshoponline.model

data class OrderModel(
    val orderId: String = "",
    val status: String = "",
    val paymentMethod: String = "",
    val totalAmount: Long = 0,
    val timestamp: Long = 0,
    val userId: String = "",
    val userName: String = "",
    val address: String = "", // In your JSON, this is a String representation
    val items: List<OrderItem>? = null
) {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}