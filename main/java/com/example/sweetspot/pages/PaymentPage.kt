package com.example.sweetspot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.sweetspot.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPage(
    navController: NavController,
    cartViewModel: CartViewModel,
    totalAmount: Double
) {
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var eWalletApp by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Amount", style = MaterialTheme.typography.titleLarge)
            Text("RM ${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Payment Option", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PaymentOptionButton("Debit/Credit", selectedPaymentMethod) { selectedPaymentMethod = "Debit/Credit" }
                PaymentOptionButton("E-Banking", selectedPaymentMethod) { selectedPaymentMethod = "E-Banking" }
                PaymentOptionButton("E-Wallet", selectedPaymentMethod) { selectedPaymentMethod = "E-Wallet" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedPaymentMethod) {
                "E-Banking" -> {
                    OutlinedTextField(
                        value = bank,
                        onValueChange = { bank = it },
                        label = { Text("Select Bank") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "E-Wallet" -> {
                    OutlinedTextField(
                        value = eWalletApp,
                        onValueChange = { eWalletApp = it },
                        label = { Text("Select E-Wallet App") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    navController.navigate("receipt/${totalAmount}/${selectedPaymentMethod}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
            ) {
                Text("Proceed Payment")
            }
        }
    }
}

@Composable
fun PaymentOptionButton(
    text: String,
    selectedOption: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (text == selectedOption) Color(0xFFFFA726) else Color.White,
            contentColor = if (text == selectedOption) Color.White else Color.Black
        ),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Text(text)
    }
}