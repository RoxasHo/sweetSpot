package com.example.sweetspot.models

//added section start//
data class Cake(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val priceSmall: Double = 0.00,
    val priceMedium: Double = 0.00,
    val priceLarge: Double = 0.00,
    val category: String = "",
    val description: String = "",
    val orders: Int = 0 //
) {
    fun getPrice(size: String): Double {
        return when (size) {
            "6 inch" -> priceSmall
            "8 inch" -> priceMedium
            "10 inch" -> priceLarge
            else -> priceSmall
        }
    }
}
