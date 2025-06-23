package com.example.sweetspot.ui.theme

import LocalThemeViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.LocalAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    title: String,
    navController: NavController
) {
    val themeViewModel = LocalThemeViewModel.current
    val authViewModel = LocalAuthViewModel.current
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title, fontSize = 20.sp) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode"
                        )
                    },
                    onClick = {
                        themeViewModel.toggleTheme()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Log Out") },
                    onClick = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                        showMenu = false
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithMenu(
    navController: NavController,
    cartViewModel: CartViewModel,
    title: String,
) {
    val themeViewModel = LocalThemeViewModel.current
    val authViewModel = LocalAuthViewModel.current
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title, fontSize = 20.sp) },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Color.Black)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("My Profile") },
                    onClick = {
                        navController.navigate("userProfile")
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode"
                        )
                    },
                    onClick = {
                        themeViewModel.toggleTheme()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Log Out") },
                    onClick = {
                        cartViewModel.clearCart()
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                        showMenu = false
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFA726))
    )
}
