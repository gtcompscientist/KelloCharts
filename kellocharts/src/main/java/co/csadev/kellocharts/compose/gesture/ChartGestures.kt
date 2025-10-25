package co.csadev.kellocharts.compose.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import co.csadev.kellocharts.compose.state.ViewportState
import co.csadev.kellocharts.model.SelectedValue
import kotlin.math.abs

/**
 * Configuration for chart gesture handling.
 *
 * @property zoomEnabled Enable pinch-to-zoom gesture
 * @property scrollEnabled Enable drag/pan gesture
 * @property selectionEnabled Enable tap-to-select gesture
 * @property minZoom Minimum zoom level (1.0 = no zoom out beyond original)
 * @property maxZoom Maximum zoom level (10.0 = 10x zoom in)
 * @property flingEnabled Enable fling animation after drag
 */
@Stable
data class GestureConfig(
    val zoomEnabled: Boolean = true,
    val scrollEnabled: Boolean = true,
    val selectionEnabled: Boolean = true,
    val minZoom: Float = 0.5f,
    val maxZoom: Float = 10f,
    val flingEnabled: Boolean = true
)

/**
 * Zoom constraint modes for chart gestures.
 */
enum class ZoomMode {
    /** Allow zooming in both horizontal and vertical directions */
    HORIZONTAL_AND_VERTICAL,

    /** Allow zooming only horizontally */
    HORIZONTAL,

    /** Allow zooming only vertically */
    VERTICAL,

    /** Disable zooming */
    NONE
}

/**
 * Apply zoom gesture handling to a chart.
 *
 * Enables pinch-to-zoom gesture with configurable constraints.
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .chartZoom(
 *             viewportState = viewportState,
 *             zoomMode = ZoomMode.HORIZONTAL_AND_VERTICAL,
 *             minZoom = 0.5f,
 *             maxZoom = 10f
 *         )
 * ) {
 *     // Draw chart
 * }
 * ```
 *
 * @param viewportState The viewport state to update during zoom
 * @param zoomMode The zoom constraint mode
 * @param minZoom Minimum zoom level (default: 0.5f)
 * @param maxZoom Maximum zoom level (default: 10f)
 * @param enabled Whether zoom is enabled (default: true)
 * @param onZoomChange Optional callback when zoom changes
 */
fun Modifier.chartZoom(
    viewportState: ViewportState,
    zoomMode: ZoomMode = ZoomMode.HORIZONTAL_AND_VERTICAL,
    minZoom: Float = 0.5f,
    maxZoom: Float = 10f,
    enabled: Boolean = true,
    onZoomChange: ((scale: Float, centroid: Offset) -> Unit)? = null
): Modifier = if (enabled && zoomMode != ZoomMode.NONE) {
    pointerInput(viewportState, zoomMode, minZoom, maxZoom) {
        detectTransformGestures { centroid, pan, zoom, _ ->
            val scaleX = when (zoomMode) {
                ZoomMode.HORIZONTAL, ZoomMode.HORIZONTAL_AND_VERTICAL -> zoom.coerceIn(minZoom, maxZoom)
                else -> 1f
            }

            val scaleY = when (zoomMode) {
                ZoomMode.VERTICAL, ZoomMode.HORIZONTAL_AND_VERTICAL -> zoom.coerceIn(minZoom, maxZoom)
                else -> 1f
            }

            // Convert screen centroid to data coordinates
            val focusX = viewportState.currentViewport.left +
                (centroid.x / size.width) * viewportState.width
            val focusY = viewportState.currentViewport.top +
                (centroid.y / size.height) * viewportState.height

            viewportState.zoom(scaleX, scaleY, focusX, focusY)
            onZoomChange?.invoke(zoom, centroid)
        }
    }
} else {
    this
}

/**
 * Apply scroll/pan gesture handling to a chart.
 *
 * Enables drag gesture to pan the chart viewport.
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .chartScroll(
 *             viewportState = viewportState,
 *             enabled = true
 *         )
 * ) {
 *     // Draw chart
 * }
 * ```
 *
 * @param viewportState The viewport state to update during scroll
 * @param enabled Whether scroll is enabled (default: true)
 * @param onScrollChange Optional callback when scroll changes
 */
fun Modifier.chartScroll(
    viewportState: ViewportState,
    enabled: Boolean = true,
    onScrollChange: ((delta: Offset) -> Unit)? = null
): Modifier = if (enabled) {
    pointerInput(viewportState) {
        detectDragGestures { change, dragAmount ->
            change.consume()

            // Convert screen drag to data coordinates
            val dx = -dragAmount.x / size.width * viewportState.width
            val dy = dragAmount.y / size.height * viewportState.height

            viewportState.pan(dx, dy)
            onScrollChange?.invoke(dragAmount)
        }
    }
} else {
    this
}

