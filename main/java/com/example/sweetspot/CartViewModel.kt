package com.example.sweetspot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sweetspot.models.Cake
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class CartItem(
    val cake: Cake,
    val size: String,
    val quantity: Int
)

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _collectionDateTime = MutableStateFlow<Pair<LocalDate, LocalTime>?>(null)
    val collectionDateTime: StateFlow<Pair<LocalDate, LocalTime>?> = _collectionDateTime.asStateFlow()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun addToCart(cake: Cake, size: String, quantity: Int) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.cake.name == cake.name && it.size == size }
            if (existingItem != null) {
                currentItems.map { item ->
                    if (item == existingItem) {
                        item.copy(quantity = item.quantity + quantity)
                    } else {
                        item
                    }
                }
            } else {
                currentItems + CartItem(cake, size, quantity)
            }
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.remove(cartItem)
        _cartItems.value = currentItems
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity > 0) {
            _cartItems.update { currentItems ->
                currentItems.map { item ->
                    if (item == cartItem) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun setCollectionDateTime(date: LocalDate, time: LocalTime) {
        _collectionDateTime.value = Pair(date, time)
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf { item ->
            item.cake.priceForSize(item.size) * item.quantity
        }
    }

    fun saveOrderToFirebase() {
        viewModelScope.launch {
            val order = hashMapOf(
                "items" to _cartItems.value.map { item ->
                    hashMapOf(
                        "cakeName" to item.cake.name,
                        "size" to item.size,
                        "quantity" to item.quantity,
                        "price" to item.cake.priceForSize(item.size)
                    )
                },
                "totalPrice" to getTotalPrice(),
                "collectionDate" to _collectionDateTime.value?.first.toString(),
                "collectionTime" to _collectionDateTime.value?.second.toString(),
                "status" to "pending"
            )

            db.collection("orders")
                .add(order)
                .addOnSuccessListener { documentReference ->
                    clearCart()
                }
                .addOnFailureListener { e ->
                }
        }
    }

    fun getTotalPriceForPayment(): Double {
        return getTotalPrice()
    }
}

fun Cake.priceForSize(size: String): Double {
    return when (size) {
        "6 inch" -> priceSmall
        "8 inch" -> priceMedium
        "10 inch" -> priceLarge
        else -> priceSmall
    }
}

