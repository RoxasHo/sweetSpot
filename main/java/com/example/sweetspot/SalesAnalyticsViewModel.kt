package com.example.sweetspot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sweetspot.models.Order
import com.example.sweetspot.models.RevenueData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

data class CakeSalesData(
    val cakeName: String,
    val totalQuantity: Int
)

class SalesAnalyticsViewModel : ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _hotSellingCakes = MutableStateFlow<List<CakeSalesData>>(emptyList())
    val hotSellingCakes: StateFlow<List<CakeSalesData>> = _hotSellingCakes.asStateFlow()

    private val _revenueData = MutableStateFlow<List<RevenueData>>(emptyList())
    val revenueData: StateFlow<List<RevenueData>> = _revenueData.asStateFlow()

    fun fetchHotSellingCakes() {
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("orders").get().await()

                val cakeSalesMap = mutableMapOf<String, Int>()

                for (document in querySnapshot) {
                    val order = document.toObject(Order::class.java)
                    for (item in order.items) {
                        cakeSalesMap[item.cakeName] =
                            cakeSalesMap.getOrDefault(item.cakeName, 0) + item.quantity
                    }
                }

                val cakeSalesList = cakeSalesMap.map { (cakeName, totalQuantity) ->
                    CakeSalesData(cakeName, totalQuantity)
                }.sortedByDescending { it.totalQuantity }

                _hotSellingCakes.value = cakeSalesList

            } catch (e: Exception) {
                Log.e("SalesAnalyticsViewModel", "Error fetching hot selling cakes: ${e.message}")
            }
        }
    }

    fun fetchRevenueData(selectedYear: Int?, selectedMonth: Month?, timeFrame: String) {
        viewModelScope.launch {
            when (timeFrame) {
                "Daily" -> if (selectedYear != null && selectedMonth != null) fetchDailyRevenue(selectedYear, selectedMonth)
                "Monthly" -> if (selectedYear != null) fetchMonthlyRevenue(selectedYear)
                "Yearly" -> fetchYearlyRevenue()
            }
        }
    }

    private suspend fun fetchDailyRevenue(selectedYear: Int, selectedMonth: Month) {
        try {
            val startDate = LocalDate.of(selectedYear, selectedMonth, 1)
            val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val querySnapshot = db.collection("orders")
                .whereGreaterThanOrEqualTo("collectionDate", formatter.format(startDate))
                .whereLessThanOrEqualTo("collectionDate", formatter.format(endDate))
                .get()
                .await()

            val dailyRevenueMap = mutableMapOf<Int, Double>()

            for (document in querySnapshot) {
                val order = document.toObject(Order::class.java)
                val orderDate = LocalDate.parse(order.collectionDate, formatter)
                val dayOfMonth = orderDate.dayOfMonth
                dailyRevenueMap[dayOfMonth] = dailyRevenueMap.getOrDefault(dayOfMonth, 0.0) + order.totalPrice
            }

            val dailyRevenueList = dailyRevenueMap.map { (day, totalRevenue) ->
                RevenueData(day.toString(), totalRevenue)
            }.sortedBy { it.label.toInt() }

            _revenueData.value = dailyRevenueList

        } catch (e: Exception) {
            Log.e("SalesAnalyticsViewModel", "Error fetching daily revenue: ${e.message}")
        }
    }

    private suspend fun fetchMonthlyRevenue(selectedYear: Int) {
        try {
            val querySnapshot = db.collection("orders")
                .whereGreaterThanOrEqualTo("collectionDate", "$selectedYear-01-01")
                .whereLessThanOrEqualTo("collectionDate", "$selectedYear-12-31")
                .get()
                .await()

            val monthlyRevenueMap = mutableMapOf<Int, Double>()

            for (document in querySnapshot) {
                val order = document.toObject(Order::class.java)
                val month = order.collectionDate.substring(5, 7).toInt()
                monthlyRevenueMap[month] =
                    monthlyRevenueMap.getOrDefault(month, 0.0) + order.totalPrice
            }

            val monthNames = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )

            val monthlyRevenueList = monthlyRevenueMap.map { (month, totalRevenue) ->
                val monthName = monthNames[month - 1]
                RevenueData(monthName, totalRevenue)
            }.sortedBy { monthNames.indexOf(it.label) }

            _revenueData.value = monthlyRevenueList
        } catch (e: Exception) {
        }
    }

    private suspend fun fetchYearlyRevenue() {
        try {
            val querySnapshot = db.collection("orders").get().await()
            val yearlyRevenueMap = mutableMapOf<Int, Double>()

            // Aggregate revenue by year
            for (document in querySnapshot) {
                val order = document.toObject(Order::class.java)
                val year = order.collectionDate.substring(0, 4).toInt()
                yearlyRevenueMap[year] =
                    yearlyRevenueMap.getOrDefault(year, 0.0) + order.totalPrice
            }

            val yearlyRevenueList = yearlyRevenueMap.map { (year, totalRevenue) ->
                RevenueData(year.toString(), totalRevenue)
            }.sortedBy { it.label }

            _revenueData.value = yearlyRevenueList
        } catch (e: Exception) {
        }
    }
}

