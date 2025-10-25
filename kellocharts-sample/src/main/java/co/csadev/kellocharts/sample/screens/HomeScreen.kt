package co.csadev.kellocharts.sample.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.compose.theme.ThemeMode
import co.csadev.kellocharts.compose.theme.ThemeState

/**
 * Home screen displaying list of chart samples.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    themeState: ThemeState
) {
    var showThemeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KelloCharts Samples") },
                actions = {
                    // Theme switcher icon
                    IconButton(onClick = { showThemeMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change theme"
                        )
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Light Theme") },
                            onClick = {
                                themeState.setThemeMode(ThemeMode.LIGHT)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LightMode, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dark Theme") },
                            onClick = {
                                themeState.setThemeMode(ThemeMode.DARK)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DarkMode, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("System Default") },
                            onClick = {
                                themeState.setThemeMode(ThemeMode.SYSTEM)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Dynamic Color: ${if (themeState.dynamicColor) "On" else "Off"}") },
                            onClick = {
                                themeState.setDynamicColor(!themeState.dynamicColor)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ColorLens, contentDescription = null)
                            }
                        )
                    }

                    // About icon
                    IconButton(onClick = { onNavigate("about") }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chartSamples) { sample ->
                ChartSampleCard(
                    sample = sample,
                    onClick = { onNavigate(sample.route) }
                )
            }
        }
    }
}

@Composable
fun ChartSampleCard(
    sample: ChartSample,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = sample.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sample.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (sample.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sample.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class ChartSample(
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector
)

val chartSamples = listOf(
    ChartSample(
        title = "Line Chart",
        description = "Display trends over time with straight, curved, or step lines",
        route = "line_chart",
        icon = Icons.Default.ShowChart
    ),
    ChartSample(
        title = "Column Chart",
        description = "Compare values with vertical bars, supports grouped and stacked",
        route = "column_chart",
        icon = Icons.Default.BarChart
    ),
    ChartSample(
        title = "Pie Chart",
        description = "Show proportions with circular slices, includes donut mode",
        route = "pie_chart",
        icon = Icons.Default.PieChart
    ),
    ChartSample(
        title = "Bubble Chart",
        description = "Visualize three dimensions with variable-sized bubbles",
        route = "bubble_chart",
        icon = Icons.Default.BubbleChart
    ),
    ChartSample(
        title = "Good/Bad Chart",
        description = "Example of filled area chart with positive/negative values",
        route = "good_bad_chart",
        icon = Icons.Default.TrendingUp
    )
)
