package co.csadev.kellocharts.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.compose.LineChart
import co.csadev.kellocharts.compose.common.ChartTitle
import co.csadev.kellocharts.compose.gesture.GestureConfig
import co.csadev.kellocharts.compose.theme.LocalChartColors
import co.csadev.kellocharts.model.*

/**
 * Demo screen for LineChart composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineChartScreen(
    onNavigateBack: () -> Unit
) {
    val chartColors = LocalChartColors.current
    var selectedValue by remember { mutableStateOf<SelectedValue?>(null) }

    // Generate sample data
    val lineChartData = remember {
        LineChartData(
            lines = listOf(
                Line(
                    values = listOf(
                        PointValue(0f, 2f),
                        PointValue(1f, 4f),
                        PointValue(2f, 3f),
                        PointValue(3f, 5f),
                        PointValue(4f, 4.5f),
                        PointValue(5f, 6f),
                        PointValue(6f, 5.5f),
                        PointValue(7f, 7f)
                    ),
                    color = chartColors.lineColors[0].toArgb(),
                    isCubic = true,
                    isFilled = true,
                    hasPoints = true,
                    hasLabels = false
                ),
                Line(
                    values = listOf(
                        PointValue(0f, 1f),
                        PointValue(1f, 2f),
                        PointValue(2f, 2.5f),
                        PointValue(3f, 3f),
                        PointValue(4f, 3.5f),
                        PointValue(5f, 4f),
                        PointValue(6f, 4.2f),
                        PointValue(7f, 5f)
                    ),
                    color = chartColors.lineColors[1].toArgb(),
                    isCubic = true,
                    hasPoints = true,
                    hasLabels = false
                )
            )
        ).apply {
            axisXBottom = Axis().apply {
                name = "Time"
            }
            axisYLeft = Axis().apply {
                name = "Value"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Line Chart") },
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
                title = "Sales Trends",
                subtitle = "Last 8 quarters"
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

            // Selected value display
            selectedValue?.let { value ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Selected Point",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Line: ${value.firstIndex}, Point: ${value.secondIndex}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
