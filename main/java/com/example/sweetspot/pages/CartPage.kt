package com.example.sweetspot.pages

import ThemeViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.CartItem
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.ui.theme.LocalThemeViewModel
import com.example.sweetspot.ui.theme.TopBarWithMenu

@SuppressLint("DefaultLocale")
@Composable
fun CartPage(
    navController: NavController,
    cartViewModel: CartViewModel,
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice = cartItems.sumOf { item ->
        val price = when (item.size) {
            "6 inch" -> item.cake.priceSmall
            "8 inch" -> item.cake.priceMedium
            "10 inch" -> item.cake.priceLarge
            else -> item.cake.priceSmall
        }
        price * item.quantity
    }

    Scaffold(
        topBar = {
            TopBarWithMenu(
                navController = navController,
                cartViewModel = cartViewModel,
                title = "Cart"
            )
        },
        bottomBar = {
            NavigationBar(navController = navController)
        },
        modifier = Modifier.navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                if (cartItems.isNotEmpty()) {
                    items(cartItems) { item ->
                        CartItemCard(
                            cartItem = item,
                            onRemove = { cartViewModel.removeFromCart(item) },
                            onIncrease = {
                                cartViewModel.updateQuantity(
                                    item,
                                    item.quantity + 1
                                )
                            },
                            onDecrease = {
                                cartViewModel.updateQuantity(
                                    item,
                                    item.quantity - 1
                                )
                            }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No items in the cart",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleLarge)
                Text(
                    "RM ${String.format("%.2f", totalPrice)}",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Button(
                onClick = { navController.navigate("orderDetails") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)),
                enabled = cartItems.isNotEmpty()
            ) {
                Text("Proceed to Order Details")
            }
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onRemove: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cartItem.cake.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Size: ${cartItem.size}", style = MaterialTheme.typography.bodyMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onDecrease, enabled = cartItem.quantity > 1) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                    }
                    Text(text = "${cartItem.quantity}", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                    }
                }
                Text(
                    text = "Price: RM ${String.format("%.2f",
                        when (cartItem.size) {
                            "6 inch" -> cartItem.cake.priceSmall
                            "8 inch" -> cartItem.cake.priceMedium
                            "10 inch" -> cartItem.cake.priceLarge
                            else -> cartItem.cake.priceSmall
                        } * cartItem.quantity
                    )}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove from cart")
            }
        }
    }
}