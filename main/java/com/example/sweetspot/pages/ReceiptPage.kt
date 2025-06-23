package com.example.sweetspot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sweetspot.CartItem
import com.example.sweetspot.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPage(
    navController: NavController,
    cartViewModel: CartViewModel,
    totalAmount: Double,
    paymentMethod: String
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var isOrderSaved by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        saveOrderToFirebase(auth, db, cartItems, totalAmount, paymentMethod) { success ->
            isOrderSaved = success
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFA726)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text("Order Summary", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(cartItems) { item ->
                ReceiptItem(item)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount", style = MaterialTheme.typography.titleMedium)
                    Text("RM ${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Payment Method: $paymentMethod", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                if (isOrderSaved) {
                    Text("Order saved successfully!", style = MaterialTheme.typography.bodyMedium, color = Color.Green)
                } else {
                    Text("Saving order...", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        cartViewModel.clearCart()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
fun ReceiptItem(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(item.cake.name, style = MaterialTheme.typography.titleSmall)
            Text("${item.size} x ${item.quantity}", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "RM ${String.format("%.2f", item.cake.getPrice(item.size) * item.quantity)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun saveOrderToFirebase(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    cartItems: List<CartItem>,
    totalAmount: Double,
    paymentMethod: String,
    onComplete: (Boolean) -> Unit
) {
    val currentUser = auth.currentUser
    if (currentUser != null) {
        val orderData = hashMapOf(
            "userId" to currentUser.uid,
            "userEmail" to currentUser.email,
            "items" to cartItems.map { item ->
                hashMapOf(
                    "cakeName" to item.cake.name,
                    "size" to item.size,
                    "quantity" to item.quantity,
                    "price" to item.cake.getPrice(item.size)
                )
            },
            "totalAmount" to totalAmount,
            "paymentMethod" to paymentMethod,
            "orderDate" to Date()
        )

        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    } else {
        onComplete(false)
    }
}