package com.example.sweetspot

import LocalThemeViewModel
import ThemeViewModel
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.sweetspot.ui.theme.SweetspotTheme
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData

class MainActivity : ComponentActivity() {
    companion object {
        const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    }

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val cakeViewModel: CakeViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val orderHistoryViewModel: OrderHistoryViewModel by viewModels()
    private val salesAnalyticsViewModel: SalesAnalyticsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    val paymentResult = mutableStateOf<PaymentViewModel.PaymentResult?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            SweetspotTheme(isDarkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val themeViewModel: ThemeViewModel = viewModel()
                val cartViewModel: CartViewModel = viewModel()
                val cakeViewModel: CakeViewModel = viewModel()

                CompositionLocalProvider(
                    LocalThemeViewModel provides themeViewModel,
                    LocalAuthViewModel provides authViewModel
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MyAppNavigations(
                            modifier = Modifier.padding(innerPadding),
                            authViewModel = authViewModel,
                            cakeViewModel = cakeViewModel,
                            cartViewModel = cartViewModel,
                            orderViewModel = orderViewModel,
                            orderHistoryViewModel = orderHistoryViewModel,
                            paymentViewModel = paymentViewModel,
                            paymentResult = paymentResult,
                            themeViewModel = themeViewModel,
                            salesAnalyticsViewModel = salesAnalyticsViewModel
                        )
                    }
                }
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = PaymentData.getFromIntent(data!!)
                    paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Success("Google Pay")
                }
                Activity.RESULT_CANCELED -> {
                    paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Error("Payment canceled")
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    paymentViewModel.paymentResult.value = PaymentViewModel.PaymentResult.Error("Payment failed: ${status?.statusMessage}")
                }
            }
        }
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Order Confirmation"
        val descriptionText = "Notifications for order confirmations"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("order_confirmation_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
