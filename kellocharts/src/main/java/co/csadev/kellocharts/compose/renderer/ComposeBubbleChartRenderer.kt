package co.csadev.kellocharts.compose.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.model.BubbleChartData
import co.csadev.kellocharts.model.BubbleValue
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport
import kotlin.math.sqrt

/**
 * Compose-based renderer for bubble charts.
 *
 * Supports the following bubble chart features:
 * - **Variable bubble sizes**: Bubble radius represents the Z value
 * - **Custom colors**: Per-bubble colors
 * - **Bubble scale**: Global scaling factor for bubble sizes
 * - **Minimum radius**: Ensure small bubbles are still visible
 * - **Transparency**: Bubbles can have alpha for overlapping visualization
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(modifier = Modifier.fillMaxSize()) {
 *     val renderer = ComposeBubbleChartRenderer(bubbleChartData)
 *     renderer.onSizeChanged(size, contentRect)
 *     renderer.draw(this, size, viewport)
 * }
 * ```
 *
 * @param data The BubbleChartData containing bubbles to render
 *
 * @see BubbleChartData
 * @see BubbleValue
 */
class ComposeBubbleChartRenderer(
    private var data: BubbleChartData
) : ComposeChartRenderer {

    private var size: Size = Size.Zero
    private var contentRect: Rect = Rect.Zero
    private var maxBubbleZ: Float = 0f

    /**
     * Update the chart data.
     * Call this when the data changes to trigger a redraw.
     */
    fun updateData(newData: BubbleChartData) {
        data = newData
        onDataChanged()
    }

    override fun onSizeChanged(size: Size, contentRect: Rect) {
        this.size = size
        this.contentRect = contentRect
    }

    override fun onDataChanged() {
        // Find maximum Z value for scaling
        maxBubbleZ = data.values.maxOfOrNull { it.z } ?: 1f
    }

    override fun onViewportChanged(viewport: Viewport) {
        // Opportunity to optimize by culling off-screen bubbles
    }

    override fun draw(drawScope: DrawScope, size: Size, viewport: Viewport) {
        with(drawScope) {
            data.values.forEach { bubble ->
                drawBubble(bubble, viewport, size)
            }
        }
    }

    /**
     * Draw a single bubble.
     */
    private fun DrawScope.drawBubble(bubble: BubbleValue, viewport: Viewport, size: Size) {
        val center = bubbleToOffset(bubble, viewport, size)
        val radius = calculateBubbleRadius(bubble.z)

        drawCircle(
            color = Color(bubble.color),
            radius = radius,
            center = center,
            style = Fill
        )
    }

    /**
     * Calculate bubble radius based on Z value.
     */
    private fun DrawScope.calculateBubbleRadius(z: Float): Float {
        val minRadius = data.minBubbleRadius.dp.toPx()

        if (maxBubbleZ == 0f) return minRadius

        // Scale radius based on Z value relative to max
        val normalizedZ = z / maxBubbleZ
        val scaledRadius = normalizedZ * data.bubbleScale * 50.dp.toPx() // Base max size

        return maxOf(minRadius, scaledRadius)
    }

    override fun getValueAtPosition(position: Offset, viewport: Viewport): SelectedValue? {
        // Check bubbles in reverse order (last drawn = on top)
        data.values.asReversed().forEachIndexed { reverseIndex, bubble ->
            val index = data.values.size - 1 - reverseIndex
            val bubbleCenter = bubbleToOffset(bubble, viewport, size)
            val radius = calculateBubbleRadius(bubble.z)

            val dx = position.x - bubbleCenter.x
            val dy = position.y - bubbleCenter.y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= radius) {
                return SelectedValue().apply {
                    set(index, index, SelectedValue.SelectedValueType.NONE)
                }
            }
        }

        return null
    }

    /**
     * Convert a BubbleValue to screen coordinates.
     */
    private fun bubbleToOffset(bubble: BubbleValue, viewport: Viewport, size: Size): Offset {
        val x = valueToX(bubble.x, viewport, size)
        val y = valueToY(bubble.y, viewport, size)
        return Offset(x, y)
    }

    /**
     * Convert bubble radius to screen scale.
     */
    private fun DrawScope.calculateBubbleRadius(z: Float): Float {
        val minRadius = data.minBubbleRadius.dp.toPx()

        if (maxBubbleZ == 0f) return minRadius

        // Scale radius based on Z value relative to max
        val normalizedZ = z / maxBubbleZ
        val scaledRadius = normalizedZ * data.bubbleScale * 50.dp.toPx()

        return maxOf(minRadius, scaledRadius)
    }

    /**
     * Convert an X value to screen coordinate.
     */
    private fun valueToX(value: Float, viewport: Viewport, size: Size): Float {
        val viewportWidth = viewport.right - viewport.left
        if (viewportWidth == 0f) return contentRect.center.x

        val normalized = (value - viewport.left) / viewportWidth
        return contentRect.left + normalized * contentRect.width
    }

    /**
     * Convert a Y value to screen coordinate.
     */
    private fun valueToY(value: Float, viewport: Viewport, size: Size): Float {
        val viewportHeight = viewport.bottom - viewport.top
        if (viewportHeight == 0f) return contentRect.center.y

        val normalized = (value - viewport.top) / viewportHeight
        // Invert Y axis (screen Y increases downward, chart Y increases upward)
        return contentRect.bottom - normalized * contentRect.height
    }
}
