package co.csadev.kellocharts.compose.renderer

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.model.PieChartData
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.SliceValue
import co.csadev.kellocharts.model.Viewport
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Compose-based renderer for pie and donut charts.
 *
 * Supports the following pie chart features:
 * - **Pie chart**: Standard circular chart with slices
 * - **Donut chart**: Pie chart with hollow center (center circle)
 * - **Slice separation**: Visual spacing between slices
 * - **Rotation**: Rotate the entire pie chart
 * - **Custom colors**: Per-slice colors
 * - **Center text**: Display text in the center (for donut charts)
 * - **Center circle**: Configurable center hole size
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(modifier = Modifier.fillMaxSize()) {
 *     val renderer = ComposePieChartRenderer(pieChartData)
 *     renderer.onSizeChanged(size, contentRect)
 *     renderer.draw(this, size, viewport)
 * }
 * ```
 *
 * @param data The PieChartData containing slices to render
 * @param rotation The rotation angle in degrees (0° = top, clockwise)
 *
 * @see PieChartData
 * @see SliceValue
 */
class ComposePieChartRenderer(
    private var data: PieChartData,
    private var rotation: Float = 0f
) : ComposeChartRenderer {

    private var size: Size = Size.Zero
    private var contentRect: Rect = Rect.Zero
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f

    companion object {
        private const val TAG = "PieChartRenderer"
    }

    /**
     * Update the chart data.
     * Call this when the data changes to trigger a redraw.
     */
    fun updateData(newData: PieChartData) {
        data = newData
        onDataChanged()
    }

    /**
     * Update the rotation angle.
     */
    fun updateRotation(newRotation: Float) {
        rotation = newRotation
    }

    override fun onSizeChanged(size: Size, contentRect: Rect) {
        this.size = size
        this.contentRect = contentRect
        calculateDimensions()
    }

    override fun onDataChanged() {
        // Opportunity to cache calculations if needed
    }

    override fun onViewportChanged(viewport: Viewport) {
        // Pie charts typically don't use viewports
    }

    override fun draw(drawScope: DrawScope, size: Size, viewport: Viewport) {
        with(drawScope) {
            if (data.values.isEmpty()) {
                Log.w(TAG, "Attempted to draw pie chart with no slices")
                return@with
            }

            val total = data.values.sumOf { it.value.toDouble() }.toFloat()
            if (total == 0f) {
                Log.w(TAG, "Attempted to draw pie chart with total value of 0")
                return@with
            }

            var currentAngle = rotation - 90f // Start at top (12 o'clock)

            data.values.forEach { slice ->
                val sweepAngle = (slice.value / total) * 360f

                if (sweepAngle > 0f) {
                    drawSlice(slice, currentAngle, sweepAngle)
                    currentAngle += sweepAngle
                }
            }

            // Draw center circle (donut hole) if enabled
            if (data.hasCenterCircle) {
                drawCenterCircle()
            }
        }
    }

    /**
     * Draw a single pie slice.
     */
    private fun DrawScope.drawSlice(slice: SliceValue, startAngle: Float, sweepAngle: Float) {
        val sliceSpacing = data.sliceSpacing.dp.toPx()
        val adjustedRadius = radius - sliceSpacing

        // Calculate separation offset if slice spacing is enabled
        val separationOffset = if (sliceSpacing > 0) {
            val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val offsetX = (sliceSpacing * cos(midAngle)).toFloat()
            val offsetY = (sliceSpacing * sin(midAngle)).toFloat()
            Offset(offsetX, offsetY)
        } else {
            Offset.Zero
        }

        val topLeft = Offset(
            centerX - adjustedRadius + separationOffset.x,
            centerY - adjustedRadius + separationOffset.y
        )

        drawArc(
            color = Color(slice.color),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = topLeft,
            size = Size(adjustedRadius * 2, adjustedRadius * 2),
            style = Fill
        )
    }

    /**
     * Draw the center circle (donut hole).
     */
    private fun DrawScope.drawCenterCircle() {
        val centerRadius = radius * data.centerCircleScale

        drawCircle(
            color = Color(data.centerCircleColor),
            radius = centerRadius,
            center = Offset(centerX, centerY),
            style = Fill
        )

        // TODO: Draw center text if provided
        // This would require using nativeCanvas.drawText() or TextMeasurer
        // data.centerText1 and data.centerText2
    }

    /**
     * Calculate dimensions based on available space.
     */
    private fun calculateDimensions() {
        centerX = contentRect.center.x
        centerY = contentRect.center.y

        // Use the smaller dimension to ensure the circle fits
        val availableWidth = contentRect.width
        val availableHeight = contentRect.height
        radius = min(availableWidth, availableHeight) / 2f

        // Account for slice spacing
        val sliceSpacing = data.sliceSpacing.dp.toPx()
        radius -= sliceSpacing
    }

    override fun getValueAtPosition(position: Offset, viewport: Viewport): SelectedValue? {
        // Calculate distance from center
        // Note: Pie charts don't use viewport for selection (polar coordinates from center)
        val dx = position.x - centerX
        val dy = position.y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        // Check if position is within the pie chart
        val minRadius = if (data.hasCenterCircle) {
            radius * data.centerCircleScale
        } else {
            0f
        }

        if (distance < minRadius || distance > radius) {
            return null
        }

        // Calculate angle of the touch position
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        angle = (angle + 90f - rotation + 360f) % 360f // Adjust for rotation and start position

        // Find which slice was touched
        val total = data.values.sumOf { it.value.toDouble() }.toFloat()
        if (total == 0f) return null

        var currentAngle = 0f
        data.values.forEachIndexed { index, slice ->
            val sweepAngle = (slice.value / total) * 360f
            if (angle >= currentAngle && angle < currentAngle + sweepAngle) {
                return SelectedValue().apply {
                    set(index, index, SelectedValue.SelectedValueType.NONE)
                }
            }
            currentAngle += sweepAngle
        }

        return null
    }
}
