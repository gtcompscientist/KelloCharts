package co.csadev.kellocharts.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.csadev.kellocharts.model.Viewport

/**
 * State holder for chart viewport management in Compose.
 *
 * The viewport defines the visible portion of the chart's data space.
 * It's used for zooming and panning operations.
 *
 * ## Usage Example
 * ```kotlin
 * val viewportState = rememberViewportState(
 *     initialViewport = Viewport(0f, 10f, 0f, 100f)
 * )
 *
 * // Use in a chart composable
 * LineChart(
 *     data = lineChartData,
 *     viewportState = viewportState
 * )
 *
 * // Programmatically change viewport
 * viewportState.setViewport(Viewport(5f, 15f, 50f, 150f))
 * ```
 *
 * @property currentViewport The current visible viewport
 * @property maximumViewport The maximum possible viewport (full data range)
 */
@Stable
class ViewportState(
    initialViewport: Viewport = Viewport(),
    initialMaxViewport: Viewport = Viewport()
) {
    /**
     * The current visible viewport.
     */
    var currentViewport by mutableStateOf(initialViewport)
        private set

    /**
     * The maximum possible viewport containing all data.
     */
    var maximumViewport by mutableStateOf(initialMaxViewport)
        private set

    /**
     * Update the current viewport.
     *
     * @param viewport The new viewport to display
     */
    fun setViewport(viewport: Viewport) {
        currentViewport = viewport
    }

    /**
     * Update the maximum viewport.
     *
     * @param viewport The new maximum viewport
     */
    fun setMaximumViewport(viewport: Viewport) {
        maximumViewport = viewport
    }

    /**
     * Set both current and maximum viewports.
     */
    fun setViewports(current: Viewport, maximum: Viewport) {
        currentViewport = current
        maximumViewport = maximum
    }

    /**
     * Zoom the viewport by a scale factor around a focal point.
     *
     * @param scaleX Horizontal scale factor (> 1 = zoom in, < 1 = zoom out)
     * @param scaleY Vertical scale factor (> 1 = zoom in, < 1 = zoom out)
     * @param focusX X coordinate of zoom focal point (in data coordinates)
     * @param focusY Y coordinate of zoom focal point (in data coordinates)
     */
    fun zoom(scaleX: Float, scaleY: Float, focusX: Float, focusY: Float) {
        val newLeft = focusX - (focusX - currentViewport.left) / scaleX
        val newRight = focusX + (currentViewport.right - focusX) / scaleX
        val newTop = focusY - (focusY - currentViewport.top) / scaleY
        val newBottom = focusY + (currentViewport.bottom - focusY) / scaleY

        currentViewport = Viewport(newLeft, newTop, newRight, newBottom)
    }

    /**
     * Pan the viewport by a delta amount.
     *
     * @param dx Horizontal pan delta (in data coordinates)
     * @param dy Vertical pan delta (in data coordinates)
     */
    fun pan(dx: Float, dy: Float) {
        currentViewport = Viewport(
            currentViewport.left + dx,
            currentViewport.top + dy,
            currentViewport.right + dx,
            currentViewport.bottom + dy
        )
    }

    /**
     * Reset the viewport to show all data (maximum viewport).
     */
    fun resetViewport() {
        currentViewport = maximumViewport
    }

    /**
     * Check if the viewport contains a specific point.
     */
    fun contains(x: Float, y: Float): Boolean {
        return x >= currentViewport.left &&
               x <= currentViewport.right &&
               y >= currentViewport.top &&
               y <= currentViewport.bottom
    }

    /**
     * Get the width of the current viewport.
     */
    val width: Float
        get() = currentViewport.right - currentViewport.left

    /**
     * Get the height of the current viewport.
     */
    val height: Float
        get() = currentViewport.bottom - currentViewport.top
}

/**
 * Remember a [ViewportState] across recompositions.
 *
 * @param initialViewport The initial viewport to display
 * @param initialMaxViewport The maximum viewport (full data range)
 */
@Composable
fun rememberViewportState(
    initialViewport: Viewport = Viewport(),
    initialMaxViewport: Viewport = Viewport()
): ViewportState {
    return remember(initialViewport, initialMaxViewport) {
        ViewportState(initialViewport, initialMaxViewport)
    }
}
