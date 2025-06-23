package com.example.sweetspot.pages

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sweetspot.OrderHistoryViewModel
import com.example.sweetspot.OrderHistoryViewModelFactory
import com.example.sweetspot.SortOption
import com.example.sweetspot.models.Order
import com.example.utils.showDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryPage(
    navController: NavController
) {
    val orderHistoryViewModel: OrderHistoryViewModel = viewModel(
        factory = OrderHistoryViewModelFactory(filterByUser = true)
    )

    val orders by orderHistoryViewModel.filteredAndSortedOrders.collectAsState()
    val startDate by orderHistoryViewModel.filterStartDate.collectAsState()
    val endDate by orderHistoryViewModel.filterEndDate.collectAsState()
    val isAscending by orderHistoryViewModel.isAscending.collectAsState()
    val sortOption by orderHistoryViewModel.sortOption.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort Options")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by Date") },
                                onClick = {
                                    orderHistoryViewModel.setSortOption(SortOption.DATE)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Price") },
                                onClick = {
                                    orderHistoryViewModel.setSortOption(SortOption.PRICE)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Status") },
                                onClick = {
                                    orderHistoryViewModel.setSortOption(SortOption.STATUS)
                                    expanded = false
                                }
                            )
                        }
                    }

                    IconButton(onClick = { orderHistoryViewModel.toggleSortOrder() }) {
                        Icon(
                            imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Toggle Sort Order"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFA726))
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = {
                        showDatePicker(context) { date ->
                            orderHistoryViewModel.setFilterStartDate(date)
                        }
                    }) {
                        Text(text = "Start Date: ${startDate?.format(dateFormatter) ?: "Any"}")
                    }
                    OutlinedButton(onClick = {
                        showDatePicker(context) { date ->
                            orderHistoryViewModel.setFilterEndDate(date)
                        }
                    }) {
                        Text(text = "End Date: ${endDate?.format(dateFormatter) ?: "Any"}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (orders.isEmpty()) {
                    Text(text = "No orders found.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(orders) { order ->
                            OrderItemCard(
                                order = order,
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun OrderItemCard(
    order: Order
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Order ID: ${order.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Total Price: RM ${String.format("%.2f", order.totalPrice)}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Collection Date: ${order.collectionDate} ${order.collectionTime}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Items:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            order.items.forEach { item ->
                Text(
                    text = "- ${item.cakeName} (${item.size}), Qty: ${item.quantity}, Price: RM ${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            val orderDate = Instant.ofEpochMilli(order.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
            Text(text = "Order Date: ${orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}", style = MaterialTheme.typography.bodySmall)
        }
    }
}