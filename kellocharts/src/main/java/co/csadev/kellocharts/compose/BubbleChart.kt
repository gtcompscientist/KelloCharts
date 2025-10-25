package co.csadev.kellocharts.compose

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
import co.csadev.kellocharts.compose.renderer.ComposeBubbleChartRenderer
import co.csadev.kellocharts.compose.state.ViewportState
import co.csadev.kellocharts.compose.state.rememberViewportState
import co.csadev.kellocharts.model.BubbleChartData
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport

/**
 * A Compose-based bubble chart component.
 *
 * Displays data as circles where X and Y determine position and Z (value) determines size.
 * Perfect for visualizing three-dimensional data in a 2D space.
 *
 * ## Features
 * - **Three dimensions**: X, Y position and Z (bubble size)
 * - **Variable sizes**: Bubbles scale based on Z value
 * - **Custom colors**: Per-bubble colors with transparency
 * - **Axes**: Configurable X and Y axes with labels and grid lines
 * - **Gestures**: Pinch-to-zoom, drag-to-pan, tap-to-select
 * - **Animations**: Smooth bubble appearance and size transitions
 *
 * ## Basic Usage
 * ```kotlin
 * val data = remember {
 *     BubbleChartData(
 *         values = listOf(
 *             BubbleValue(1f, 2f, 50f), // x, y, z (size)
 *             BubbleValue(2f, 4f, 30f),
 *             BubbleValue(3f, 3f, 70f),
 *             BubbleValue(4f, 5f, 40f)
 *         )
 *     )
 * }
 *
 * BubbleChart(
 *     data = data,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * ## With Custom Bubble Scale
 * ```kotlin
 * val data = remember {
 *     BubbleChartData(
 *         values = listOf(/* bubbles */),
 *         bubbleScale = 1.5f, // Make bubbles 50% larger
 *         minBubbleRadius = 8 // Minimum 8dp radius
 *     )
 * }
 *
 * BubbleChart(
 *     data = data,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * @param data The bubble chart data to display
 * @param modifier Modifier to be applied to the chart
 * @param viewportState State holder for viewport (zoom/pan). If null, creates a default viewport.
 * @param gestureConfig Configuration for gesture handling
 * @param zoomMode Zoom constraint mode (horizontal, vertical, both, or none)
 * @param animate Whether to animate initial chart appearance
 * @param onValueSelected Callback when a bubble is selected via tap
 * @param onValueDeselected Callback when selection is cleared
 */
@Composable
fun BubbleChart(
    data: BubbleChartData,
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
        ComposeBubbleChartRenderer(data)
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
        val maxViewport = calculateBubbleChartViewport(data)
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
            drawIntoCanvas { canvas ->
                renderer.draw(this, size, viewportState.currentViewport)
            }
        }
    }
}

/**
 * Calculate the viewport that encompasses all bubble data.
 */
private fun calculateBubbleChartViewport(data: BubbleChartData): Viewport {
    if (data.values.isEmpty()) {
        return Viewport(0f, 100f, 0f, 100f)
    }

    var minX = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var minY = Float.MAX_VALUE
    var maxY = Float.MIN_VALUE

    data.values.forEach { bubble ->
        minX = minOf(minX, bubble.x)
        maxX = maxOf(maxX, bubble.x)
        minY = minOf(minY, bubble.y)
        maxY = maxOf(maxY, bubble.y)
    }

    // Add 15% padding to account for bubble sizes
    val xPadding = (maxX - minX) * 0.15f
    val yPadding = (maxY - minY) * 0.15f

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
private fun calculateContentRect(size: Size, data: BubbleChartData): Rect {
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
