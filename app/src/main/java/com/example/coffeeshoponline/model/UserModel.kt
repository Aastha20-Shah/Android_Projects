package com.example.coffeeshoponline.model

data class UserModel(
    var id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val firstOrder: Boolean = false
)
