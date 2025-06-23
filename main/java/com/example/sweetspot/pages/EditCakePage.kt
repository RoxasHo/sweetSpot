package com.example.sweetspot.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCakePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    cakeViewModel: CakeViewModel,
    cakeId: String
) {
    val cake by cakeViewModel.getCakeById(cakeId).collectAsState(initial = null)

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceSmall by remember { mutableStateOf("") }
    var priceMedium by remember { mutableStateOf("") }
    var priceLarge by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Select a cake category") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var priceSmallError by remember { mutableStateOf<String?>(null) }
    var priceMediumError by remember { mutableStateOf<String?>(null) }
    var priceLargeError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

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

    cake?.let {
        name = it.name
        description = it.description
        priceSmall = it.priceSmall.toString()
        priceMedium = it.priceMedium.toString()
        priceLarge = it.priceLarge.toString()
        category = it.category
        imageUri = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Cake", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFA726))
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } ?: cake?.imageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (imageUri != null) "Change Image" else "Select Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Cake Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null
            )
            nameError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Cake Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                isError = descriptionError != null
            )
            descriptionError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceSmall,
                onValueChange = { priceSmall = it },
                label = { Text("Small (6 inches) - RM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = priceSmallError != null
            )
            priceSmallError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceMedium,
                onValueChange = { priceMedium = it },
                label = { Text("Medium (8 inches) - RM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = priceMediumError != null
            )
            priceMediumError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceLarge,
                onValueChange = { priceLarge = it },
                label = { Text("Large (10 inches) - RM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = priceLargeError != null
            )
            priceLargeError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = category)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expanded = false
                            }
                        )
                    }
                }
            }
            categoryError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val priceSmallDouble = priceSmall.toDoubleOrNull()
                    val priceMediumDouble = priceMedium.toDoubleOrNull()
                    val priceLargeDouble = priceLarge.toDoubleOrNull()

                    var isValid = true
                    if (name.isBlank()) {
                        nameError = "Please enter a valid cake name"
                        isValid = false
                    }
                    if (description.isBlank()) {
                        descriptionError = "Please enter a description"
                        isValid = false
                    }
                    if (priceSmallDouble == null || priceSmallDouble <= 0) {
                        priceSmallError = "Enter a valid price for small cake"
                        isValid = false
                    }
                    if (priceMediumDouble == null || priceMediumDouble <= 0) {
                        priceMediumError = "Enter a valid price for medium cake"
                        isValid = false
                    }
                    if (priceLargeDouble == null || priceLargeDouble <= 0) {
                        priceLargeError = "Enter a valid price for large cake"
                        isValid = false
                    }
                    if (category == "Select a cake category") {
                        categoryError = "Please select a valid category"
                        isValid = false
                    }

                    if (isValid) {
                        cakeViewModel.updateCake(
                            cakeId = cakeId,
                            name = name,
                            description = description,
                            priceSmall = priceSmallDouble ?: 0.0,
                            priceMedium = priceMediumDouble ?: 0.0,
                            priceLarge = priceLargeDouble ?: 0.0,
                            category = category,
                            newImageUri = imageUri
                        )
                        navController.navigate("productManagement") {
                            popUpTo("editcake/$cakeId") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Save Changes")
            }
        }
    }
}
