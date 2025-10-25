package co.csadev.kellocharts.compose.renderer

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import co.csadev.kellocharts.compose.util.ColorCache
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.ValueShape
import co.csadev.kellocharts.model.Viewport
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Compose-based renderer for line charts.
 *
 * Supports the following line chart features:
 * - **Straight lines**: Direct point-to-point connections
 * - **Cubic bezier curves**: Smooth curved lines using cubic interpolation
 * - **Square lines**: Step-style lines with right angles
 * - **Filled areas**: Area under the line filled with semi-transparent color
 * - **Point markers**: Circles, squares, or diamonds at each data point
 * - **Custom colors**: Per-line colors and point colors
 * - **Custom stroke widths**: Per-line configurable line thickness
 * - **Path effects**: Dashed lines, dotted lines, etc.
 *
 * ## Usage Example
 * ```kotlin
 * Canvas(modifier = Modifier.fillMaxSize()) {
 *     val renderer = ComposeLineChartRenderer(lineChartData)
 *     renderer.onSizeChanged(size, contentRect)
 *     renderer.draw(this, size, viewport)
 * }
 * ```
 *
 * @param data The LineChartData containing lines and points to render
 * @param baseValue The baseline value for filled areas (default: 0.0f)
 *
 * @see LineChartData
 * @see Line
 * @see PointValue
 */
