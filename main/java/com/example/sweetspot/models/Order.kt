package com.example.sweetspot.models

import com.example.sweetspot.CartItem

data class Order(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "",
    val paymentMethod: String = "",
    val collectionDate: String = "",
    val collectionTime: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val sendEmail: Boolean = true,
    val emailSent: Boolean = false
)

data class OrderItem(
    val cakeId: String = "",
    val cakeName: String = "",
    val size: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)



