package com.example.sweetspot.pages

import ThemeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
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
import com.example.sweetspot.ui.theme.TopBarWithMenu
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CakeListPage(
    navController: NavController,
    cakeViewModel: CakeViewModel,
    cartViewModel: CartViewModel,
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val cakeList by cakeViewModel.cakeList.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        cakeViewModel.fetchCakes()
    }

    Scaffold(
        topBar = {
            TopBarWithMenu(
                navController = navController,
                cartViewModel = cartViewModel,
                title = "Cake Lists",
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
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(text = "Search by Name or Category") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filteredCakeList = cakeList.filter { cake ->
                cake.name.contains(searchQuery, ignoreCase = true) ||
                        cake.category.contains(searchQuery, ignoreCase = true)
            }

            if (filteredCakeList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    items(filteredCakeList) { cake ->
                        CakeListItem(
                            cake = cake,
                            cakeViewModel = cakeViewModel,
                            onClick = {
                                navController.navigate("cakeDetails/${cake.id}")
                            }
                        )
                    }
                }
            } else {
                Text(text = "No cakes available.", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun CakeListItem(cake: Cake, cakeViewModel: CakeViewModel, onClick: () -> Unit) {
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
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    user?.let { cakeViewModel.toggleFavoriteStatus(it.uid, cake.id) }
                }
        )
    }
}
