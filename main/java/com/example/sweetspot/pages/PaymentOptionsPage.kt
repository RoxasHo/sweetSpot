package com.example.sweetspot.pages

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.CartItem
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.models.Order
import com.example.sweetspot.OrderViewModel
import com.example.sweetspot.PaymentViewModel
import com.example.sweetspot.R
import com.example.sweetspot.models.OrderItem
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentOptionsPage(
    navController: NavController,
    totalAmount: Double,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel,
    paymentViewModel: PaymentViewModel,
    paymentResult: MutableState<PaymentViewModel.PaymentResult?>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val paymentsClient = remember {
        Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST) // Change to ENVIRONMENT_PRODUCTION for eating your money
                .build()
        )
    }

    var isGooglePayAvailable by remember { mutableStateOf(false) }

    var showCardPaymentForm by remember { mutableStateOf(false) }

    var cardNumber by remember { mutableStateOf("") }
    var cardNumberError by remember { mutableStateOf<String?>(null) }

    var expiryDate by remember { mutableStateOf("") }
    var expiryDateError by remember { mutableStateOf<String?>(null) }

    var cvv by remember { mutableStateOf("") }
    var cvvError by remember { mutableStateOf<String?>(null) }

    var cardHolderName by remember { mutableStateOf("") }
    var cardHolderNameError by remember { mutableStateOf<String?>(null) }

    val collectionDateTime by cartViewModel.collectionDateTime.collectAsState()

    val userEmail by authViewModel.userEmail.observeAsState()
    val authState by authViewModel.authState.observeAsState()
    val userId = authViewModel.auth.currentUser?.uid

    val loadPaymentDataRequestCode = 991

    val paymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val data = result.data
        val status = data?.let { AutoResolveHelper.getStatusFromIntent(it) }
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val paymentData = data?.let { PaymentData.getFromIntent(it) }
                paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Success("Google Pay")
            }
            Activity.RESULT_CANCELED -> {
                paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Error("Payment canceled")
            }
            else -> {
                val statusMessage = status?.statusMessage ?: "Unknown error"
                paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Error("Payment failed: $statusMessage")
            }
        }
    }

    LaunchedEffect(Unit) {
        val isReadyToPayRequest = IsReadyToPayRequest.fromJson(getIsReadyToPayRequestJson())
        val task = paymentsClient.isReadyToPay(isReadyToPayRequest)
        task.addOnCompleteListener { completedTask ->
            try {
                isGooglePayAvailable = completedTask.getResult(ApiException::class.java) == true
            } catch (exception: ApiException) {
                isGooglePayAvailable = false
            }
        }
    }

    val paymentResult by paymentViewModel.paymentResult

    LaunchedEffect(paymentResult) {
        when (val result = paymentResult) {
            is PaymentViewModel.PaymentResult.Success -> {
                paymentViewModel.paymentResult.value = null
                if (collectionDateTime == null) {
                    Toast.makeText(context, "Please select a collection date and time.", Toast.LENGTH_LONG).show()
                    return@LaunchedEffect
                }
                coroutineScope.launch {
                    saveOrderToFirestore(
                        cartItems = cartViewModel.cartItems.value,
                        totalPrice = totalAmount,
                        collectionDateTime = collectionDateTime,
                        paymentMethod = result.method,
                        userEmail = userEmail,
                        userId = userId,
                        orderViewModel = orderViewModel,
                        onSuccess = {
                            cartViewModel.clearCart()
                            navController.navigate("paymentConfirmation") {
                                popUpTo("userHome") { inclusive = true }
                            }
                            sendOrderConfirmationNotification(context)
                        }
                        ,
                        onFailure = { e ->
                            Toast.makeText(context, "Failed to save order: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
            is PaymentViewModel.PaymentResult.Error -> {
                snackbarHostState.showSnackbar(
                    message = "Payment failed: ${result.message}",
                    duration = SnackbarDuration.Short
                )
                paymentViewModel.paymentResult.value = null
            }
            else -> {
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Payment Option") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
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
                Text(
                    text = "Total Amount: RM ${String.format("%.2f", totalAmount)}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Choose a payment method:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isGooglePayAvailable) {
                    PaymentOptionItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Google Pay",
                        onClick = {
                            startGooglePayPayment(
                                paymentsClient = paymentsClient,
                                totalAmount = totalAmount,
                                paymentLauncher = paymentLauncher
                            )
                        }
                    )
                }
                PaymentOptionItem(
                    icon = Icons.Default.CreditCard,
                    title = "Credit/Debit Card",
                    onClick = { showCardPaymentForm = true }
                )
                PaymentOptionItem(
                    icon = Icons.Default.Money,
                    title = "Pay At Shop",
                    onClick = {
                        paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Success("Pay At Shop")
                    }
                )

                if (showCardPaymentForm) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Enter Card Details",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cardHolderName,
                        onValueChange = {
                            cardHolderName = it
                            cardHolderNameError = if (it.isBlank()) "Card holder name is required" else null
                        },
                        label = { Text("Card Holder Name") },
                        isError = cardHolderNameError != null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (cardHolderNameError != null) {
                        Text(
                            text = cardHolderNameError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            if (it.length <= 16 && it.all { char -> char.isDigit() }) {
                                cardNumber = it
                                cardNumberError = if (it.length < 16) "Card number must be 16 digits" else null
                            }
                        },
                        label = { Text("Card Number") },
                        placeholder = { Text("1234 5678 9012 3456") },
                        isError = cardNumberError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (cardNumberError != null) {
                        Text(
                            text = cardNumberError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { input ->
                                    var digits = input.filter { it.isDigit() }
                                    if (digits.length > 4) digits = digits.take(4)
                                    expiryDate = when {
                                        digits.length >= 3 -> digits.take(2) + "/" + digits.drop(2)
                                        else -> digits
                                    }
                                    expiryDateError = when {
                                        expiryDate.length < 5 -> "Expiry date must be MM/YY"
                                        else -> null
                                    }
                                },
                                label = { Text("Expiry Date (MM/YY)") },
                                placeholder = { Text("MM/YY") },
                                isError = expiryDateError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            if (expiryDateError != null) {
                                Text(
                                    text = expiryDateError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = {
                                    if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                                        cvv = it
                                        cvvError =
                                            if (it.length < 3) "CVV must be 3 digits" else null
                                    } else if (it.isNotEmpty()) {
                                        cvvError = "CVV must be numeric"
                                    }
                                },
                                label = { Text("CVV") },
                                placeholder = { Text("123") },
                                isError = cvvError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            if (cvvError != null) {
                                Text(
                                    text = cvvError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val isFormValid = listOf(
                                cardHolderNameError,
                                cardNumberError,
                                expiryDateError,
                                cvvError
                            ).all { it == null } &&
                                    cardHolderName.isNotBlank() &&
                                    cardNumber.length == 16 &&
                                    expiryDate.length == 5 &&
                                    cvv.length == 3

                            if (isFormValid) {
                                paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Success("Card")
                            } else {
                                Toast.makeText(context, "Please correct the errors in the form", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5F9EA0))
                    ) {
                        Text("Pay RM ${String.format("%.2f", totalAmount)}")
                    }
                }
            }
        }
    )
}

@Composable
fun PaymentOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick), elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun startGooglePayPayment(
    paymentsClient: PaymentsClient,
    totalAmount: Double,
    paymentLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
) {
    val paymentDataRequestJson = getPaymentDataRequestJson(totalAmount)
    val request = PaymentDataRequest.fromJson(paymentDataRequestJson)
    val task = paymentsClient.loadPaymentData(request)
    task.addOnCompleteListener { completedTask ->
        if (completedTask.isSuccessful) {
            val paymentData = completedTask.result
        } else {
            val exception = completedTask.exception
            if (exception is ApiException) {
                val status = exception.status
                if (status.hasResolution()) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(status.resolution!!).build()
                        paymentLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                    }
                } else {
                }
            }
        }
    }
}

suspend fun saveOrderToFirestore(
    cartItems: List<CartItem>,
    totalPrice: Double,
    collectionDateTime: Pair<LocalDate, LocalTime>?,
    paymentMethod: String,
    userEmail: String?,
    userId: String?,
    orderViewModel: OrderViewModel,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val orderItems = cartItems.map { cartItem ->
        OrderItem(
            cakeName = cartItem.cake.name,
            price = cartItem.cake.getPrice(cartItem.size),
            quantity = cartItem.quantity,
            size = cartItem.size
        )
    }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val collectionDate = collectionDateTime?.first?.format(dateFormatter) ?: ""
    val collectionTime = collectionDateTime?.second?.format(timeFormatter) ?: ""

    val order = Order(
        collectionDate = collectionDate,
        collectionTime = collectionTime,
        items = orderItems,
        status = "Pending",
        paymentMethod = paymentMethod,
        totalPrice = totalPrice,
        userEmail = userEmail?: "",
        userId = userId?: "",
        timestamp = System.currentTimeMillis(),
        sendEmail = true,
        emailSent = false
    )

    orderViewModel.saveOrder(order, onSuccess, onFailure)
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun getIsReadyToPayRequestJson(): String {
    val isReadyToPayRequest = JSONObject()
    isReadyToPayRequest.put("apiVersion", 2)
    isReadyToPayRequest.put("apiVersionMinor", 0)

    val allowedPaymentMethods = JSONArray()
    val cardPaymentMethod = JSONObject()
    cardPaymentMethod.put("type", "CARD")

    val parameters = JSONObject()
    parameters.put(
        "allowedAuthMethods",
        JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS")
    )
    parameters.put(
        "allowedCardNetworks",
        JSONArray().put("AMEX").put("DISCOVER").put("JCB").put("MASTERCARD").put("VISA")
    )
    cardPaymentMethod.put("parameters", parameters)

    allowedPaymentMethods.put(cardPaymentMethod)
    isReadyToPayRequest.put("allowedPaymentMethods", allowedPaymentMethods)

    return isReadyToPayRequest.toString()
}

fun getPaymentDataRequestJson(totalAmount: Double): String {
    val paymentDataRequest = JSONObject()
    paymentDataRequest.put("apiVersion", 2)
    paymentDataRequest.put("apiVersionMinor", 0)

    val allowedPaymentMethods = JSONArray()
    val cardPaymentMethod = JSONObject()
    cardPaymentMethod.put("type", "CARD")

    val parameters = JSONObject()
    parameters.put(
        "allowedAuthMethods",
        JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS")
    )
    parameters.put(
        "allowedCardNetworks",
        JSONArray().put("AMEX").put("DISCOVER").put("JCB").put("MASTERCARD").put("VISA")
    )
    parameters.put("billingAddressRequired", true)
    val billingAddressParameters = JSONObject()
    billingAddressParameters.put("format", "FULL")
    parameters.put("billingAddressParameters", billingAddressParameters)
    cardPaymentMethod.put("parameters", parameters)

    val tokenizationSpecification = JSONObject()
    tokenizationSpecification.put("type", "PAYMENT_GATEWAY")
    val tokenizationParameters = JSONObject()
    tokenizationParameters.put("gateway", "example")
    tokenizationParameters.put("gatewayMerchantId", "exampleGatewayMerchantId")
    tokenizationSpecification.put("parameters", tokenizationParameters)
    cardPaymentMethod.put("tokenizationSpecification", tokenizationSpecification)

    allowedPaymentMethods.put(cardPaymentMethod)
    paymentDataRequest.put("allowedPaymentMethods", allowedPaymentMethods)

    val transactionInfo = JSONObject()
    transactionInfo.put("totalPrice", String.format("%.2f", totalAmount))
    transactionInfo.put("totalPriceStatus", "FINAL")
    transactionInfo.put("currencyCode", "MYR")
    paymentDataRequest.put("transactionInfo", transactionInfo)

    val merchantInfo = JSONObject()
    merchantInfo.put("merchantName", "Your Merchant Name")
    paymentDataRequest.put("merchantInfo", merchantInfo)

    return paymentDataRequest.toString()
}

fun sendOrderConfirmationNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, "order_confirmation_channel")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Order Confirmed")
        .setContentText("Your order has been placed successfully.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify(1001, builder.build())
    }
}
