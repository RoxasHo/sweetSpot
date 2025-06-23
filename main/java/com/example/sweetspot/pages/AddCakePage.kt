package com.example.sweetspot.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sweetspot.CakeViewModel
import com.example.sweetspot.CakeState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCakePage(
    navController: NavController,
    cakeViewModel: CakeViewModel
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceSmall by remember { mutableStateOf("") }
    var priceMedium by remember { mutableStateOf("") }
    var priceLarge by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Select a cake category") }
    var expanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val categories = listOf(
        "Cream Cake", "Ice Cream Cake", "Sponge Cake", "Butter Cake",
        "Cheesecake", "Fruit Cake", "Chocolate Cake", "Red Velvet Cake",
        "Carrot Cake", "Mousse Cake", "Pound Cake", "Tiramisu", "Chiffon Cake", "Others"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val cakeState by cakeViewModel.cakeState.observeAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Cake", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        cakeViewModel.resetState()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFA726)
                )
            )
        },
        modifier = Modifier.navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Cake Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = "Cake Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceSmall,
                onValueChange = { priceSmall = it },
                label = { Text(text = "Small (6 inches) - RM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceMedium,
                onValueChange = { priceMedium = it },
                label = { Text(text = "Medium (8 inches) - RM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceLarge,
                onValueChange = { priceLarge = it },
                label = { Text(text = "Large (10 inches) - RM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .clickable { expanded = true }) {
                Text(
                    text = category,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    color = Color.Gray,
                    fontSize = 16.sp
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, fontSize = 16.sp) },
                            onClick = {
                                category = cat
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (imageUri != null) "Change Image" else "Select Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)  // Make the image larger
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)  // Center the image
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val priceSmallDouble = priceSmall.toDoubleOrNull()
                    val priceMediumDouble = priceMedium.toDoubleOrNull()
                    val priceLargeDouble = priceLarge.toDoubleOrNull()

                    if (name.isNotBlank() && description.isNotBlank() && priceSmallDouble != null && priceMediumDouble != null && priceLargeDouble != null && category != "Select a cake category") {
                        val imageName = "$name.png"
                        cakeViewModel.addCake(
                            name = name,
                            description = description,
                            priceSmall = priceSmallDouble,
                            priceMedium = priceMediumDouble,
                            priceLarge = priceLargeDouble,
                            category = category,
                            imageUri = imageUri,
                            imageName = imageName
                        )
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add Cake")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (cakeState) {
                is CakeState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is CakeState.Success -> {
                    Toast.makeText(context, (cakeState as CakeState.Success).message, Toast.LENGTH_SHORT).show()
                    cakeViewModel.resetState()
                    name = ""
                    description = ""
                    priceSmall = ""
                    priceMedium = ""
                    priceLarge = ""
                    category = "Select a cake category"
                    imageUri = null
                    navController.navigate("productManagement") {
                        popUpTo("addcake") { inclusive = true }
                    }
                }
                is CakeState.Error -> {
                    Toast.makeText(context, (cakeState as CakeState.Error).message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
