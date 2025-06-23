package com.example.sweetspot.pages

import ThemeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.CakeViewModel
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.models.Cake
import com.example.sweetspot.ui.theme.LocalThemeViewModel
import com.example.sweetspot.ui.theme.TopBarWithMenu
import com.example.sweetspot.ui.theme.TopNavigationBar
import kotlinx.coroutines.launch

@Composable
fun UserHomePage(
    navController: NavController,
    cartViewModel: CartViewModel,
    cakeViewModel: CakeViewModel,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        cakeViewModel.fetchCakes()
    }

    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val cakeList by cakeViewModel.cakeList.observeAsState(emptyList())

    val latestCakes = cakeList.sortedByDescending { it.id }.take(3)
    val categories = cakeList.map { it.category }.distinct()

    val categoryOffsets = remember { mutableStateMapOf<String, Int>() }


    Scaffold(
        topBar = {
            TopBarWithMenu(
                title = "SweetSpot",
                cartViewModel = cartViewModel,
                navController = navController
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
                .verticalScroll(scrollState)
                .padding(bottom = 56.dp)
        ) {
            WelcomeSection()

            Spacer(modifier = Modifier.height(24.dp))

            RecommendationsSection(latestCakes, navController)

            Spacer(modifier = Modifier.height(24.dp))

            CategoriesHeader()

            CategoryButtons(
                categories = categories,
                scrollState = scrollState,
                categoryOffsets = categoryOffsets
            )

            Spacer(modifier = Modifier.height(24.dp))

            CategoriesSection(
                categories = categories,
                cakeList = cakeList,
                navController = navController,
                categoryOffsets = categoryOffsets
            )
        }
    }
}

@Composable
fun WelcomeSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8BBD0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Discover Delicious Cakes \uD83C\uDF82",
            fontSize = 24.sp,
            color = Color.White
        )
        Text(
            text = "Order your favorite cakes and treats.",
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
fun RecommendationsSection(cakes: List<Cake>, navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Latest Additions",
            fontSize = 24.sp
        )

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            cakes.forEach { cake ->
                RecommendedCakeCard(cake = cake, onClick = {
                    navController.navigate("cakeDetails/${cake.id}")
                })
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
fun RecommendedCakeCard(cake: Cake, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(cake.imageUrl),
                contentDescription = cake.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = cake.name,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "RM${"%.2f".format(cake.priceMedium)}",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun CategoriesHeader() {
    Text(
        text = "Categories",
        fontSize = 26.sp,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
    )
}

@Composable
fun CategoryButtons(categories: List<String>, scrollState: ScrollState, categoryOffsets: Map<String, Int>) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        categories.forEach { category ->
            Button(
                onClick = {
                    coroutineScope.launch {
                        categoryOffsets[category]?.let { offset ->
                            scrollState.animateScrollTo(offset)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = category,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<String>,
    cakeList: List<Cake>,
    navController: NavController,
    categoryOffsets: MutableMap<String, Int>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        categories.forEach { category ->
            val cakesInCategory = cakeList.filter { it.category == category }
            if (cakesInCategory.isNotEmpty()) {
                var categoryPosition by remember { mutableStateOf(0) }

                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            categoryPosition = coordinates.positionInWindow().y.toInt()
                            categoryOffsets[category] = categoryPosition
                        }
                ) {
                    CategoryHeader(category = category)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    cakesInCategory.forEach { cake ->
                        RecommendedCakeCard(cake = cake, onClick = {
                            navController.navigate("cakeDetails/${cake.id}")
                        })
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CategoryHeader(category: String) {
    Text(
        text = category,
        fontSize = 22.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(
    title: String,
    navController: NavController
) {
    val backgroundColor = Color(0xFFFFA726)
    val contentColor = Color.Black

    TopAppBar(
        title = { Text(title, fontSize = 20.sp, color = contentColor) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = contentColor)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor
        )
    )
}

@Composable
fun NavigationBar(navController: NavController) {
    NavigationBar(
        containerColor = Color(0xFFFFA726),
        modifier = Modifier.height(56.dp)
    ) {
        NavigationBarItem(
            icon = { Text("🏠") },
            label = { Text("Home", color = Color.Black) },
            selected = false,
            onClick = { navController.navigate("userHome") }
        )
        NavigationBarItem(
            icon = { Text("🎂") },
            label = { Text("Cakes", color = Color.Black) },
            selected = false,
            onClick = { navController.navigate("cakeList") }
        )
        NavigationBarItem(
            icon = { Text("🛒") },
            label = { Text("Cart", color = Color.Black) },
            selected = false,
            onClick = { navController.navigate("cart") }
        )
        NavigationBarItem(
            icon = { Text("📦") },
            label = { Text("Order", color = Color.Black) },
            selected = false,
            onClick = { navController.navigate("orderHistoryUser") }
        )
    }
}
