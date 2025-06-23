package com.example.sweetspot

import ThemeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sweetspot.pages.AddCakePage
import com.example.sweetspot.pages.AdminAnalyticsPage
import com.example.sweetspot.pages.AdminHomePage
import com.example.sweetspot.pages.AdminOrderHistoryPage
import com.example.sweetspot.pages.AdminOrderPage
import com.example.sweetspot.pages.AnalysisPage
import com.example.sweetspot.pages.CakeDetailsPage
import com.example.sweetspot.pages.CakeListPage
import com.example.sweetspot.pages.LoginPage
import com.example.sweetspot.pages.ProductManagementPage
import com.example.sweetspot.pages.RegisterPage
import com.example.sweetspot.pages.ResetPasswordPage
import com.example.sweetspot.pages.StartPage
import com.example.sweetspot.pages.UserHomePage
import com.example.sweetspot.pages.UserProfilePage
import com.example.sweetspot.pages.CartPage
import com.example.sweetspot.pages.EditCakePage
import com.example.sweetspot.pages.InvoicePage
import com.example.sweetspot.pages.OrderDetailsPage
import com.example.sweetspot.pages.OrderHistoryPage
import com.example.sweetspot.pages.PaymentConfirmationPage
import com.example.sweetspot.pages.PaymentOptionsPage
import com.example.sweetspot.pages.PaymentPage
import com.example.sweetspot.pages.ReceiptPage
import com.example.sweetspot.ui.theme.LocalThemeViewModel

@Composable
fun MyAppNavigations(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    cakeViewModel: CakeViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    orderHistoryViewModel: OrderHistoryViewModel,
    paymentViewModel: PaymentViewModel,
    paymentResult: MutableState<PaymentViewModel.PaymentResult?>,
    themeViewModel: ThemeViewModel,
    salesAnalyticsViewModel: SalesAnalyticsViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartPage(modifier, navController, authViewModel)
        }
        composable("register") {
            RegisterPage(modifier, navController, authViewModel)
        }
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("userHome") {
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                UserHomePage(navController, cartViewModel, cakeViewModel)
            }
        }
        composable("userProfile") {
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                UserProfilePage(navController,cakeViewModel)
            }
        }
        composable("resetPassword") {
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                ResetPasswordPage(navController)
            }
        }
        composable("cakeList") {
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                CakeListPage(navController, cakeViewModel, cartViewModel)
            }
        }
        composable(
            "cakeDetails/{cakeId}",
            arguments = listOf(navArgument("cakeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cakeId = backStackEntry.arguments?.getString("cakeId") ?: ""
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                CakeDetailsPage(navController, cakeViewModel, cartViewModel, cakeId)
            }
        }
        composable("cart") {
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                CartPage(navController, cartViewModel)
            }
        }
        composable("orderDetails") {
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                OrderDetailsPage(navController, cartViewModel)
            }
        }
        composable("invoice") {
            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                InvoicePage(navController, cartViewModel)
            }
        }
        composable("paymentOptions/{totalAmount}",
            arguments = listOf(navArgument("totalAmount") { type = NavType.FloatType })
        ) { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getFloat("totalAmount")?.toDouble() ?: 0.0
            PaymentOptionsPage(
                navController = navController,
                totalAmount = totalAmount,
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel,
                authViewModel = authViewModel,
                paymentViewModel = paymentViewModel,
                paymentResult = paymentResult
            )
        }

        composable("paymentConfirmation") {
            PaymentConfirmationPage(navController)
        }

        composable("orderHistoryUser") {
            OrderHistoryPage(
                navController = navController
            )
        }
        composable("payment/{totalAmount}") { backStackEntry ->
            val totalAmount =
                backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0
            PaymentPage(navController, cartViewModel, totalAmount)
        }
        composable(
            route = "receipt/{totalAmount}/{paymentMethod}",
            arguments = listOf(
                navArgument("totalAmount") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val totalAmount =
                backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0
            val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: ""
            ReceiptPage(navController, cartViewModel, totalAmount, paymentMethod)
        }


        composable("adminHome") {
            AdminHomePage(modifier, navController, authViewModel)
        }
        composable("productManagement") {
            ProductManagementPage(navController, cakeViewModel)
        }
        composable("addcake") {
            AddCakePage(navController, cakeViewModel)
        }
        composable("editcake/{cakeId}") { backStackEntry ->
            val cakeId = backStackEntry.arguments?.getString("cakeId") ?: ""
            EditCakePage(
                cakeId = cakeId,
                navController = navController,
                cakeViewModel = cakeViewModel
            )
        }
        composable("orderHistoryAdmin") {
            AdminOrderHistoryPage(
                navController = navController
            )
        }
        composable("adminAnalyticsPage") {
            AdminAnalyticsPage(navController)
        }

    }
}
