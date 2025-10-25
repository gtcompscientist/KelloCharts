package co.csadev.kellocharts.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.compose.LineChart
import co.csadev.kellocharts.compose.common.ChartTitle
import co.csadev.kellocharts.compose.gesture.GestureConfig
import co.csadev.kellocharts.model.*

/**
 * Demo screen for Line Chart with positive and negative values.
 * Demonstrates profit/loss visualization with filled areas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodBadChartScreen(
    onNavigateBack: () -> Unit
) {
    var selectedValue by remember { mutableStateOf<SelectedValue?>(null) }

    // Generate sample data representing profit/loss over time
    val lineChartData = remember {
        LineChartData(
            lines = listOf(
                Line(
                    values = listOf(
                        PointValue(0f, 2f),
                        PointValue(1f, 3f),
                        PointValue(2f, 1f),
                        PointValue(3f, -1f),
                        PointValue(4f, -2f),
                        PointValue(5f, -1.5f),
                        PointValue(6f, 0.5f),
                        PointValue(7f, 2f),
                        PointValue(8f, 3.5f),
                        PointValue(9f, 2.5f),
                        PointValue(10f, 4f),
                        PointValue(11f, 3f)
                    ),
                    // Green for positive (above baseline), Red for negative (below baseline)
                    color = Color(0xFF2E7D32).toArgb(), // Dark green
                    isCubic = true,
                    isFilled = true,
                    hasPoints = true,
                    hasLabels = false,
                    strokeWidth = 3,
                    pointRadius = 4
                )
            ),
            baseValue = 0f // Zero baseline
        ).apply {
            axisXBottom = Axis().apply {
                name = "Month"
            }
            axisYLeft = Axis().apply {
                name = "Profit/Loss ($M)"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profit & Loss Chart") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ChartTitle(
                title = "Monthly Profit & Loss",
                subtitle = "Last 12 months"
            )

            LineChart(
                data = lineChartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                gestureConfig = GestureConfig(
                    zoomEnabled = true,
                    scrollEnabled = true,
                    selectionEnabled = true
                ),
                animate = true,
                onValueSelected = { value ->
                    selectedValue = value
                },
                onValueDeselected = {
                    selectedValue = null
                }
            )

            // Summary cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Positive months card
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFC8E6C9), // Light green
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Profit Months",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1B5E20) // Dark green
                        )
                        Text(
                            text = "8 of 12",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                // Negative months card
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFFCDD2), // Light red
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Loss Months",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB71C1C) // Dark red
                        )
                        Text(
                            text = "4 of 12",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
            }

            // Info card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "About This Chart",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "This chart demonstrates visualization of positive and negative values with filled areas. " +
                                "Values above the baseline (0) represent profit, while values below represent loss.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Selected value display
            selectedValue?.let { value ->
                val point = lineChartData.lines[0].values[value.secondIndex]
                val isProfit = point.y >= 0
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isProfit) {
                        Color(0xFFC8E6C9) // Light green
                    } else {
                        Color(0xFFFFCDD2) // Light red
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (isProfit) "Profit Month" else "Loss Month",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isProfit) {
                                Color(0xFF1B5E20)
                            } else {
                                Color(0xFFB71C1C)
                            }
                        )
                        Text(
                            text = "Month ${point.x.toInt() + 1}: ${if (isProfit) "+" else ""}${String.format("%.1f", point.y)}M",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isProfit) {
                                Color(0xFF1B5E20)
                            } else {
                                Color(0xFFB71C1C)
                            }
                        )
                    }
                }
            }
        }
    }
}
