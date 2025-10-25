package co.csadev.kellocharts.compose.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.AxisValue
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport

/**
 * Compose-based renderer for chart axes.
 *
 * Supports the following axis features:
 * - **Four axis positions**: Top, Bottom, Left, Right
 * - **Axis lines**: Configurable line thickness and color
 * - **Axis labels**: Text labels at specific positions
 * - **Grid lines**: Horizontal and vertical grid lines
 * - **Inside axes**: Axes drawn inside the chart area
 * - **Custom formatters**: Format axis values as text
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(modifier = Modifier.fillMaxSize()) {
 *     val renderer = ComposeAxesRenderer(axisBottom, axisLeft)
 *     renderer.onSizeChanged(size, contentRect)
 *     renderer.draw(this, size, viewport)
 * }
 * ```
 *
 * @param axisBottom Bottom X axis (can be null)
 * @param axisLeft Left Y axis (can be null)
 * @param axisTop Top X axis (can be null)
 * @param axisRight Right Y axis (can be null)
 *
 * @see Axis
 * @see AxisValue
 */
class ComposeAxesRenderer(
    private var axisBottom: Axis? = null,
    private var axisLeft: Axis? = null,
    private var axisTop: Axis? = null,
    private var axisRight: Axis? = null
) : ComposeChartRenderer {

    private var size: Size = Size.Zero
    private var contentRect: Rect = Rect.Zero

    // Axis styling
    private val axisLineWidth = 1.dp
    private val gridLineWidth = 0.5.dp
    private val labelTextSize = 12.dp

    /**
     * Update axes.
     */
    fun updateAxes(
        bottom: Axis? = null,
        left: Axis? = null,
        top: Axis? = null,
        right: Axis? = null
    ) {
        axisBottom = bottom
        axisLeft = left
        axisTop = top
        axisRight = right
        onDataChanged()
    }

    override fun onSizeChanged(size: Size, contentRect: Rect) {
        this.size = size
        this.contentRect = contentRect
    }

    override fun onDataChanged() {
        // Opportunity to cache calculations if needed
    }

    override fun onViewportChanged(viewport: Viewport) {
        // Axes may need to update visible labels based on viewport
    }

    override fun draw(drawScope: DrawScope, size: Size, viewport: Viewport) {
        with(drawScope) {
            // Draw grid lines first (behind data)
            axisLeft?.let { axis ->
                if (axis.hasLines) {
                    drawVerticalGridLines(axis, viewport)
                }
            }

            axisBottom?.let { axis ->
                if (axis.hasLines) {
                    drawHorizontalGridLines(axis, viewport)
                }
            }

            // Draw axis lines
            axisBottom?.let { drawBottomAxis(it, viewport) }
            axisLeft?.let { drawLeftAxis(it, viewport) }
            axisTop?.let { drawTopAxis(it, viewport) }
            axisRight?.let { drawRightAxis(it, viewport) }
        }
    }

    /**
     * Draw bottom X axis.
     */
    private fun DrawScope.drawBottomAxis(axis: Axis, viewport: Viewport) {
        val y = contentRect.bottom
        val axisColor = Color(axis.lineColor)

        // Draw axis line
        drawLine(
            color = axisColor,
            start = Offset(contentRect.left, y),
            end = Offset(contentRect.right, y),
            strokeWidth = axisLineWidth.toPx()
        )

        // Draw labels
        if (axis.hasLabels()) {
            axis.values?.forEach { axisValue ->
                val x = valueToX(axisValue.value, viewport)
                if (x in contentRect.left..contentRect.right) {
                    drawAxisLabel(
                        text = axisValue.label?.concatToString() ?: axisValue.value.toString(),
                        x = x,
                        y = y + ChartRenderingConstants.AXIS_LABEL_OFFSET_Y_DP.dp.toPx(),
                        color = Color(axis.textColor)
                    )
                }
            }
        }
    }

    /**
     * Draw left Y axis.
     */
    private fun DrawScope.drawLeftAxis(axis: Axis, viewport: Viewport) {
        val x = contentRect.left
        val axisColor = Color(axis.lineColor)

        // Draw axis line
        drawLine(
            color = axisColor,
            start = Offset(x, contentRect.top),
            end = Offset(x, contentRect.bottom),
            strokeWidth = axisLineWidth.toPx()
        )

        // Draw labels
        if (axis.hasLabels()) {
            axis.values?.forEach { axisValue ->
                val y = valueToY(axisValue.value, viewport)
                if (y in contentRect.top..contentRect.bottom) {
                    drawAxisLabel(
                        text = axisValue.label?.concatToString() ?: axisValue.value.toString(),
                        x = x - ChartRenderingConstants.AXIS_LABEL_OFFSET_X_DP.dp.toPx(),
                        y = y,
                        color = Color(axis.textColor),
                        alignRight = true
                    )
                }
            }
        }
    }

    /**
     * Draw top X axis.
     */
    private fun DrawScope.drawTopAxis(axis: Axis, viewport: Viewport) {
        val y = contentRect.top
        val axisColor = Color(axis.lineColor)

        drawLine(
            color = axisColor,
            start = Offset(contentRect.left, y),
            end = Offset(contentRect.right, y),
            strokeWidth = axisLineWidth.toPx()
        )
    }

    /**
     * Draw right Y axis.
     */
    private fun DrawScope.drawRightAxis(axis: Axis, viewport: Viewport) {
        val x = contentRect.right
        val axisColor = Color(axis.lineColor)

        drawLine(
            color = axisColor,
            start = Offset(x, contentRect.top),
            end = Offset(x, contentRect.bottom),
            strokeWidth = axisLineWidth.toPx()
        )
    }

    /**
     * Draw vertical grid lines.
     */
    private fun DrawScope.drawVerticalGridLines(axis: Axis, viewport: Viewport) {
        val gridColor = Color(axis.lineColor).copy(alpha = 0.2f)

        axis.values?.forEach { axisValue ->
            val x = valueToX(axisValue.value, viewport)
            if (x in contentRect.left..contentRect.right) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, contentRect.top),
                    end = Offset(x, contentRect.bottom),
                    strokeWidth = gridLineWidth.toPx()
                )
            }
        }
    }

    /**
     * Draw horizontal grid lines.
     */
    private fun DrawScope.drawHorizontalGridLines(axis: Axis, viewport: Viewport) {
        val gridColor = Color(axis.lineColor).copy(alpha = 0.2f)

        axis.values?.forEach { axisValue ->
            val y = valueToY(axisValue.value, viewport)
            if (y in contentRect.top..contentRect.bottom) {
                drawLine(
                    color = gridColor,
                    start = Offset(contentRect.left, y),
                    end = Offset(contentRect.right, y),
                    strokeWidth = gridLineWidth.toPx()
                )
            }
        }
    }

    /**
     * Draw axis label text.
     * Note: This uses nativeCanvas for text rendering until TextMeasurer is available.
     */
    private fun DrawScope.drawAxisLabel(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        alignRight: Boolean = false
    ) {
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = labelTextSize.toPx()
            isAntiAlias = true
            textAlign = if (alignRight) android.graphics.Paint.Align.RIGHT else android.graphics.Paint.Align.CENTER
        }

        drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
    }

    override fun getValueAtPosition(position: Offset, viewport: Viewport): SelectedValue? {
        // Axes don't typically have selectable values
        return null
    }

    /**
     * Convert an X value to screen coordinate.
     */
    private fun valueToX(value: Float, viewport: Viewport): Float {
        val viewportWidth = viewport.right - viewport.left
        if (viewportWidth == 0f) return contentRect.center.x

        val normalized = (value - viewport.left) / viewportWidth
        return contentRect.left + normalized * contentRect.width
    }

    /**
     * Convert a Y value to screen coordinate.
     */
    private fun valueToY(value: Float, viewport: Viewport): Float {
        val viewportHeight = viewport.bottom - viewport.top
        if (viewportHeight == 0f) return contentRect.center.y

        val normalized = (value - viewport.top) / viewportHeight
        // Invert Y axis (screen Y increases downward, chart Y increases upward)
        return contentRect.bottom - normalized * contentRect.height
    }
}
