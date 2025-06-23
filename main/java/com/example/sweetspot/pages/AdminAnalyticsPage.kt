package com.example.sweetspot.pages

import LocalThemeViewModel
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetspot.SalesAnalyticsViewModel
import com.example.sweetspot.CakeSalesData
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.navigation.NavController
import java.time.Year
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sweetspot.LocalAuthViewModel
import com.example.sweetspot.models.RevenueData
import com.example.sweetspot.ui.theme.TopNavigationBar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.Month

@Composable
fun AdminAnalyticsPage(
    navController: NavController,
) {
    val authViewModel = LocalAuthViewModel.current
    val themeViewModel = LocalThemeViewModel.current
    val viewModel: SalesAnalyticsViewModel = viewModel()

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val hotSellingCakes by viewModel.hotSellingCakes.collectAsState(emptyList())
    val revenueData by viewModel.revenueData.collectAsState(emptyList())

    var selectedYear by remember { mutableStateOf(Year.now().value) }
    var selectedTimeFrame by remember { mutableStateOf("Monthly") }
    var selectedMonth by remember { mutableStateOf(java.time.Month.JANUARY) }

    LaunchedEffect(selectedTimeFrame, selectedYear, selectedMonth) {
        when (selectedTimeFrame) {
            "Daily" -> viewModel.fetchRevenueData(selectedYear, selectedMonth, selectedTimeFrame)
            "Monthly" -> viewModel.fetchRevenueData(selectedYear, null, selectedTimeFrame)
            "Yearly" -> viewModel.fetchRevenueData(null, null, selectedTimeFrame)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchHotSellingCakes()
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = "Analytics",
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
                .padding(16.dp)
        ) {
            Text(
                text = "Sales Analytics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "Select Time Frame: ")
                Spacer(modifier = Modifier.width(8.dp))
                TimeFrameDropdownMenu(selectedTimeFrame, onTimeFrameSelected = { selectedTimeFrame = it })
            }

            if (selectedTimeFrame == "Daily") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Select Year: ")
                        Spacer(modifier = Modifier.width(8.dp))
                        YearDropdownMenu(selectedYear, onYearSelected = { selectedYear = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Select Month: ")
                        Spacer(modifier = Modifier.width(8.dp))
                        MonthDropdownMenu(selectedMonth, onMonthSelected = { selectedMonth = it })
                    }
                }
            }
            else if (selectedTimeFrame == "Monthly") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Select Year: ")
                    Spacer(modifier = Modifier.width(8.dp))
                    YearDropdownMenu(selectedYear, onYearSelected = { selectedYear = it })
                }
            }
            Log.d("AdminAnalyticsPage", "Fetching revenue data for time frame: $selectedTimeFrame")

            Text(
                text = "Revenue by $selectedTimeFrame",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            RevenueGraph(revenueData, selectedTimeFrame)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Top Selling Cakes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HotSellingCakesTable(hotSellingCakes)
            }
        }
    }

@Composable
fun RevenueGraph(revenueData: List<RevenueData>, timeFrame: String) {
    if (revenueData.isEmpty()) {
        Text(
            text = "No revenue data available.",
            color = MaterialTheme.colorScheme.onBackground
        )
        return
    }

    val (entries, xAxisLabels) = prepareChartData(revenueData, timeFrame)

    val dataSet = LineDataSet(entries, "Revenue").apply {
        color = MaterialTheme.colorScheme.primary.toArgb()
        valueTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
        setCircleColor(MaterialTheme.colorScheme.primary.toArgb())
        circleHoleColor = MaterialTheme.colorScheme.background.toArgb()
        lineWidth = 2f
        circleRadius = 4f
        setDrawValues(false)
    }

    val lineData = LineData(dataSet)
    val surfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground.toArgb()

    Log.d("prepareChartData", "Time Frame: $timeFrame, Revenue Data: $revenueData")

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.textColor = surfaceColor
                axisLeft.textColor = surfaceColor
                axisRight.isEnabled = false
                legend.isEnabled = false
                setNoDataTextColor(onBackgroundColor)
                setBackgroundColor(backgroundColor)
            }
        },
        update = { chart ->
            chart.data = lineData
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return xAxisLabels[value] ?: value.toInt().toString()
                }
            }
            chart.xAxis.labelCount = xAxisLabels.size
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}


@Composable
fun YearDropdownMenu(selectedYear: Int, onYearSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = Year.now().value
    val years = (currentYear downTo currentYear - 5).toList()

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("$selectedYear")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    },
                    text = {
                        Text(text = "$year")
                    }
                )
            }
        }
    }
}

@Composable
fun TimeFrameDropdownMenu(selectedTimeFrame: String, onTimeFrameSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Daily", "Monthly", "Yearly")

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(selectedTimeFrame)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onTimeFrameSelected(option)
                        expanded = false
                    },
                    text = {
                        Text(text = option)
                    }
                )
            }
        }
    }
}

@Composable
fun HotSellingCakesTable(cakes: List<CakeSalesData>) {
    LazyColumn {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cake Name",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Total Sold",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        items(cakes) { cake ->
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = cake.cakeName,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = cake.totalQuantity.toString(),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun MonthDropdownMenu(selectedMonth: Month, onMonthSelected: (Month) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val months = Month.entries.toTypedArray()

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(selectedMonth.name.capitalize())
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    },
                    text = {
                        Text(text = month.name.capitalize())
                    }
                )
            }
        }
    }
}

private fun parseLabelToFloat(label: String): Float {
    return label.toFloatOrNull() ?: 0f
}

private fun prepareChartData(
    revenueData: List<RevenueData>,
    timeFrame: String
): Pair<List<Entry>, Map<Float, String>> {
    return when (timeFrame) {
        "Daily" -> {
            val entries = revenueData.mapNotNull { data ->
                val xValue = data.label.toFloatOrNull()
                if (xValue != null) {
                    Entry(xValue, data.revenue.toFloat())
                } else {
                    Log.e("prepareChartData", "Invalid day label: ${data.label}")
                    null
                }
            }
            val labels = entries.associate { entry ->
                entry.x to entry.x.toInt().toString()
            }
            entries to labels
        }
        "Monthly" -> {
            val monthMap = mapOf(
                "January" to 1f, "February" to 2f, "March" to 3f, "April" to 4f,
                "May" to 5f, "June" to 6f, "July" to 7f, "August" to 8f,
                "September" to 9f, "October" to 10f, "November" to 11f, "December" to 12f
            )
            val entries = revenueData.mapNotNull { data ->
                val xValue = monthMap[data.label]
                if (xValue != null) {
                    Entry(xValue, data.revenue.toFloat())
                } else {
                    Log.e("prepareChartData", "Invalid month label: ${data.label}")
                    null
                }
            }
            val labels = monthMap.entries.associate { (month, xValue) ->
                xValue to month.substring(0, 3)
            }
            entries to labels
        }
        "Yearly" -> {
            val entries = revenueData.mapNotNull { data ->
                val xValue = data.label.toFloatOrNull()
                if (xValue != null) {
                    Entry(xValue, data.revenue.toFloat())
                } else {
                    Log.e("prepareChartData", "Invalid year label: ${data.label}")
                    null
                }
            }
            val labels = entries.associate { entry ->
                entry.x to entry.x.toInt().toString()
            }
            entries to labels
        }
        else -> emptyList<Entry>() to emptyMap()
    }
}


@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}