/**
 * Apply value selection gesture handling to a chart.
 *
 * Enables tap gesture to select chart values at the tapped position.
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .chartValueSelection(
 *             onValueSelected = { selectedValue ->
 *                 println("Selected: ${selectedValue}")
 *             },
 *             getValueAtPosition = { offset ->
 *                 renderer.getValueAtPosition(offset)
 *             }
 *         )
 * ) {
 *     // Draw chart
 * }
 * ```
 *
 * @param onValueSelected Callback when a value is selected
 * @param onValueDeselected Optional callback when selection is cleared
 * @param getValueAtPosition Function to determine which value (if any) is at the tapped position
 * @param enabled Whether selection is enabled (default: true)
 */
fun Modifier.chartValueSelection(
    onValueSelected: (SelectedValue) -> Unit,
    onValueDeselected: (() -> Unit)? = null,
    getValueAtPosition: (Offset) -> SelectedValue?,
    enabled: Boolean = true
): Modifier = if (enabled) {
    pointerInput(Unit) {
        detectTapGestures(
            onTap = { offset ->
                val selectedValue = getValueAtPosition(offset)
                if (selectedValue != null) {
                    onValueSelected(selectedValue)
                } else {
                    onValueDeselected?.invoke()
                }
            },
            onLongPress = { offset ->
                // Long press can be used for additional actions
                val selectedValue = getValueAtPosition(offset)
                if (selectedValue != null) {
                    onValueSelected(selectedValue)
                }
            }
        )
    }
} else {
    this
}

/**
 * Apply combined gesture handling to a chart.
 *
 * Convenience modifier that combines zoom, scroll, and selection gestures.
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .chartGestures(
 *             viewportState = viewportState,
 *             config = GestureConfig(
 *                 zoomEnabled = true,
 *                 scrollEnabled = true,
 *                 selectionEnabled = true
 *             ),
 *             onValueSelected = { value ->
 *                 selectedValue = value
 *             },
 *             getValueAtPosition = { offset ->
 *                 renderer.getValueAtPosition(offset)
 *             }
 *         )
 * ) {
 *     // Draw chart
 * }
 * ```
 *
 * @param viewportState The viewport state to update during gestures
 * @param config Gesture configuration
 * @param zoomMode The zoom constraint mode (default: HORIZONTAL_AND_VERTICAL)
 * @param onValueSelected Optional callback when a value is selected
 * @param onValueDeselected Optional callback when selection is cleared
 * @param getValueAtPosition Optional function to determine value at position
 */
fun Modifier.chartGestures(
    viewportState: ViewportState,
    config: GestureConfig = GestureConfig(),
    zoomMode: ZoomMode = ZoomMode.HORIZONTAL_AND_VERTICAL,
    onValueSelected: ((SelectedValue) -> Unit)? = null,
    onValueDeselected: (() -> Unit)? = null,
    getValueAtPosition: ((Offset) -> SelectedValue?)? = null
): Modifier {
    var modifier = this

    // Apply scroll first (bottom layer)
    if (config.scrollEnabled) {
        modifier = modifier.chartScroll(viewportState, enabled = true)
    }

    // Apply zoom (middle layer)
    if (config.zoomEnabled) {
        modifier = modifier.chartZoom(
            viewportState = viewportState,
            zoomMode = zoomMode,
            minZoom = config.minZoom,
            maxZoom = config.maxZoom,
            enabled = true
        )
    }

    // Apply selection (top layer - doesn't interfere with zoom/scroll)
    if (config.selectionEnabled && onValueSelected != null && getValueAtPosition != null) {
        modifier = modifier.chartValueSelection(
            onValueSelected = onValueSelected,
            onValueDeselected = onValueDeselected,
            getValueAtPosition = getValueAtPosition,
            enabled = true
        )
    }

    return modifier
}

/**
 * Apply double-tap to reset viewport gesture.
 *
 * Enables double-tap gesture to reset the viewport to its maximum extent.
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .chartDoubleTapReset(
 *             viewportState = viewportState
 *         )
 * ) {
 *     // Draw chart
 * }
 * ```
 *
 * @param viewportState The viewport state to reset
 * @param enabled Whether double-tap reset is enabled (default: true)
 * @param onReset Optional callback when viewport is reset
 */
fun Modifier.chartDoubleTapReset(
    viewportState: ViewportState,
    enabled: Boolean = true,
    onReset: (() -> Unit)? = null
): Modifier = if (enabled) {
    pointerInput(viewportState) {
        detectTapGestures(
            onDoubleTap = {
                viewportState.resetViewport()
                onReset?.invoke()
            }
        )
    }
} else {
    this
}
