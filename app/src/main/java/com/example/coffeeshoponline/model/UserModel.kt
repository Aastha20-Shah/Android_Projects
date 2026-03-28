package com.example.coffeeshoponline.model

import java.io.Serializable

data class UserModel(
    var id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: Any? = null, // Changed to Any? to handle both String and Object/Map from database
    val firstOrder: Boolean = false
) : Serializable
