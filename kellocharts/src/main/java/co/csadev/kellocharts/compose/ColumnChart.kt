package co.csadev.kellocharts.compose

import co.csadev.kellocharts.compose.common.ChartLayoutConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import co.csadev.kellocharts.compose.animation.ChartAnimationDefaults
import co.csadev.kellocharts.compose.animation.rememberChartDataAnimation
import co.csadev.kellocharts.compose.gesture.GestureConfig
import co.csadev.kellocharts.compose.gesture.ZoomMode
import co.csadev.kellocharts.compose.gesture.chartGestures
import co.csadev.kellocharts.compose.renderer.ComposeAxesRenderer
import co.csadev.kellocharts.compose.renderer.ComposeColumnChartRenderer
import co.csadev.kellocharts.compose.state.ViewportState
import co.csadev.kellocharts.compose.state.rememberViewportState
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport

/**
 * A Compose-based column (bar) chart component.
 *
 * Displays vertical or horizontal columns/bars with support for grouping, stacking,
 * and negative values.
 *
 * ## Features
 * - **Grouped columns**: Display multiple series side-by-side
 * - **Stacked columns**: Stack values on top of each other
 * - **Negative values**: Support for values below baseline
 * - **Rounded corners**: Modern Material 3 appearance
 * - **Axes**: Configurable X and Y axes with labels and grid lines
 * - **Gestures**: Pinch-to-zoom, drag-to-pan, tap-to-select
 * - **Animations**: Sequential column appearance with stagger
 *
 * ## Basic Usage
 * ```kotlin
 * val data = remember {
 *     ColumnChartData(
 *         columns = listOf(
 *             Column(
 *                 values = listOf(
 *                     SubcolumnValue(4f),
 *                     SubcolumnValue(3f),
 *                     SubcolumnValue(2f)
 *                 )
 *             )
 *         )
 *     )
 * }
 *
 * ColumnChart(
 *     data = data,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * ## Stacked Columns
 * ```kotlin
 * val data = remember {
 *     ColumnChartData(
 *         columns = listOf(/* columns */),
 *         isStacked = true
 *     )
 * }
 *
 * ColumnChart(
 *     data = data,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * @param data The column chart data to display
 * @param modifier Modifier to be applied to the chart
 * @param viewportState State holder for viewport (zoom/pan). If null, creates a default viewport.
 * @param gestureConfig Configuration for gesture handling
 * @param zoomMode Zoom constraint mode (horizontal, vertical, both, or none)
 * @param animate Whether to animate initial chart appearance
 * @param onValueSelected Callback when a value is selected via tap
 * @param onValueDeselected Callback when selection is cleared
 */
@Composable
fun ColumnChart(
    data: ColumnChartData,
    modifier: Modifier = Modifier,
    viewportState: ViewportState = rememberViewportState(),
    gestureConfig: GestureConfig = GestureConfig(),
    zoomMode: ZoomMode = ZoomMode.HORIZONTAL_AND_VERTICAL,
    animate: Boolean = true,
    onValueSelected: ((SelectedValue) -> Unit)? = null,
    onValueDeselected: (() -> Unit)? = null
) {
    // Animation progress for initial appearance
    val animationProgress = if (animate) {
        rememberChartDataAnimation(
            animationSpec = ChartAnimationDefaults.tween
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    // Create renderer
    val renderer = remember(data) {
        ComposeColumnChartRenderer(data)
    }

    // Create axes renderer if axes are present
    val axesRenderer = remember(data.axisXBottom, data.axisYLeft) {
        if (data.axisXBottom != null || data.axisYLeft != null) {
            ComposeAxesRenderer(
                axisBottom = data.axisXBottom,
                axisLeft = data.axisYLeft,
                axisTop = data.axisXTop,
                axisRight = data.axisYRight
            )
        } else null
    }

    // Calculate initial viewport if not set
    if (viewportState.maximumViewport.isEmpty) {
        val maxViewport = calculateColumnChartViewport(data)
        viewportState.setViewports(maxViewport, maxViewport)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .chartGestures(
                viewportState = viewportState,
                config = gestureConfig,
                zoomMode = zoomMode,
                onValueSelected = onValueSelected,
                onValueDeselected = onValueDeselected,
                getValueAtPosition = { offset ->
                    renderer.getValueAtPosition(offset, viewportState.currentViewport)
                }
            )
    ) {
        val contentRect = calculateContentRect(size, data)

        // Update renderer size
        renderer.onSizeChanged(size, contentRect)
        renderer.onViewportChanged(viewportState.currentViewport)

        // Draw axes first (behind data)
        axesRenderer?.let {
            it.onSizeChanged(size, contentRect)
            it.draw(this, size, viewportState.currentViewport)
        }

        // Draw chart with animation progress
        if (animationProgress.value > 0f) {
            drawIntoCanvas { canvas ->
                renderer.draw(this, size, viewportState.currentViewport)
            }
        }
    }
}

/**
 * Calculate the viewport that encompasses all column data.
 */
private fun calculateColumnChartViewport(data: ColumnChartData): Viewport {
    if (data.columns.isEmpty()) {
        return Viewport(0f, 100f, 0f, 100f)
    }

    val minX = -0.5f
    val maxX = data.columns.size - 0.5f

    var minY = data.baseValue
    var maxY = data.baseValue

    data.columns.forEach { column ->
        if (data.isStacked) {
            // For stacked, sum all positive and negative values separately
            var positiveSum = data.baseValue
            var negativeSum = data.baseValue

            column.values.forEach { value ->
                if (value.value >= 0) {
                    positiveSum += value.value
                } else {
                    negativeSum += value.value
                }
            }

            minY = minOf(minY, negativeSum)
            maxY = maxOf(maxY, positiveSum)
        } else {
            // For grouped, find min/max of all subcolumn values
            column.values.forEach { value ->
                minY = minOf(minY, value.value)
                maxY = maxOf(maxY, value.value)
            }
        }
    }

    // Add 10% padding
    val yPadding = (maxY - minY) * 0.1f

    return Viewport(
        left = minX,
        top = maxY + yPadding,
        right = maxX,
        bottom = minY - yPadding
    )
}

/**
 * Calculate the content rectangle (chart drawing area excluding margins).
 */
private fun calculateContentRect(size: Size, data: ColumnChartData): Rect {
    val marginLeft = if (data.axisYLeft != null) ChartLayoutConstants.MARGIN_WITH_AXIS else ChartLayoutConstants.MARGIN_WITHOUT_AXIS
    val marginRight = if (data.axisYRight != null) ChartLayoutConstants.MARGIN_WITH_AXIS else ChartLayoutConstants.MARGIN_WITHOUT_AXIS
    val marginTop = if (data.axisXTop != null) ChartLayoutConstants.MARGIN_TOP_WITH_AXIS else ChartLayoutConstants.MARGIN_WITHOUT_AXIS
    val marginBottom = if (data.axisXBottom != null) ChartLayoutConstants.MARGIN_BOTTOM_WITH_AXIS else ChartLayoutConstants.MARGIN_WITHOUT_AXIS

    return Rect(
        left = marginLeft,
        top = marginTop,
        right = size.width - marginRight,
        bottom = size.height - marginBottom
    )
}
