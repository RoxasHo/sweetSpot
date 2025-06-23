package com.example.sweetspot.pages

import LocalThemeViewModel
import ThemeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.CakeViewModel
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.models.Cake
import com.example.sweetspot.ui.theme.TopNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementPage(
    navController: NavController,
    cakeViewModel: CakeViewModel
) {
    val themeViewModel = LocalThemeViewModel.current
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cakeViewModel.fetchCakes()
    }

    val cakeList by cakeViewModel.cakeList.observeAsState(emptyList())

    val categories = listOf("All Categories", "Cream Cake", "Ice Cream Cake", "Sponge Cake", "Butter Cake", "Cheesecake", "Fruit Cake", "Chocolate Cake", "Red Velvet Cake", "Carrot Cake", "Mousse Cake", "Pound Cake", "Tiramisu", "Chiffon Cake", "Others")

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Product Management",
                navController = navController
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        modifier = Modifier.navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 56.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(text = "Search by Cake Name") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFFFA726),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.ManageSearch,
                        contentDescription = "Manage Search Icon"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = selectedCategory)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { navController.navigate("addcake") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Add Cake")
            }

            val filteredCakes = cakeList.filter { cake ->
                (selectedCategory == "All Categories" || cake.category == selectedCategory) &&
                        (searchQuery.isBlank() || cake.name.contains(
                            searchQuery,
                            ignoreCase = true
                        ))
            }.sortedByDescending { it.orders }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (filteredCakes.isEmpty()) {
                    item {
                        Text(text = "No cakes available", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(filteredCakes) { cake ->
                        CakeTableRow(cake, navController, cakeViewModel)
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun CakeTableRow(cake: Cake, navController: NavController, cakeViewModel: CakeViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(cake.imageUrl),
            contentDescription = "Cake Image",
            modifier = Modifier
                .size(64.dp)
                .padding(end = 8.dp),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = cake.name, fontSize = 16.sp)
            Text(text = "Small: RM${cake.priceSmall},", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Medium: RM${cake.priceMedium},", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Large: RM${cake.priceLarge}", fontSize = 14.sp, color = Color.Gray)
            Text(text = cake.category, fontSize = 14.sp, color = Color.Gray)

        }

        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                navController.navigate("editcake/${cake.id}")
            }) {
                Text(text = "Edit")
            }

            Button(
                onClick = {
                    cakeViewModel.softDeleteCake(cake.id)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "Delete")
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        containerColor = Color(0xFFFFA726),
        modifier = Modifier
            .height(56.dp)
            .navigationBarsPadding()
    ) {
        NavigationBarItem(
            icon = { Text("🏠") },
            label = { Text("Home") },
            selected = false,
            onClick = { navController.navigate("adminhome") }
        )
        NavigationBarItem(
            icon = { Text("📋") },
            label = { Text("Orders") },
            selected = false,
            onClick = { navController.navigate("orderHistoryAdmin") }
        )
        NavigationBarItem(
            icon = { Text("📊") },
            label = { Text("Analytics") },
            selected = false,
            onClick = { navController.navigate("adminAnalyticsPage") }
        )
    }
}
