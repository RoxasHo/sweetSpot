package com.example.sweetspot.pages

import ThemeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sweetspot.CakeViewModel
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.models.Cake
import com.example.sweetspot.ui.theme.LocalThemeViewModel
import com.example.sweetspot.ui.theme.TopNavigationBar
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CakeDetailsPage(
    navController: NavController,
    cakeViewModel: CakeViewModel,
    cartViewModel: CartViewModel,
    cakeId: String
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    val cake by cakeViewModel.getCakeById(cakeId).collectAsState(initial = null)
    val user = FirebaseAuth.getInstance().currentUser
    var selectedSize by remember { mutableStateOf("6 inch") }
    var quantity by remember { mutableIntStateOf(1) }
    val isFavorite by remember { derivedStateOf { cakeViewModel.getFavoriteStatus(cakeId) } }

    LaunchedEffect(user, cakeId) {
        user?.let { cakeViewModel.observeFavoriteStatus(it.uid, cakeId) }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Cake Details",
                navController = navController
            )
        }
    ) { innerPadding ->
        cake?.let { cakeDetails ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            Image(
                                painter = rememberAsyncImagePainter(cakeDetails.imageUrl),
                                contentDescription = cakeDetails.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cakeDetails.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFA726)
                                    )
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFavorite) Color.Red else Color.Gray,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable {
                                                user?.let {
                                                    cakeViewModel.toggleFavoriteStatus(
                                                        it.uid,
                                                        cakeId
                                                    )
                                                }
                                            }
                                    )
                                }
                                Text(
                                    text = "RM ${ String.format("%.2f", getPrice(cakeDetails, selectedSize))}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = "Description",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                Text(
                                    text = cakeDetails.description,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = "Size",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SizeButton("6 inch", selectedSize) {
                                        selectedSize = "6 inch"
                                    }
                                    SizeButton("8 inch", selectedSize) {
                                        selectedSize = "8 inch"
                                    }
                                    SizeButton("10 inch", selectedSize) {
                                        selectedSize = "10 inch"
                                    }
                                }
                                Text(
                                    text = "Quantity",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Decrease"
                                        )
                                    }
                                    Text(
                                        text = quantity.toString(),
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        fontSize = 20.sp
                                    )
                                    IconButton(
                                        onClick = { quantity++ },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Increase"
                                        )

                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                Button(
                    onClick = {
                        cartViewModel.addToCart(cakeDetails, selectedSize, quantity)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                ) {
                    Text("Add To Cart")
                }
            }
        }
    }
}

@Composable
fun SizeButton(size: String, selectedSize: String, onSelect: () -> Unit) {
    Button(
        onClick = onSelect,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (size == selectedSize) Color(0xFFFFA726) else Color.LightGray
        ),
        modifier = Modifier.width(100.dp)
    ) {
        Text(size)
    }
}

fun getPrice(cake: Cake, size: String): Double {
    return when (size) {
        "6 inch" -> cake.priceSmall
        "8 inch" -> cake.priceMedium
        "10 inch" -> cake.priceLarge
        else -> cake.priceSmall
    }
}