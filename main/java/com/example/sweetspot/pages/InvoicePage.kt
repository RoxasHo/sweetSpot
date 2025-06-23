package com.example.sweetspot.pages

import ThemeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.priceForSize
import com.example.sweetspot.ui.theme.LocalThemeViewModel
import com.example.sweetspot.ui.theme.TopNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePage(
    navController: NavController,
    cartViewModel: CartViewModel,
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    val cartItems by cartViewModel.cartItems.collectAsState()
    val collectionDateTime by cartViewModel.collectionDateTime.collectAsState()

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Payment Confirmation",
                navController = navController
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Confirmation of Payment",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Collection Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Collection Date: ${collectionDateTime?.first ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Collection Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Collection Time: ${collectionDateTime?.second ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Text(
                        text = "Ordered Items:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(item.cake.imageUrl),
                                contentDescription = item.cake.name,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${item.cake.name} (${item.size})",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "Quantity: ${item.quantity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "RM ${
                                    String.format(
                                        "%.2f",
                                        item.cake.getPrice(item.size) * item.quantity
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Divider()
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "RM ${String.format("%.2f", cartViewModel.getTotalPrice())}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Row {
//                    Button(
//                        onClick = {
//                            val totalAmount = cartViewModel.getTotalPrice()
//                            navController.navigate("googlePayPayment/${totalAmount.toFloat()}")
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
//                    ) {
//                        Text("Select Payment Option")
//                    }
                    Button(
                        onClick = {
                            val totalAmount = cartViewModel.getTotalPrice()
                            navController.navigate("paymentOptions/${totalAmount.toFloat()}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                    ) {
                        Text("Select Payment Option")
                    }
                }
            }
        }
    }
}
