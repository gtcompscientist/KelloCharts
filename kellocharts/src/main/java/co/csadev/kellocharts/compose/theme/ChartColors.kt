package co.csadev.kellocharts.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color palette for chart rendering in KelloCharts.
 *
 * Provides Material 3-based colors for different chart types and elements.
 * All colors are immutable and optimized for Compose recomposition.
 *
 * ## Usage Example
 * ```kotlin
 * @Composable
 * fun MyChart() {
 *     val chartColors = LocalChartColors.current
 *     LineChart(
 *         data = data,
 *         lineColor = chartColors.lineColors[0]
 *     )
 * }
 * ```
 */
@Immutable
data class ChartColors(
    /** Colors for line charts (multiple lines can use different colors) */
    val lineColors: List<Color>,

    /** Colors for column charts */
    val columnColors: List<Color>,

    /** Colors for pie chart slices */
    val pieColors: List<Color>,

    /** Colors for bubble charts */
    val bubbleColors: List<Color>,

    /** Color for axis lines */
    val axisLineColor: Color,

    /** Color for grid lines */
    val gridLineColor: Color,

    /** Color for axis labels */
    val axisLabelColor: Color,

    /** Color for value labels */
    val valueLabelColor: Color,

    /** Color for selection/highlight */
    val selectionColor: Color,

    /** Background color for value labels */
    val labelBackgroundColor: Color,

    /** Color for chart background */
    val chartBackgroundColor: Color
) {
    companion object {
        /**
         * Material 3 color palette for charts (light theme).
         */
        val LightPalette = listOf(
            Color(0xFF6750A4), // Primary Purple
            Color(0xFF0B57D0), // Blue
            Color(0xFF006A6A), // Teal
            Color(0xFF984061), // Pink
            Color(0xFF8D4E2A), // Brown
            Color(0xFF705C00), // Yellow/Gold
            Color(0xFF00639B), // Cyan
            Color(0xFF8E4585), // Magenta
            Color(0xFF006E26), // Green
            Color(0xFFB3261E)  // Red/Error
        )

        /**
         * Material 3 color palette for charts (dark theme).
         */
        val DarkPalette = listOf(
            Color(0xFFD0BCFF), // Primary Purple Light
            Color(0xFFA8C7FA), // Blue Light
            Color(0xFF4DD0E1), // Teal Light
            Color(0xFFF48FB1), // Pink Light
            Color(0xFFFFAB91), // Brown Light
            Color(0xFFFFF59D), // Yellow/Gold Light
            Color(0xFF81D4FA), // Cyan Light
            Color(0xFFCE93D8), // Magenta Light
            Color(0xFFA5D6A7), // Green Light
            Color(0xFFF2B8B5)  // Red/Error Light
        )
    }
}

/**
 * CompositionLocal for providing [ChartColors] to the composition tree.
 */
val LocalChartColors = staticCompositionLocalOf<ChartColors> {
    error("No ChartColors provided")
}

/**
 * Create default chart colors for light theme based on Material 3 color scheme.
 */
fun defaultLightChartColors(colorScheme: ColorScheme): ChartColors {
    return ChartColors(
        lineColors = ChartColors.LightPalette,
        columnColors = ChartColors.LightPalette,
        pieColors = ChartColors.LightPalette,
        bubbleColors = ChartColors.LightPalette,
        axisLineColor = colorScheme.onSurfaceVariant,
        gridLineColor = colorScheme.surfaceVariant,
        axisLabelColor = colorScheme.onSurface,
        valueLabelColor = colorScheme.onSurface,
        selectionColor = colorScheme.primary,
        labelBackgroundColor = colorScheme.surface.copy(alpha = 0.9f),
        chartBackgroundColor = colorScheme.surface
    )
}

/**
 * Create default chart colors for dark theme based on Material 3 color scheme.
 */
fun defaultDarkChartColors(colorScheme: ColorScheme): ChartColors {
    return ChartColors(
        lineColors = ChartColors.DarkPalette,
        columnColors = ChartColors.DarkPalette,
        pieColors = ChartColors.DarkPalette,
        bubbleColors = ChartColors.DarkPalette,
        axisLineColor = colorScheme.onSurfaceVariant,
        gridLineColor = colorScheme.surfaceVariant,
        axisLabelColor = colorScheme.onSurface,
        valueLabelColor = colorScheme.onSurface,
        selectionColor = colorScheme.primary,
        labelBackgroundColor = colorScheme.surface.copy(alpha = 0.9f),
        chartBackgroundColor = colorScheme.surface
    )
}

/**
 * Extension to get a chart color by index (with wrapping for safety).
 */
fun List<Color>.getChartColor(index: Int): Color {
    if (isEmpty()) return Color.Gray
    return this[index % size]
}
