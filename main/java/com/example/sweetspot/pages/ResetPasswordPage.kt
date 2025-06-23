package com.example.sweetspot.pages

import ThemeViewModel
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.ui.theme.LocalThemeViewModel
import com.example.sweetspot.ui.theme.TopNavigationBar
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordPage(
    navController: NavController
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Reset Password",
                navController = navController
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // New Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display error message if there is one
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)  // Add padding around the error message
                )
            }

            // Change Password Button
            Button(
                onClick = {
                    if (password.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Password fields cannot be empty"
                    } else if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                    } else {
                        errorMessage = null
                        // Call the function to update the password
                        updatePassword(password, authViewModel) { success, error ->
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Password changed successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Navigate back to user profile or home
                                navController.navigate("userProfile") {
                                    popUpTo("userProfile") { inclusive = true }
                                }
                            } else {
                                errorMessage = error ?: "Unknown error occurred"
                                Toast.makeText(
                                    context,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA726),
                    contentColor = Color.White
                )
            ) {
                Text("Change Password", fontSize = 18.sp)
            }
        }
    }
}

// Function to update the password
fun updatePassword(
    newPassword: String,
    authViewModel: AuthViewModel,
    onResult: (Boolean, String?) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        currentUser.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After a successful password update, reload the user's data
                    currentUser.reload().addOnCompleteListener { reloadTask ->
                        if (reloadTask.isSuccessful) {
                            // Refresh email in ViewModel
                            authViewModel.refreshUserEmail()
                            onResult(true, null)
                        } else {
                            onResult(false, "Failed to reload user data")
                        }
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Failed to change password")
                }
            }
    } else {
        onResult(false, "No authenticated user")
    }
}

