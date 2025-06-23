package com.example.sweetspot.pages

import ThemeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.CakeViewModel
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.models.Cake
import com.example.sweetspot.ui.theme.LocalThemeViewModel
import com.example.sweetspot.ui.theme.TopNavigationBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun UserProfilePage(
    navController: NavController,
    cakeViewModel: CakeViewModel,
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    val userEmail by authViewModel.userEmail.observeAsState()
    val user = FirebaseAuth.getInstance().currentUser
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.getUserDetails { name, phone ->
            fullName = name
            phoneNumber = phone
        }
    }

    val coroutineScope = rememberCoroutineScope()

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
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "User Icon",
                modifier = Modifier
                    .size(100.dp)
                    .padding(top = 16.dp),
                tint = Color.Gray
            )

            userEmail?.let { email ->
                Text(
                    text = "Email: $email",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } ?: Text(
                text = "No email found",
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            authViewModel.updateUserDetails(fullName, phoneNumber)
                            isEditing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Save", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isEditing = false }) {
                    Text(text = "Cancel", color = Color.Gray)
                }

            } else {
                Text(
                    text = "Full Name: $fullName",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = "Phone Number: $phoneNumber",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA726),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Edit Details", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { navController.navigate("resetPassword") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA726),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Reset Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            user?.let {
                FavoriteCakeList(
                    userId = it.uid,
                    cakeViewModel = cakeViewModel,
                    navController = navController
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FavoriteCakeList(userId: String, cakeViewModel: CakeViewModel, navController: NavController) {
    val cakeList by cakeViewModel.cakeList.observeAsState(emptyList())
    val favoriteCakes = remember { mutableStateListOf<Cake>() }

    LaunchedEffect(Unit) {
        cakeViewModel.fetchCakes()

        val favoriteCakeIds = cakeViewModel.fetchFavoriteCakes(userId)
        favoriteCakes.clear()
        favoriteCakes.addAll(cakeList.filter { it.id in favoriteCakeIds })
    }

    if (favoriteCakes.isNotEmpty()) {
        Text(text = "Favorite Cakes", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(favoriteCakes) { cake ->
                FavoriteCakeListItem(cake = cake, cakeViewModel = cakeViewModel) {
                    navController.navigate("cakeDetails/${cake.id}")
                }
            }
        }
    } else {
        Text(text = "No favorite cakes found.", fontSize = 18.sp, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun FavoriteCakeListItem(cake: Cake, cakeViewModel: CakeViewModel, onClick: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val isFavorite by remember { derivedStateOf { cakeViewModel.getFavoriteStatus(cake.id) } }

    LaunchedEffect(user, cake.id) {
        user?.let { cakeViewModel.observeFavoriteStatus(it.uid, cake.id) }
    }

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(cake.imageUrl),
            contentDescription = cake.name,
            modifier = Modifier.size(100.dp)
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(text = cake.name, fontSize = 18.sp)
            Text(
                text = "RM ${String.format("%.2f", cake.priceSmall)} - Small",
                fontSize = 14.sp
            )
            Text(
                text = "RM ${String.format("%.2f", cake.priceMedium)} - Medium",
                fontSize = 14.sp
            )
            Text(
                text = "RM ${String.format("%.2f", cake.priceLarge)} - Large",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(0.1f))

        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color.Red else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}
