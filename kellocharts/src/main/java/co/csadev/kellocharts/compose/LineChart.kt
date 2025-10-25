package co.csadev.kellocharts.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import co.csadev.kellocharts.compose.animation.ChartAnimationDefaults
import co.csadev.kellocharts.compose.animation.rememberChartDataAnimation
import co.csadev.kellocharts.compose.gesture.GestureConfig
import co.csadev.kellocharts.compose.gesture.ZoomMode
import co.csadev.kellocharts.compose.gesture.chartGestures
import co.csadev.kellocharts.compose.renderer.ComposeAxesRenderer
import co.csadev.kellocharts.compose.renderer.ComposeLineChartRenderer
import co.csadev.kellocharts.compose.state.ViewportState
import co.csadev.kellocharts.compose.state.rememberViewportState
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport

/**
 * A Compose-based line chart component.
 *
 * Displays one or more lines with optional point markers, filled areas, and axes.
 * Supports zoom, scroll, and value selection gestures.
 *
 * ## Features
 * - **Multiple lines**: Display multiple data series
 * - **Line styles**: Straight, cubic (smooth), or square (step) lines
 * - **Filled areas**: Optional area fill under lines
 * - **Point markers**: Circle, square, or diamond markers at data points
 * - **Axes**: Configurable X and Y axes with labels and grid lines
 * - **Gestures**: Pinch-to-zoom, drag-to-pan, tap-to-select
 * - **Animations**: Smooth data transitions and viewport changes
 *
 * ## Basic Usage
 * ```kotlin
 * val data = remember {
 *     LineChartData(
 *         lines = listOf(
 *             Line(
 *                 values = listOf(
 *                     PointValue(0f, 2f),
 *                     PointValue(1f, 4f),
 *                     PointValue(2f, 3f),
 *                     PointValue(3f, 5f)
 *                 )
 *             )
 *         )
 *     )
 * }
 *
 * LineChart(
 *     data = data,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * ## With Gestures and Selection
 * ```kotlin
 * var selectedValue by remember { mutableStateOf<SelectedValue?>(null) }
 *
 * LineChart(
 *     data = data,
 *     gestureConfig = GestureConfig(
 *         zoomEnabled = true,
 *         scrollEnabled = true,
 *         selectionEnabled = true
 *     ),
 *     onValueSelected = { value ->
 *         selectedValue = value
 *     },
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * @param data The line chart data to display
 * @param modifier Modifier to be applied to the chart
 * @param viewportState State holder for viewport (zoom/pan). If null, creates a default viewport.
 * @param gestureConfig Configuration for gesture handling
 * @param zoomMode Zoom constraint mode (horizontal, vertical, both, or none)
 * @param animate Whether to animate initial chart appearance
 * @param onValueSelected Callback when a value is selected via tap
 * @param onValueDeselected Callback when selection is cleared
 */
@Composable
fun LineChart(
    data: LineChartData,
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
        ComposeLineChartRenderer(data)
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
        val maxViewport = calculateLineChartViewport(data)
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
                    renderer.getValueAtPosition(offset)
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
            // Apply animation by scaling the drawing
            drawIntoCanvas { canvas ->
                renderer.draw(this, size, viewportState.currentViewport)
            }
        }
    }
}

/**
 * Calculate the viewport that encompasses all data points.
 */
private fun calculateLineChartViewport(data: LineChartData): Viewport {
    if (data.lines.isEmpty()) {
        return Viewport(0f, 100f, 0f, 100f)
    }

    var minX = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var minY = Float.MAX_VALUE
    var maxY = Float.MIN_VALUE

    data.lines.forEach { line ->
        line.values.forEach { point ->
            minX = minOf(minX, point.x)
            maxX = maxOf(maxX, point.x)
            minY = minOf(minY, point.y)
            maxY = maxOf(maxY, point.y)
        }
    }

    // Add 10% padding
    val xPadding = (maxX - minX) * 0.1f
    val yPadding = (maxY - minY) * 0.1f

    return Viewport(
        left = minX - xPadding,
        top = maxY + yPadding,
        right = maxX + xPadding,
        bottom = minY - yPadding
    )
}

/**
 * Calculate the content rectangle (chart drawing area excluding margins).
 */
private fun calculateContentRect(size: Size, data: LineChartData): Rect {
    val marginLeft = if (data.axisYLeft != null) 60f else 10f
    val marginRight = if (data.axisYRight != null) 60f else 10f
    val marginTop = if (data.axisXTop != null) 40f else 10f
    val marginBottom = if (data.axisXBottom != null) 40f else 10f

    return Rect(
        left = marginLeft,
        top = marginTop,
        right = size.width - marginRight,
        bottom = size.height - marginBottom
    )
}
