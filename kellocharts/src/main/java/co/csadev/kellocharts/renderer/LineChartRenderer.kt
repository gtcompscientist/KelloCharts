package co.csadev.kellocharts.renderer

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.PorterDuff.Mode
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.model.SelectedValue.SelectedValueType
import co.csadev.kellocharts.provider.LineChartDataProvider
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart

/**
 * Renderer for line chart. Can draw lines, cubic lines, filled area chart and scattered chart.
 */
open class LineChartRenderer(
    context: Context,
    chart: Chart,
    private val dataProvider: LineChartDataProvider
) : AbstractChartRenderer(context, chart) {

    private val checkPrecision: Int

    private var baseValue: Float = 0f

    private val touchToleranceMargin: Int
    private val path = Path()
    private val linePaint = Paint()
    private val pointPaint = Paint()

    private var softwareBitmap: Bitmap? = null
    private val softwareCanvas = Canvas()
    private val tempMaximumViewport = Viewport()

    init {

        touchToleranceMargin = ChartUtils.dp2px(density, DEFAULT_TOUCH_TOLERANCE_MARGIN_DP)

        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Cap.ROUND
        linePaint.strokeWidth = ChartUtils.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP).toFloat()

        pointPaint.isAntiAlias = true
        pointPaint.style = Paint.Style.FILL

        checkPrecision = ChartUtils.dp2px(density, 2)
    }

    override fun onChartSizeChanged() {
        val internalMargin = calculateContentRectInternalMargin()
        computator.insetContentRectByInternalMargins(
            internalMargin, internalMargin,
            internalMargin, internalMargin
        )
        if (computator.chartWidth > 0 && computator.chartHeight > 0) {
            softwareBitmap = Bitmap.createBitmap(
                computator.chartWidth, computator.chartHeight,
                Bitmap.Config.ARGB_8888
            )
            softwareCanvas.setBitmap(softwareBitmap)
        }
    }

    override fun onChartDataChanged() {
        super.onChartDataChanged()
        val internalMargin = calculateContentRectInternalMargin()
        computator.insetContentRectByInternalMargins(
            internalMargin, internalMargin,
            internalMargin, internalMargin
        )
        baseValue = dataProvider.lineChartData.baseValue

        onChartViewportChanged()
    }

    override fun onChartViewportChanged() {
        if (isViewportCalculationEnabled) {
            calculateMaxViewport()
            computator.maximumViewport = tempMaximumViewport
            computator.currentViewport = computator.maximumViewport
        }
    }

    override fun draw(canvas: Canvas) {
        val data = dataProvider.lineChartData

        val drawCanvas: Canvas

        // softwareBitmap can be null if chart is rendered in layout editor. In that case use default canvas and not
        // softwareCanvas.
        if (null != softwareBitmap) {
            drawCanvas = softwareCanvas
            drawCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR)
        } else {
            drawCanvas = canvas
        }

        data.lines
            .filter { it.hasLines }
            .forEach {
                when {
                    it.isCubic -> drawSmoothPath(drawCanvas, it)
                    it.isSquare -> drawSquarePath(drawCanvas, it)
                    else -> drawPath(drawCanvas, it)
                }
            }

        if (null != softwareBitmap) {
            canvas.drawBitmap(softwareBitmap!!, 0f, 0f, null)
        }
    }

    override fun drawUnclipped(canvas: Canvas) {
        val data = dataProvider.lineChartData
        var lineIndex = 0
        for (line in data.lines) {
            if (checkIfShouldDrawPoints(line)) {
                drawPoints(canvas, line, lineIndex, MODE_DRAW)
            }
            ++lineIndex
        }
        if (isTouched) {
            // Redraw touched point to bring it to the front
            highlightPoints(canvas)
        }
    }

    private fun checkIfShouldDrawPoints(line: Line): Boolean {
        return line.hasPoints || line.values.size == 1
    }

    override fun checkTouch(touchX: Float, touchY: Float): Boolean {
        selectedValue.clear()
        val data = dataProvider.lineChartData
        var lineIndex = 0
        for (line in data.lines) {
            if (checkIfShouldDrawPoints(line)) {
                val pointRadius = ChartUtils.dp2px(density, line.pointRadius)
                var valueIndex = 0
                for (pointValue in line.values) {
                    val rawValueX = computator.computeRawX(pointValue.x)
                    val rawValueY = computator.computeRawY(pointValue.y)
                    if (isInArea(
                            rawValueX,
                            rawValueY,
                            touchX,
                            touchY,
                            (pointRadius + touchToleranceMargin).toFloat()
                        )
                    ) {
                        selectedValue[lineIndex, valueIndex] = SelectedValueType.LINE
                    }
                    ++valueIndex
                }
            }
            ++lineIndex
        }
        return isTouched
    }

    private fun calculateMaxViewport() {
        tempMaximumViewport.set(
            java.lang.Float.MAX_VALUE,
            java.lang.Float.MIN_VALUE,
            java.lang.Float.MIN_VALUE,
            java.lang.Float.MAX_VALUE
        )
        val data = dataProvider.lineChartData

        for (line in data.lines) {
            // Calculate max and min for viewport.
            for (pointValue in line.values) {
                if (pointValue.x < tempMaximumViewport.left) {
                    tempMaximumViewport.left = pointValue.x
                }
                if (pointValue.x > tempMaximumViewport.right) {
                    tempMaximumViewport.right = pointValue.x
                }
                if (pointValue.y < tempMaximumViewport.bottom) {
                    tempMaximumViewport.bottom = pointValue.y
                }
                if (pointValue.y > tempMaximumViewport.top) {
                    tempMaximumViewport.top = pointValue.y
                }
            }
        }
    }

    private fun calculateContentRectInternalMargin(): Int {
        var contentAreaMargin = 0
        val data = dataProvider.lineChartData
        for (line in data.lines) {
            if (checkIfShouldDrawPoints(line)) {
                val margin = line.pointRadius + DEFAULT_TOUCH_TOLERANCE_MARGIN_DP
                if (margin > contentAreaMargin) {
                    contentAreaMargin = margin
                }
            }
        }
        return ChartUtils.dp2px(density, contentAreaMargin)
    }

    /**
     * Draws lines, uses path for drawing filled area on software canvas. Line is drawn with canvas.drawLines() method.
     */
    private fun drawPath(canvas: Canvas, line: Line) {
        prepareLinePaint(line)

        var valueIndex = 0
        for (pointValue in line.values) {

            val rawX = computator.computeRawX(pointValue.x)
            val rawY = computator.computeRawY(pointValue.y)

            if (valueIndex == 0) {
                path.moveTo(rawX, rawY)
            } else {
                path.lineTo(rawX, rawY)
            }

            ++valueIndex
        }

        canvas.drawPath(path, linePaint)

        if (line.isFilled) {
            drawArea(canvas, line)
        }

        path.reset()
    }

    private fun drawSquarePath(canvas: Canvas, line: Line) {
        prepareLinePaint(line)

        var valueIndex = 0
        var previousRawY = 0f
        for (pointValue in line.values) {

            val rawX = computator.computeRawX(pointValue.x)
            val rawY = computator.computeRawY(pointValue.y)

            if (valueIndex == 0) {
                path.moveTo(rawX, rawY)
            } else {
                path.lineTo(rawX, previousRawY)
                path.lineTo(rawX, rawY)
            }

            previousRawY = rawY

            ++valueIndex
        }

        canvas.drawPath(path, linePaint)

        if (line.isFilled) {
            drawArea(canvas, line)
        }

        path.reset()
    }

    private fun drawSmoothPath(canvas: Canvas, line: Line) {
        prepareLinePaint(line)

        val lineSize = line.values.size
        var prePreviousPointX = java.lang.Float.NaN
        var prePreviousPointY = java.lang.Float.NaN
        var previousPointX = java.lang.Float.NaN
        var previousPointY = java.lang.Float.NaN
        var currentPointX = java.lang.Float.NaN
        var currentPointY = java.lang.Float.NaN
        var nextPointX = java.lang.Float.NaN
        var nextPointY = java.lang.Float.NaN

        for (valueIndex in 0 until lineSize) {
            if (java.lang.Float.isNaN(currentPointX)) {
                val linePoint = line.values[valueIndex]
                currentPointX = computator.computeRawX(linePoint.x)
                currentPointY = computator.computeRawY(linePoint.y)
            }
            if (java.lang.Float.isNaN(previousPointX)) {
                if (valueIndex > 0) {
                    val linePoint = line.values[valueIndex - 1]
                    previousPointX = computator.computeRawX(linePoint.x)
                    previousPointY = computator.computeRawY(linePoint.y)
                } else {
                    previousPointX = currentPointX
                    previousPointY = currentPointY
                }
            }

            if (java.lang.Float.isNaN(prePreviousPointX)) {
                if (valueIndex > 1) {
                    val linePoint = line.values[valueIndex - 2]
                    prePreviousPointX = computator.computeRawX(linePoint.x)
                    prePreviousPointY = computator.computeRawY(linePoint.y)
                } else {
                    prePreviousPointX = previousPointX
                    prePreviousPointY = previousPointY
                }
            }

            // nextPoint is always new one or it is equal currentPoint.
            if (valueIndex < lineSize - 1) {
                val linePoint = line.values[valueIndex + 1]
                nextPointX = computator.computeRawX(linePoint.x)
                nextPointY = computator.computeRawY(linePoint.y)
            } else {
                nextPointX = currentPointX
                nextPointY = currentPointY
            }

            if (valueIndex == 0) {
                // Move to start point.
                path.moveTo(currentPointX, currentPointY)
            } else {
                // Calculate control points.
                val firstDiffX = currentPointX - prePreviousPointX
                val firstDiffY = currentPointY - prePreviousPointY
                val secondDiffX = nextPointX - previousPointX
                val secondDiffY = nextPointY - previousPointY
                val firstControlPointX = previousPointX + LINE_SMOOTHNESS * firstDiffX
                val firstControlPointY = previousPointY + LINE_SMOOTHNESS * firstDiffY
                val secondControlPointX = currentPointX - LINE_SMOOTHNESS * secondDiffX
                val secondControlPointY = currentPointY - LINE_SMOOTHNESS * secondDiffY
                path.cubicTo(
                    firstControlPointX,
                    firstControlPointY,
                    secondControlPointX,
                    secondControlPointY,
                    currentPointX,
                    currentPointY
                )
            }

            // Shift values by one back to prevent recalculation of values that have
            // been already calculated.
            prePreviousPointX = previousPointX
            prePreviousPointY = previousPointY
            previousPointX = currentPointX
            previousPointY = currentPointY
            currentPointX = nextPointX
            currentPointY = nextPointY
        }

        canvas.drawPath(path, linePaint)
        if (line.isFilled) {
            drawArea(canvas, line)
        }
        path.reset()
    }

    private fun prepareLinePaint(line: Line) {
        linePaint.strokeWidth = ChartUtils.dp2px(density, line.strokeWidth).toFloat()
        linePaint.color = line.color
        linePaint.pathEffect = line.pathEffect
    }

    // TODO Drawing points can be done in the same loop as drawing lines but it
    // may cause problems in the future with
    // implementing point styles.
    private fun drawPoints(canvas: Canvas, line: Line, lineIndex: Int, mode: Int) {
        pointPaint.color = line.pointColor
        var valueIndex = 0
        for (pointValue in line.values) {
            val pointRadius = ChartUtils.dp2px(density, line.pointRadius)
            val rawX = computator.computeRawX(pointValue.x)
            val rawY = computator.computeRawY(pointValue.y)
            if (computator.isWithinContentRect(rawX, rawY, checkPrecision.toFloat())) {
                // Draw points only if they are within contentRectMinusAllMargins, using contentRectMinusAllMargins
                // instead of viewport to avoid some
                // float rounding problems.
                if (MODE_DRAW == mode) {
                    drawPoint(canvas, line, pointValue, rawX, rawY, pointRadius.toFloat())
                    if (line.hasLabels) {
                        drawLabel(
                            canvas,
                            line,
                            pointValue,
                            rawX,
                            rawY,
                            (pointRadius + labelOffset).toFloat()
                        )
                    }
                } else if (MODE_HIGHLIGHT == mode) {
                    highlightPoint(canvas, line, pointValue, rawX, rawY, lineIndex, valueIndex)
                } else {
                    throw IllegalStateException("Cannot process points in mode: " + mode)
                }
            }
            ++valueIndex
        }
    }

    private fun drawPoint(
        canvas: Canvas,
        line: Line,
        pointValue: PointValue,
        rawX: Float,
        rawY: Float,
        pointRadius: Float
    ) {
        if (ValueShape.SQUARE == line.shape) {
            canvas.drawRect(
                rawX - pointRadius, rawY - pointRadius, rawX + pointRadius, rawY + pointRadius,
                pointPaint
            )
        } else if (ValueShape.CIRCLE == line.shape) {
            canvas.drawCircle(rawX, rawY, pointRadius, pointPaint)
        } else if (ValueShape.DIAMOND == line.shape) {
            canvas.save()
            canvas.rotate(45f, rawX, rawY)
            canvas.drawRect(
                rawX - pointRadius, rawY - pointRadius, rawX + pointRadius, rawY + pointRadius,
                pointPaint
            )
            canvas.restore()
        } else {
            throw IllegalArgumentException("Invalid point shape: " + line.shape)
        }
    }

    private fun highlightPoints(canvas: Canvas) {
        val lineIndex = selectedValue.firstIndex
        val line = dataProvider.lineChartData.lines[lineIndex]
        drawPoints(canvas, line, lineIndex, MODE_HIGHLIGHT)
    }

    private fun highlightPoint(
        canvas: Canvas,
        line: Line,
        pointValue: PointValue,
        rawX: Float,
        rawY: Float,
        lineIndex: Int,
        valueIndex: Int
    ) {
        if (selectedValue.firstIndex == lineIndex && selectedValue.secondIndex == valueIndex) {
            val pointRadius = ChartUtils.dp2px(density, line.pointRadius)
            pointPaint.color = line.darkenColor
            drawPoint(
                canvas,
                line,
                pointValue,
                rawX,
                rawY,
                (pointRadius + touchToleranceMargin).toFloat()
            )
            if (line.hasLabels || line.hasLabelsOnlyForSelected) {
                drawLabel(
                    canvas,
                    line,
                    pointValue,
                    rawX,
                    rawY,
                    (pointRadius + labelOffset).toFloat()
                )
            }
        }
    }

    private fun drawLabel(
        canvas: Canvas,
        line: Line,
        pointValue: PointValue,
        rawX: Float,
        rawY: Float,
        offset: Float
    ) {
        val contentRect = computator.contentRectMinusAllMargins
        val numChars = line.formatter.formatChartValue(labelBuffer, pointValue)
        if (numChars == 0) {
            // No need to draw empty label
            return
        }

        val labelWidth = labelPaint.measureText(labelBuffer, labelBuffer.size - numChars, numChars)
        val labelHeight = Math.abs(fontMetrics.ascent)
        var left = rawX - labelWidth / 2 - labelMargin.toFloat()
        var right = rawX + labelWidth / 2 + labelMargin.toFloat()

        var top: Float
        var bottom: Float

        if (pointValue.y >= baseValue) {
            top = rawY - offset - labelHeight.toFloat() - (labelMargin * 2).toFloat()
            bottom = rawY - offset
        } else {
            top = rawY + offset
            bottom = rawY + offset + labelHeight.toFloat() + (labelMargin * 2).toFloat()
        }

        if (top < contentRect.top) {
            top = rawY + offset
            bottom = rawY + offset + labelHeight.toFloat() + (labelMargin * 2).toFloat()
        }
        if (bottom > contentRect.bottom) {
            top = rawY - offset - labelHeight.toFloat() - (labelMargin * 2).toFloat()
            bottom = rawY - offset
        }
        if (left < contentRect.left) {
            left = rawX
            right = rawX + labelWidth + (labelMargin * 2).toFloat()
        }
        if (right > contentRect.right) {
            left = rawX - labelWidth - (labelMargin * 2).toFloat()
            right = rawX
        }

        labelBackgroundRect.set(left, top, right, bottom)
        drawLabelTextAndBackground(
            canvas, labelBuffer, labelBuffer.size - numChars, numChars,
            line.darkenColor
        )
    }

    private fun drawArea(canvas: Canvas, line: Line) {
        val lineSize = line.values.size
        if (lineSize < 2) {
            // No point to draw area for one point or empty line.
            return
        }

        val contentRect = computator.contentRectMinusAllMargins
        val baseRawValue = Math.min(
            contentRect.bottom.toFloat(),
            Math.max(
                computator.computeRawY(baseValue),
                contentRect.top.toFloat()
            )
        )
        // That checks works only if the last point is the right most one.
        val left = Math.max(computator.computeRawX(line.values[0].x), contentRect.left.toFloat())
        val right = Math.min(
            computator.computeRawX(line.values[lineSize - 1].x),
            contentRect.right.toFloat()
        )

        path.lineTo(right, baseRawValue)
        path.lineTo(left, baseRawValue)
        path.close()

        linePaint.style = Paint.Style.FILL
        linePaint.alpha = line.areaTransparency
        canvas.drawPath(path, linePaint)
        linePaint.style = Paint.Style.STROKE
    }

    private fun isInArea(x: Float, y: Float, touchX: Float, touchY: Float, radius: Float): Boolean {
        val diffX = touchX - x
        val diffY = touchY - y
        return Math.pow(diffX.toDouble(), 2.0) + Math.pow(diffY.toDouble(), 2.0) <= 2 * Math.pow(
            radius.toDouble(),
            2.0
        )
    }

    companion object {
        private val LINE_SMOOTHNESS = 0.16f
        private val DEFAULT_LINE_STROKE_WIDTH_DP = 3
        private val DEFAULT_TOUCH_TOLERANCE_MARGIN_DP = 4

        private val MODE_DRAW = 0
        private val MODE_HIGHLIGHT = 1
    }
}
