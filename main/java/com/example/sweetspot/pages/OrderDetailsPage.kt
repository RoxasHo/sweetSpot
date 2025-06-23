package com.example.sweetspot.pages

import ThemeViewModel
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sweetspot.CartViewModel
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.ui.theme.LocalThemeViewModel
import com.example.sweetspot.ui.theme.TopNavigationBar
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsPage(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var invalidTimeSelected by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val currentTime = LocalTime.now()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Order Details",
                navController = navController
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Select Collection Date and Time",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val currentDate = LocalDate.now()
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            currentDate.year,
                            currentDate.monthValue - 1,
                            currentDate.dayOfMonth
                        )
                        datePicker.datePicker.minDate = System.currentTimeMillis()
                        datePicker.show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                ) {
                    Text(
                        selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Select Date"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedDate != null) {
                            val timePicker = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val selectedLocalTime = LocalTime.of(hourOfDay, minute)

                                    val minTimeToday =
                                        maxOf(currentTime.plusMinutes(30), LocalTime.of(10, 0))
                                    val minTime =
                                        if (selectedDate == today) minTimeToday else LocalTime.of(
                                            10,
                                            0
                                        )
                                    val maxTime = LocalTime.of(21, 0)

                                    if (selectedLocalTime.isAfter(minTime) && selectedLocalTime.isBefore(
                                            maxTime
                                        )
                                    ) {
                                        selectedTime = selectedLocalTime
                                        invalidTimeSelected = false
                                    } else {
                                        invalidTimeSelected = true
                                    }
                                },
                                currentTime.hour,
                                currentTime.minute,
                                false
                            )
                            timePicker.show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                ) {
                    Text(
                        selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                            ?: "Select Time"
                    )
                }

                if (invalidTimeSelected) {
                    Text(
                        "Invalid time. Please select a time at least 30 minutes from now (if today) and between 10 AM and 9 PM.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Button(
                onClick = {
                    if (selectedDate != null && selectedTime != null && !invalidTimeSelected) {
                        cartViewModel.setCollectionDateTime(selectedDate!!, selectedTime!!)
                        navController.navigate("invoice")
                    }
                },
                enabled = selectedDate != null && selectedTime != null && !invalidTimeSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
            ) {
                Text("Proceed to Invoice")
            }
        }
    }
}
