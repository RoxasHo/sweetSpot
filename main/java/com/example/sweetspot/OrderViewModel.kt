package com.example.sweetspot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sweetspot.models.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    fun saveOrder(order: Order, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("orders")
                    .add(order.copy(timestamp = System.currentTimeMillis()))
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}