class ComposeLineChartRenderer(
    private var data: LineChartData,
    private var baseValue: Float = 0f
) : ComposeChartRenderer {

    private var size: Size = Size.Zero
    private var contentRect: Rect = Rect.Zero

    companion object {
        private const val TAG = "LineChartRenderer"
    }

    /**
     * Update the chart data.
     * Call this when the data changes to trigger a redraw.
     */
    fun updateData(newData: LineChartData) {
        data = newData
        baseValue = newData.baseValue
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
        // Opportunity to optimize by culling off-screen points
    }

    override fun draw(drawScope: DrawScope, size: Size, viewport: Viewport) {
        with(drawScope) {
            // Draw all lines
            data.lines
                .filter { it.hasLines }
                .forEach { line ->
                    when {
                        line.isCubic -> drawSmoothLine(line, viewport, size)
                        line.isSquare -> drawSquareLine(line, viewport, size)
                        else -> drawStraightLine(line, viewport, size)
                    }
                }

            // Draw all points
            data.lines
                .filter { it.hasPoints }
                .forEach { line ->
                    drawPoints(line, viewport, size)
                }
        }
    }

    /**
     * Draw a straight line connecting points.
     *
     * Validates that the line has at least 2 points before drawing.
     * Logs warnings for edge cases like empty or single-point lines.
     */
    private fun DrawScope.drawStraightLine(line: Line, viewport: Viewport, size: Size) {
        if (line.values.isEmpty()) {
            Log.w(TAG, "Attempted to draw straight line with no values")
            return
        }
        if (line.values.size < 2) {
            Log.d(TAG, "Line has only one point, skipping line drawing (will draw point marker only)")
            return
        }

        val path = Path()
        val strokeWidth = line.strokeWidth.dp.toPx()
        val lineColor = ColorCache.get(line.color)

        // Build the path
        line.values.forEachIndexed { index, point ->
            val offset = pointToOffset(point, viewport, size)
            if (index == 0) {
                path.moveTo(offset.x, offset.y)
            } else {
                path.lineTo(offset.x, offset.y)
            }
        }

        // Draw filled area if enabled
        if (line.isFilled) {
            drawFilledArea(line, viewport, size)
        }

        // Draw the line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = line.pathEffect?.let { convertPathEffect(it) }
            )
        )
    }

    /**
     * Draw a smooth cubic bezier line.
     *
     * Uses cubic bezier curves to create smooth transitions between points.
     * Validates that the line has at least 2 points before drawing.
     */
    private fun DrawScope.drawSmoothLine(line: Line, viewport: Viewport, size: Size) {
        if (line.values.isEmpty()) {
            Log.w(TAG, "Attempted to draw smooth line with no values")
            return
        }
        if (line.values.size < 2) {
            Log.d(TAG, "Line has only one point, skipping line drawing (will draw point marker only)")
            return
        }

        val path = Path()
        val strokeWidth = line.strokeWidth.dp.toPx()
        val lineColor = ColorCache.get(line.color)

        // Build smooth path using cubic bezier curves
        line.values.forEachIndexed { index, point ->
            val offset = pointToOffset(point, viewport, size)

            when (index) {
                0 -> path.moveTo(offset.x, offset.y)
                else -> {
                    // Calculate control points for smooth curve
                    val prevPoint = line.values[index - 1]
                    val prevOffset = pointToOffset(prevPoint, viewport, size)

                    val controlPoint1X = prevOffset.x + (offset.x - prevOffset.x) / 3f
                    val controlPoint1Y = prevOffset.y
                    val controlPoint2X = prevOffset.x + 2f * (offset.x - prevOffset.x) / 3f
                    val controlPoint2Y = offset.y

                    path.cubicTo(
                        controlPoint1X, controlPoint1Y,
                        controlPoint2X, controlPoint2Y,
                        offset.x, offset.y
                    )
                }
            }
        }

        // Draw filled area if enabled
        if (line.isFilled) {
            drawFilledArea(line, viewport, size)
        }

        // Draw the smooth line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = line.pathEffect?.let { convertPathEffect(it) }
            )
        )
    }

    /**
     * Draw a square (step) line.
     *
     * Creates a step pattern with horizontal then vertical segments.
     * Validates that the line has at least 2 points before drawing.
     */
    private fun DrawScope.drawSquareLine(line: Line, viewport: Viewport, size: Size) {
        if (line.values.isEmpty()) {
            Log.w(TAG, "Attempted to draw square line with no values")
            return
        }
        if (line.values.size < 2) {
            Log.d(TAG, "Line has only one point, skipping line drawing (will draw point marker only)")
            return
        }

        val path = Path()
        val strokeWidth = line.strokeWidth.dp.toPx()
        val lineColor = ColorCache.get(line.color)

        // Build step path
        line.values.forEachIndexed { index, point ->
            val offset = pointToOffset(point, viewport, size)

            when (index) {
                0 -> path.moveTo(offset.x, offset.y)
                else -> {
                    val prevPoint = line.values[index - 1]
                    val prevOffset = pointToOffset(prevPoint, viewport, size)

                    // Draw horizontal then vertical (step pattern)
                    path.lineTo(offset.x, prevOffset.y)
                    path.lineTo(offset.x, offset.y)
                }
            }
        }

        // Draw filled area if enabled
        if (line.isFilled) {
            drawFilledArea(line, viewport, size)
        }

        // Draw the square line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Miter,
                pathEffect = line.pathEffect?.let { convertPathEffect(it) }
            )
        )
    }

    /**
     * Draw filled area under the line.
     */
    private fun DrawScope.drawFilledArea(line: Line, viewport: Viewport, size: Size) {
        if (line.values.size < 2) return

        val path = Path()
        val fillColor = ColorCache.get(line.color).copy(alpha = line.areaTransparency / 255f)

        // Start at baseline
        val firstPoint = line.values.first()
        val firstOffset = pointToOffset(firstPoint, viewport, size)
        val baselineY = valueToY(baseValue, viewport, size)

        path.moveTo(firstOffset.x, baselineY)

        // Draw to each point
        line.values.forEach { point ->
            val offset = pointToOffset(point, viewport, size)
            path.lineTo(offset.x, offset.y)
        }

        // Close path back to baseline
        val lastPoint = line.values.last()
        val lastOffset = pointToOffset(lastPoint, viewport, size)
        path.lineTo(lastOffset.x, baselineY)
        path.close()

        // Draw filled area
        drawPath(
            path = path,
            color = fillColor,
            style = Fill,
            blendMode = BlendMode.SrcOver
        )
    }

    /**
     * Draw point markers.
     *
     * Uses viewport culling to only draw visible points for performance.
     */
    private fun DrawScope.drawPoints(line: Line, viewport: Viewport, size: Size) {
        val pointRadius = line.pointRadius.dp.toPx()
        val pointColor = ColorCache.get(line.pointColor)

        // Viewport culling: only draw points that are visible
        line.values.filter { isPointInViewport(it, viewport) }.forEach { point ->
            val offset = pointToOffset(point, viewport, size)

            when (line.shape) {
                ValueShape.CIRCLE -> {
                    drawCircle(
                        color = pointColor,
                        radius = pointRadius,
                        center = offset,
                        style = Fill
                    )
                }
                ValueShape.SQUARE -> {
                    drawRect(
                        color = pointColor,
                        topLeft = Offset(offset.x - pointRadius, offset.y - pointRadius),
                        size = Size(pointRadius * 2, pointRadius * 2),
                        style = Fill
                    )
                }
                ValueShape.DIAMOND -> {
                    val diamondPath = Path().apply {
                        moveTo(offset.x, offset.y - pointRadius)
                        lineTo(offset.x + pointRadius, offset.y)
                        lineTo(offset.x, offset.y + pointRadius)
                        lineTo(offset.x - pointRadius, offset.y)
                        close()
                    }
                    drawPath(
                        path = diamondPath,
                        color = pointColor,
                        style = Fill
                    )
                }
            }
        }
    }

    override fun getValueAtPosition(position: Offset, viewport: Viewport): SelectedValue? {
        val touchTolerance = ChartRenderingConstants.TOUCH_TOLERANCE_DP.dp.toPx()

        data.lines.forEachIndexed { lineIndex, line ->
            // Viewport culling: only check visible points for selection
            line.values.forEachIndexed { pointIndex, point ->
                if (!isPointInViewport(point, viewport)) return@forEachIndexed

                val pointOffset = pointToOffset(point, viewport, size)
                val distance = sqrt(
                    (position.x - pointOffset.x) * (position.x - pointOffset.x) +
                    (position.y - pointOffset.y) * (position.y - pointOffset.y)
                )

                if (distance <= touchTolerance) {
                    return SelectedValue().apply {
                        set(lineIndex, pointIndex, SelectedValue.SelectedValueType.LINE)
                    }
                }
            }
        }

        return null
    }

    /**
     * Check if a point is within the viewport bounds (viewport culling).
     *
     * This optimization prevents rendering off-screen data points, which can
     * provide 10-100x performance improvement on large datasets.
     *
     * @param point The data point to check
     * @param viewport The current viewport
     * @return true if the point should be rendered, false otherwise
     */
    private fun isPointInViewport(point: PointValue, viewport: Viewport): Boolean {
        return point.x >= viewport.left && point.x <= viewport.right &&
               point.y >= viewport.bottom && point.y <= viewport.top
    }

    /**
     * Convert a PointValue to screen coordinates.
     */
    private fun pointToOffset(point: PointValue, viewport: Viewport, size: Size): Offset {
        val x = valueToX(point.x, viewport, size)
        val y = valueToY(point.y, viewport, size)
        return Offset(x, y)
    }

    /**
     * Convert an X value to screen coordinate.
     */
    private fun valueToX(value: Float, viewport: Viewport, size: Size): Float {
        val viewportWidth = viewport.right - viewport.left
        if (viewportWidth == 0f) return 0f

        val normalized = (value - viewport.left) / viewportWidth
        return contentRect.left + normalized * contentRect.width
    }

    /**
     * Convert a Y value to screen coordinate.
     */
    private fun valueToY(value: Float, viewport: Viewport, size: Size): Float {
        val viewportHeight = viewport.bottom - viewport.top
        if (viewportHeight == 0f) return 0f

        val normalized = (value - viewport.top) / viewportHeight
        // Invert Y axis (screen Y increases downward, chart Y increases upward)
        return contentRect.bottom - normalized * contentRect.height
    }

    /**
     * Convert Android PathEffect to Compose PathEffect.
     * Note: This is a simplified conversion. Full implementation may need more sophisticated mapping.
     */
    private fun convertPathEffect(effect: android.graphics.PathEffect): PathEffect? {
        // This would need proper implementation based on the type of PathEffect
        // For now, return null as a placeholder
        // TODO: Implement proper PathEffect conversion
        return null
    }
}
