package com.example.sweetspot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sweetspot.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class SortOption {
    DATE, PRICE, STATUS
}

class OrderHistoryViewModel(private val filterByUser: Boolean = true) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _filterStartDate = MutableStateFlow<LocalDate?>(null)
    val filterStartDate: StateFlow<LocalDate?> = _filterStartDate.asStateFlow()

    private val _filterEndDate = MutableStateFlow<LocalDate?>(null)
    val filterEndDate: StateFlow<LocalDate?> = _filterEndDate.asStateFlow()

    private val _isAscending = MutableStateFlow(false)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Define the combined flow as a property
    val filteredAndSortedOrders: StateFlow<List<Order>> = combine(
        orders,
        filterStartDate,
        filterEndDate,
        isAscending,
        sortOption
    ) { ordersList, startDate, endDate, ascending, sortOption ->
        var filteredOrders = ordersList

        Log.d("OrderHistoryViewModel", "Initial orders count: ${filteredOrders.size}")

        if (startDate != null) {
            filteredOrders = filteredOrders.filter { order ->
                val orderDate = Instant.ofEpochMilli(order.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                !orderDate.isBefore(startDate)
            }
            Log.d("OrderHistoryViewModel", "After start date filter: ${filteredOrders.size}")
        }
        if (endDate != null) {
            filteredOrders = filteredOrders.filter { order ->
                val orderDate = Instant.ofEpochMilli(order.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                !orderDate.isAfter(endDate)
            }
            Log.d("OrderHistoryViewModel", "After end date filter: ${filteredOrders.size}")
        }

        filteredOrders = when (sortOption) {
            SortOption.DATE -> if (ascending) filteredOrders.sortedBy { it.timestamp } else filteredOrders.sortedByDescending { it.timestamp }
            SortOption.PRICE -> if (ascending) filteredOrders.sortedBy { it.totalPrice } else filteredOrders.sortedByDescending { it.totalPrice }
            SortOption.STATUS -> if (ascending) filteredOrders.sortedBy { it.status } else filteredOrders.sortedByDescending { it.status }
        }

        Log.d("OrderHistoryViewModel", "After sorting: ${filteredOrders.size}")

        filteredOrders
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        fetchOrders()
    }

    private fun fetchOrders() {
        viewModelScope.launch {
            val fetchedOrders = getOrdersFromDataSource()
            _orders.value = fetchedOrders
            Log.d("OrderHistoryViewModel", "Fetched orders: ${fetchedOrders.size}")
        }
    }

    fun setFilterStartDate(date: LocalDate?) {
        _filterStartDate.value = date
    }

    fun setFilterEndDate(date: LocalDate?) {
        _filterEndDate.value = date
    }

    fun toggleSortOrder() {
        _isAscending.value = !_isAscending.value
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("orders").document(order.id)
            .update("status", newStatus)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
            }
    }

    private suspend fun getOrdersFromDataSource(): List<Order> {
        return withContext(Dispatchers.IO) {
            val firestore = FirebaseFirestore.getInstance()

            val query = if (filterByUser) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                firestore.collection("orders").whereEqualTo("userId", currentUserId)
            } else {
                firestore.collection("orders")
            }

            val querySnapshot = query.get().await()

            val ordersList = mutableListOf<Order>()
            for (document in querySnapshot.documents) {
                val order = document.toObject(Order::class.java)?.copy(id = document.id)
                if (order != null) {
                    ordersList.add(order)
                }
            }
            Log.d("OrderHistoryViewModel", "Fetched orders: ${ordersList.size}")
            ordersList
        }
    }

}
