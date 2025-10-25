package co.csadev.kellocharts.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.compose.PieChart
import co.csadev.kellocharts.compose.common.ChartLegend
import co.csadev.kellocharts.compose.common.ChartTitle
import co.csadev.kellocharts.compose.common.LegendItemData
import co.csadev.kellocharts.compose.theme.LocalChartColors
import co.csadev.kellocharts.model.*

/**
 * Demo screen for PieChart composable.
 * Demonstrates pie/donut charts with rotation and selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PieChartScreen(
    onNavigateBack: () -> Unit
) {
    val chartColors = LocalChartColors.current
    var selectedValue by remember { mutableStateOf<SelectedValue?>(null) }
    var rotation by remember { mutableStateOf(0f) }
    var hasCenterCircle by remember { mutableStateOf(false) }
    var rotationEnabled by remember { mutableStateOf(false) }

    // Sample data with labels
    val sliceLabels = listOf("Mobile", "Desktop", "Tablet", "Other")
    val sliceValues = listOf(45f, 30f, 20f, 5f)

    // Generate sample data
    val pieChartData = remember(hasCenterCircle) {
        PieChartData(
            values = sliceValues.mapIndexed { index, value ->
                SliceValue(
                    value = value,
                    color = chartColors.pieColors[index % chartColors.pieColors.size].toArgb(),
                    label = sliceLabels[index].toCharArray()
                )
            }.toMutableList(),
            hasCenterCircle = hasCenterCircle,
            centerCircleScale = if (hasCenterCircle) 0.6f else 1f,
            slicesSpacing = 2
        )
    }

    // Legend items
    val legendItems = remember {
        sliceLabels.mapIndexed { index, label ->
            LegendItemData(
                label = "$label (${sliceValues[index].toInt()}%)",
                color = chartColors.pieColors[index % chartColors.pieColors.size]
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (hasCenterCircle) "Donut Chart" else "Pie Chart") },
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
                title = "Platform Usage Distribution",
                subtitle = "Active users by platform"
            )

            PieChart(
                data = pieChartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                rotation = rotation,
                rotationEnabled = rotationEnabled,
                selectionEnabled = true,
                animate = true,
                onRotationChange = { newRotation ->
                    rotation = newRotation
                },
                onValueSelected = { value ->
                    selectedValue = value
                },
                onValueDeselected = {
                    selectedValue = null
                }
            )

            // Legend
            ChartLegend(
                items = legendItems,
                modifier = Modifier.fillMaxWidth()
            )

            // Chart options
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chart style toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Chart Style",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row {
                            FilterChip(
                                selected = !hasCenterCircle,
                                onClick = { hasCenterCircle = false },
                                label = { Text("Pie") },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            FilterChip(
                                selected = hasCenterCircle,
                                onClick = { hasCenterCircle = true },
                                label = { Text("Donut") }
                            )
                        }
                    }

                    // Rotation toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rotation Gesture",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Switch(
                            checked = rotationEnabled,
                            onCheckedChange = { rotationEnabled = it }
                        )
                    }

                    // Rotation slider
                    if (!rotationEnabled) {
                        Column {
                            Text(
                                text = "Rotation: ${rotation.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = rotation,
                                onValueChange = { rotation = it },
                                valueRange = 0f..360f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

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
                            text = "Selected Slice",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${sliceLabels[value.firstIndex]}: ${sliceValues[value.firstIndex].toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
