package co.csadev.kellocharts.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.compose.ColumnChart
import co.csadev.kellocharts.compose.common.ChartTitle
import co.csadev.kellocharts.compose.gesture.GestureConfig
import co.csadev.kellocharts.compose.theme.LocalChartColors
import co.csadev.kellocharts.model.*

/**
 * Demo screen for ColumnChart composable.
 * Demonstrates grouped and stacked column charts with selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnChartScreen(
    onNavigateBack: () -> Unit
) {
    val chartColors = LocalChartColors.current
    var selectedValue by remember { mutableStateOf<SelectedValue?>(null) }
    var isStacked by remember { mutableStateOf(false) }

    // Generate sample data
    val columnChartData = remember(isStacked) {
        ColumnChartData(
            columns = listOf(
                Column(
                    values = listOf(
                        SubcolumnValue(5f, chartColors.columnColors[0].toArgb()),
                        SubcolumnValue(4f, chartColors.columnColors[1].toArgb()),
                        SubcolumnValue(3f, chartColors.columnColors[2].toArgb())
                    )
                ),
                Column(
                    values = listOf(
                        SubcolumnValue(6f, chartColors.columnColors[0].toArgb()),
                        SubcolumnValue(5f, chartColors.columnColors[1].toArgb()),
                        SubcolumnValue(4f, chartColors.columnColors[2].toArgb())
                    )
                ),
                Column(
                    values = listOf(
                        SubcolumnValue(4f, chartColors.columnColors[0].toArgb()),
                        SubcolumnValue(6f, chartColors.columnColors[1].toArgb()),
                        SubcolumnValue(5f, chartColors.columnColors[2].toArgb())
                    )
                ),
                Column(
                    values = listOf(
                        SubcolumnValue(7f, chartColors.columnColors[0].toArgb()),
                        SubcolumnValue(6f, chartColors.columnColors[1].toArgb()),
                        SubcolumnValue(5f, chartColors.columnColors[2].toArgb())
                    )
                ),
                Column(
                    values = listOf(
                        SubcolumnValue(5f, chartColors.columnColors[0].toArgb()),
                        SubcolumnValue(7f, chartColors.columnColors[1].toArgb()),
                        SubcolumnValue(6f, chartColors.columnColors[2].toArgb())
                    )
                ),
                Column(
                    values = listOf(
                        SubcolumnValue(8f, chartColors.columnColors[0].toArgb()),
                        SubcolumnValue(7f, chartColors.columnColors[1].toArgb()),
                        SubcolumnValue(6f, chartColors.columnColors[2].toArgb())
                    )
                )
            ),
            isStacked = isStacked
        ).apply {
            axisXBottom = Axis().apply {
                name = "Quarter"
            }
            axisYLeft = Axis().apply {
                name = "Revenue (millions)"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isStacked) "Stacked Column Chart" else "Grouped Column Chart") },
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
                title = "Quarterly Revenue by Product",
                subtitle = "Last 6 quarters"
            )

            ColumnChart(
                data = columnChartData,
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

            // Toggle between grouped and stacked
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Chart Type",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row {
                        FilterChip(
                            selected = !isStacked,
                            onClick = { isStacked = false },
                            label = { Text("Grouped") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = isStacked,
                            onClick = { isStacked = true },
                            label = { Text("Stacked") }
                        )
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
                            text = "Selected Column",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Column: ${value.firstIndex}, Subcolumn: ${value.secondIndex}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
