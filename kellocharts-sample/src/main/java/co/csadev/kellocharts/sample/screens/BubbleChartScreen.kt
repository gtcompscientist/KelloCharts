package co.csadev.kellocharts.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.compose.BubbleChart
import co.csadev.kellocharts.compose.common.ChartTitle
import co.csadev.kellocharts.compose.gesture.GestureConfig
import co.csadev.kellocharts.compose.theme.LocalChartColors
import co.csadev.kellocharts.model.*

/**
 * Demo screen for BubbleChart composable.
 * Demonstrates three-dimensional data visualization with variable bubble sizes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubbleChartScreen(
    onNavigateBack: () -> Unit
) {
    val chartColors = LocalChartColors.current
    var selectedValue by remember { mutableStateOf<SelectedValue?>(null) }

    // Generate sample data representing product performance
    // X = Marketing Spend (thousands), Y = Revenue (millions), Z = Market Share (%)
    val bubbleChartData = remember {
        BubbleChartData(
            values = listOf(
                // Product A - High revenue, high spend, large market share
                BubbleValue(8f, 15f, 25f).apply {
                    color = chartColors.bubbleColors[0].toArgb()
                    label = "A".toCharArray()
                },
                // Product B - Medium revenue, medium spend, medium market share
                BubbleValue(5f, 10f, 15f).apply {
                    color = chartColors.bubbleColors[1].toArgb()
                    label = "B".toCharArray()
                },
                // Product C - Low revenue, low spend, small market share
                BubbleValue(3f, 6f, 8f).apply {
                    color = chartColors.bubbleColors[2].toArgb()
                    label = "C".toCharArray()
                },
                // Product D - High spend, low revenue, medium market share
                BubbleValue(7f, 8f, 12f).apply {
                    color = chartColors.bubbleColors[3].toArgb()
                    label = "D".toCharArray()
                },
                // Product E - Low spend, medium revenue, small market share
                BubbleValue(2f, 9f, 10f).apply {
                    color = chartColors.bubbleColors[4].toArgb()
                    label = "E".toCharArray()
                },
                // Product F - Medium spend, high revenue, large market share
                BubbleValue(6f, 14f, 22f).apply {
                    color = chartColors.bubbleColors[5].toArgb()
                    label = "F".toCharArray()
                },
                // Product G - Very high spend, very high revenue, very large market share
                BubbleValue(10f, 18f, 30f).apply {
                    color = chartColors.bubbleColors[6].toArgb()
                    label = "G".toCharArray()
                },
                // Product H - Very low spend, very low revenue, very small market share
                BubbleValue(1f, 4f, 5f).apply {
                    color = chartColors.bubbleColors[7].toArgb()
                    label = "H".toCharArray()
                }
            ).toMutableList(),
            bubbleScale = 1.2f,
            minBubbleRadius = 10
        ).apply {
            axisXBottom = Axis().apply {
                name = "Marketing Spend ($K)"
            }
            axisYLeft = Axis().apply {
                name = "Revenue ($M)"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bubble Chart") },
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
                title = "Product Performance Analysis",
                subtitle = "Bubble size = Market share"
            )

            BubbleChart(
                data = bubbleChartData,
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
                        text = "Chart Information",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "• X-axis: Marketing spend in thousands",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "• Y-axis: Revenue in millions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "• Bubble size: Market share percentage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "• Pinch to zoom, drag to pan, tap to select",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Selected value display
            selectedValue?.let { value ->
                val bubble = bubbleChartData.values[value.firstIndex]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Selected Product ${String(bubble.label)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Marketing Spend: $${bubble.x.toInt()}K",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Revenue: $${bubble.y.toInt()}M",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Market Share: ${bubble.z.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
