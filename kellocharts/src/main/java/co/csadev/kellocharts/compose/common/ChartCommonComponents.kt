package co.csadev.kellocharts.compose.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A chart title component with Material 3 styling.
 *
 * ## Usage Example
 * ```kotlin
 * Column {
 *     ChartTitle(
 *         title = "Monthly Revenue",
 *         subtitle = "Last 12 months"
 *     )
 *     LineChart(data = data)
 * }
 * ```
 *
 * @param title The main title text
 * @param subtitle Optional subtitle text
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun ChartTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * A legend item representing a single data series.
 *
 * @param label The label text
 * @param color The color associated with this series
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun LegendItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * A chart legend displaying multiple series.
 *
 * ## Usage Example
 * ```kotlin
 * Column {
 *     LineChart(data = data)
 *     ChartLegend(
 *         items = listOf(
 *             LegendItemData("Revenue", Color.Blue),
 *             LegendItemData("Expenses", Color.Red),
 *             LegendItemData("Profit", Color.Green)
 *         )
 *     )
 * }
 * ```
 *
 * @param items List of legend items to display
 * @param modifier Modifier to be applied to the component
 * @param arrangement How to arrange the legend items (horizontal or vertical)
 */
@Composable
fun ChartLegend(
    items: List<LegendItemData>,
    modifier: Modifier = Modifier,
    arrangement: LegendArrangement = LegendArrangement.Horizontal
) {
    when (arrangement) {
        LegendArrangement.Horizontal -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items.forEach { item ->
                    LegendItem(
                        label = item.label,
                        color = item.color
                    )
                }
            }
        }
        LegendArrangement.Vertical -> {
            Column(
                modifier = modifier.padding(16.dp)
            ) {
                items.forEach { item ->
                    LegendItem(
                        label = item.label,
                        color = item.color
                    )
                }
            }
        }
    }
}

/**
 * Data class for legend items.
 */
data class LegendItemData(
    val label: String,
    val color: Color
)

/**
 * Legend arrangement options.
 */
enum class LegendArrangement {
    Horizontal,
    Vertical
}

/**
 * A loading indicator for charts.
 *
 * Displays a circular progress indicator with optional message.
 *
 * ## Usage Example
 * ```kotlin
 * if (isLoading) {
 *     ChartLoadingIndicator(
 *         message = "Loading chart data..."
 *     )
 * } else {
 *     LineChart(data = data)
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the component
 * @param message Optional loading message
 */
@Composable
fun ChartLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )

        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * An error view for charts.
 *
 * Displays an error message with optional retry button.
 *
 * ## Usage Example
 * ```kotlin
 * if (hasError) {
 *     ChartErrorView(
 *         message = "Failed to load data",
 *         onRetry = { viewModel.retryLoad() }
 *     )
 * } else {
 *     LineChart(data = data)
 * }
 * ```
 *
 * @param message The error message to display
 * @param modifier Modifier to be applied to the component
 * @param onRetry Optional callback when retry button is clicked
 */
@Composable
fun ChartErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Retry")
            }
        }
    }
}

/**
 * An empty state view for charts.
 *
 * Displays a message when there's no data to show.
 *
 * ## Usage Example
 * ```kotlin
 * if (data.isEmpty()) {
 *     ChartEmptyView(
 *         message = "No data available"
 *     )
 * } else {
 *     LineChart(data = data)
 * }
 * ```
 *
 * @param message The message to display
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun ChartEmptyView(
    message: String = "No data available",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A value label overlay for selected points.
 *
 * Displays a label with the selected value in a rounded background.
 *
 * ## Usage Example
 * ```kotlin
 * selectedValue?.let { value ->
 *     ValueLabel(
 *         text = "Value: ${value.value}",
 *         position = Offset(x, y)
 *     )
 * }
 * ```
 *
 * @param text The label text
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun ValueLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
