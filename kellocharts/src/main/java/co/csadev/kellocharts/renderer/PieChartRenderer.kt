package co.csadev.kellocharts.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.FontMetricsInt
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextUtils

import co.csadev.kellocharts.formatter.PieChartValueFormatter
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.model.SelectedValue.SelectedValueType
import co.csadev.kellocharts.provider.PieChartDataProvider
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart

/**
 * Default renderer for PieChart. PieChart doesn't use viewport concept so it a little different than others chart
 * types.
 */
class PieChartRenderer(context: Context, chart: Chart, private val dataProvider: PieChartDataProvider) : AbstractChartRenderer(context, chart) {
    var chartRotation = DEFAULT_START_ROTATION
        set(rotation) {
            var rotation = rotation
            rotation = (rotation % 360 + 360) % 360
            field = rotation
        }
    private val slicePaint = Paint()
    private var maxSum: Float = 0.toFloat()
    var circleOval = RectF()
    private val drawCircleOval = RectF()
    private val sliceVector = PointF()
    private val touchAdditional: Int
    /**
     * @see .setCircleFillRatio
     */
    /**
     * Set how much of view area should be taken by chart circle. Value should be between 0 and 1. Default is 1 so
     * circle will have radius equals min(View.width, View.height).
     */
    var circleFillRatio = 1.0f
        set(value) {
            field = Math.min(1f, Math.max(0f, value))
            calculateCircleOval()
        }

    // Center circle related attributes
    private var hasCenterCircle: Boolean = false
    private var centerCircleScale: Float = 0.toFloat()
    private val centerCirclePaint = Paint()
    // Text1
    private val centerCircleText1Paint = Paint()
    private val centerCircleText1FontMetrics = FontMetricsInt()
    // Text2
    private val centerCircleText2Paint = Paint()
    private val centerCircleText2FontMetrics = FontMetricsInt()
    // Separation lines
    private val separationLinesPaint = Paint()

    private var hasLabelsOutside: Boolean = false
    private var hasLabels: Boolean = false
    private var hasLabelsOnlyForSelected: Boolean = false
    private var valueFormatter: PieChartValueFormatter? = null
    private val tempMaximumViewport = Viewport()

    private var softwareBitmap: Bitmap? = null
    private val softwareCanvas = Canvas()

    init {
        touchAdditional = ChartUtils.dp2px(density, DEFAULT_TOUCH_ADDITIONAL_DP)

        slicePaint.isAntiAlias = true
        slicePaint.style = Paint.Style.FILL

        centerCirclePaint.isAntiAlias = true
        centerCirclePaint.style = Paint.Style.FILL
        centerCirclePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)

        centerCircleText1Paint.isAntiAlias = true
        centerCircleText1Paint.textAlign = Align.CENTER

        centerCircleText2Paint.isAntiAlias = true
        centerCircleText2Paint.textAlign = Align.CENTER

        separationLinesPaint.isAntiAlias = true
        separationLinesPaint.style = Paint.Style.STROKE
        separationLinesPaint.strokeCap = Paint.Cap.ROUND
        separationLinesPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        separationLinesPaint.color = Color.TRANSPARENT
    }

    override fun onChartSizeChanged() {
        calculateCircleOval()

        if (computator.chartWidth > 0 && computator.chartHeight > 0) {
            softwareBitmap = Bitmap.createBitmap(computator.chartWidth, computator.chartHeight,
                    Bitmap.Config.ARGB_8888)
            softwareCanvas.setBitmap(softwareBitmap)
        }
    }

    override fun onChartDataChanged() {
        super.onChartDataChanged()
        val data = dataProvider.pieChartData
        hasLabelsOutside = data.hasLabelsOutside
        hasLabels = data.hasLabels
        hasLabelsOnlyForSelected = data.hasLabelsOnlyForSelected
        valueFormatter = data.formatter
        hasCenterCircle = data.hasCenterCircle
        centerCircleScale = data.centerCircleScale
        centerCirclePaint.color = data.centerCircleColor
        if (null != data.centerText1Typeface) {
            centerCircleText1Paint.typeface = data.centerText1Typeface
        }
        centerCircleText1Paint.textSize = ChartUtils.sp2px(scaledDensity, data.centerText1FontSize).toFloat()
        centerCircleText1Paint.color = data.centerText1Color
        centerCircleText1Paint.getFontMetricsInt(centerCircleText1FontMetrics)
        if (null != data.centerText2Typeface) {
            centerCircleText2Paint.typeface = data.centerText2Typeface
        }
        centerCircleText2Paint.textSize = ChartUtils.sp2px(scaledDensity, data.centerText2FontSize).toFloat()
        centerCircleText2Paint.color = data.centerText2Color
        centerCircleText2Paint.getFontMetricsInt(centerCircleText2FontMetrics)

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
        // softwareBitmap can be null if chart is rendered in layout editor. In that case use default canvas and not
        // softwareCanvas.
        val drawCanvas: Canvas
        if (null != softwareBitmap) {
            drawCanvas = softwareCanvas
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        } else {
            drawCanvas = canvas
        }

        drawSlices(drawCanvas)
        drawSeparationLines(drawCanvas)
        if (hasCenterCircle) {
            drawCenterCircle(drawCanvas)
        }
        drawLabels(drawCanvas)

        if (null != softwareBitmap) {
            canvas.drawBitmap(softwareBitmap!!, 0f, 0f, null)
        }
    }

    override fun drawUnclipped(canvas: Canvas) {}

    override fun checkTouch(touchX: Float, touchY: Float): Boolean {
        selectedValue.clear()
        val data = dataProvider.pieChartData
        val centerX = circleOval.centerX()
        val centerY = circleOval.centerY()
        val circleRadius = circleOval.width() / 2f

        sliceVector.set(touchX - centerX, touchY - centerY)
        // Check if touch is on circle area, if not return false;
        if (sliceVector.length() > circleRadius + touchAdditional) {
            return false
        }
        // Check if touch is not in center circle, if yes return false;
        if (data.hasCenterCircle && sliceVector.length() < circleRadius * data.centerCircleScale) {
            return false
        }

        // Get touchAngle and align touch 0 degrees with chart 0 degrees, that why I subtracting start angle,
        // adding 360
        // and modulo 360 translates i.e -20 degrees to 340 degrees.
        val touchAngle = (pointToAngle(touchX, touchY, centerX, centerY) - chartRotation + 360f) % 360f
        val sliceScale = 360f / maxSum
        var lastAngle = 0f // No start angle here, see above
        var sliceIndex = 0
        for (sliceValue in data.values) {
            val angle = Math.abs(sliceValue.value) * sliceScale
            if (touchAngle >= lastAngle) {
                selectedValue[sliceIndex, sliceIndex] = SelectedValueType.NONE
            }
            lastAngle += angle
            ++sliceIndex
        }
        return isTouched
    }

    /**
     * Draw center circle with text if [PieChartData.hasCenterCircle] is set true.
     */
    private fun drawCenterCircle(canvas: Canvas) {
        val data = dataProvider.pieChartData
        val circleRadius = circleOval.width() / 2f
        val centerRadius = circleRadius * data.centerCircleScale
        val centerX = circleOval.centerX()
        val centerY = circleOval.centerY()

        canvas.drawCircle(centerX, centerY, centerRadius, centerCirclePaint)

        // Draw center text1 and text2 if not empty.
        if (!TextUtils.isEmpty(data.centerText1)) {

            val text1Height = Math.abs(centerCircleText1FontMetrics.ascent)

            if (!TextUtils.isEmpty(data.centerText2)) {
                // Draw text 2 only if text 1 is not empty.
                val text2Height = Math.abs(centerCircleText2FontMetrics.ascent)
                canvas.drawText(data.centerText1!!, centerX, centerY - text1Height * 0.2f, centerCircleText1Paint)
                canvas.drawText(data.centerText2!!, centerX, centerY + text2Height, centerCircleText2Paint)
            } else {
                canvas.drawText(data.centerText1!!, centerX, centerY + text1Height / 4, centerCircleText1Paint)
            }
        }
    }

    /**
     * Draw all slices for this PieChart, if mode == [.MODE_HIGHLIGHT] currently selected slices will be redrawn
     * and
     * highlighted.
     *
     * @param canvas
     */
    private fun drawSlices(canvas: Canvas) {
        val data = dataProvider.pieChartData
        val sliceScale = 360f / maxSum
        var lastAngle = chartRotation.toFloat()
        var sliceIndex = 0
        for (sliceValue in data.values) {
            val angle = Math.abs(sliceValue.value) * sliceScale
            if (isTouched && selectedValue.firstIndex == sliceIndex) {
                drawSlice(canvas, sliceValue, lastAngle, angle, MODE_HIGHLIGHT)
            } else {
                drawSlice(canvas, sliceValue, lastAngle, angle, MODE_DRAW)
            }
            lastAngle += angle
            ++sliceIndex
        }
    }

    private fun drawSeparationLines(canvas: Canvas) {
        val data = dataProvider.pieChartData
        if (data.values.size < 2) {
            //No need for separation lines for 0 or 1 slices.
            return
        }
        val sliceSpacing = ChartUtils.dp2px(density, data.sliceSpacing)
        if (sliceSpacing < 1) {
            //No need for separation lines
            return
        }
        val sliceScale = 360f / maxSum
        var lastAngle = chartRotation.toFloat()
        val circleRadius = circleOval.width() / 2f
        separationLinesPaint.strokeWidth = sliceSpacing.toFloat()
        for (sliceValue in data.values) {
            val angle = Math.abs(sliceValue.value) * sliceScale

            sliceVector.set(Math.cos(Math.toRadians(lastAngle.toDouble())).toFloat(),
                    Math.sin(Math.toRadians(lastAngle.toDouble())).toFloat())
            normalizeVector(sliceVector)

            val x1 = sliceVector.x * (circleRadius + touchAdditional) + circleOval.centerX()
            val y1 = sliceVector.y * (circleRadius + touchAdditional) + circleOval.centerY()

            canvas.drawLine(circleOval.centerX(), circleOval.centerY(), x1, y1, separationLinesPaint)

            lastAngle += angle
        }
    }

    fun drawLabels(canvas: Canvas) {
        val data = dataProvider.pieChartData
        val sliceScale = 360f / maxSum
        var lastAngle = chartRotation.toFloat()
        var sliceIndex = 0
        for (sliceValue in data.values) {
            val angle = Math.abs(sliceValue.value) * sliceScale
            if (isTouched) {
                if (hasLabels) {
                    drawLabel(canvas, sliceValue, lastAngle, angle)
                } else if (hasLabelsOnlyForSelected && selectedValue.firstIndex == sliceIndex) {
                    drawLabel(canvas, sliceValue, lastAngle, angle)
                }
            } else {
                if (hasLabels) {
                    drawLabel(canvas, sliceValue, lastAngle, angle)
                }
            }
            lastAngle += angle
            ++sliceIndex
        }
    }

    /**
     * Method draws single slice from lastAngle to lastAngle+angle, if mode = [.MODE_HIGHLIGHT] slice will be
     * darken
     * and will have bigger radius.
     */
    private fun drawSlice(canvas: Canvas, sliceValue: SliceValue, lastAngle: Float, angle: Float, mode: Int) {
        sliceVector.set(Math.cos(Math.toRadians((lastAngle + angle / 2).toDouble())).toFloat(),
                Math.sin(Math.toRadians((lastAngle + angle / 2).toDouble())).toFloat())
        normalizeVector(sliceVector)
        drawCircleOval.set(circleOval)
        if (MODE_HIGHLIGHT == mode) {
            // Add additional touch feedback by setting bigger radius for that slice and darken color.
            drawCircleOval.inset((-touchAdditional).toFloat(), (-touchAdditional).toFloat())
            slicePaint.color = sliceValue.darkenColor
            canvas.drawArc(drawCircleOval, lastAngle, angle, true, slicePaint)
        } else {
            slicePaint.color = sliceValue.color
            canvas.drawArc(drawCircleOval, lastAngle, angle, true, slicePaint)
        }
    }

    private fun drawLabel(canvas: Canvas, sliceValue: SliceValue, lastAngle: Float, angle: Float) {
        sliceVector.set(Math.cos(Math.toRadians((lastAngle + angle / 2).toDouble())).toFloat(),
                Math.sin(Math.toRadians((lastAngle + angle / 2).toDouble())).toFloat())
        normalizeVector(sliceVector)

        val numChars = valueFormatter!!.formatChartValue(labelBuffer, sliceValue)

        if (numChars == 0) {
            // No need to draw empty label
            return
        }

        val labelWidth = labelPaint.measureText(labelBuffer, labelBuffer.size - numChars, numChars)
        val labelHeight = Math.abs(fontMetrics.ascent)

        val centerX = circleOval.centerX()
        val centerY = circleOval.centerY()
        val circleRadius = circleOval.width() / 2f
        val labelRadius: Float

        if (hasLabelsOutside) {
            labelRadius = circleRadius * DEFAULT_LABEL_OUTSIDE_RADIUS_FACTOR
        } else {
            if (hasCenterCircle) {
                labelRadius = circleRadius - (circleRadius - circleRadius * centerCircleScale) / 2
            } else {
                labelRadius = circleRadius * DEFAULT_LABEL_INSIDE_RADIUS_FACTOR
            }
        }

        val rawX = labelRadius * sliceVector.x + centerX
        val rawY = labelRadius * sliceVector.y + centerY

        val left: Float
        val right: Float
        val top: Float
        val bottom: Float

        if (hasLabelsOutside) {
            if (rawX > centerX) {
                // Right half.
                left = rawX + labelMargin
                right = rawX + labelWidth + (labelMargin * 3).toFloat()
            } else {
                left = rawX - labelWidth - (labelMargin * 3).toFloat()
                right = rawX - labelMargin
            }

            if (rawY > centerY) {
                // Lower half.
                top = rawY + labelMargin
                bottom = rawY + labelHeight.toFloat() + (labelMargin * 3).toFloat()
            } else {
                top = rawY - labelHeight.toFloat() - (labelMargin * 3).toFloat()
                bottom = rawY - labelMargin
            }
        } else {
            left = rawX - labelWidth / 2 - labelMargin.toFloat()
            right = rawX + labelWidth / 2 + labelMargin.toFloat()
            top = rawY - (labelHeight / 2).toFloat() - labelMargin.toFloat()
            bottom = rawY + (labelHeight / 2).toFloat() + labelMargin.toFloat()
        }

        labelBackgroundRect.set(left, top, right, bottom)
        drawLabelTextAndBackground(canvas, labelBuffer, labelBuffer.size - numChars, numChars,
                sliceValue.darkenColor)
    }

    private fun normalizeVector(point: PointF) {
        val abs = point.length()
        point.set(point.x / abs, point.y / abs)
    }

    /**
     * Calculates angle of touched point.
     */
    private fun pointToAngle(x: Float, y: Float, centerX: Float, centerY: Float): Float {
        val diffX = (x - centerX).toDouble()
        val diffY = (y - centerY).toDouble()
        // Pass -diffX to get clockwise degrees order.
        val radian = Math.atan2(-diffX, diffY)

        var angle = (Math.toDegrees(radian).toFloat() + 360) % 360
        // Add 90 because atan2 returns 0 degrees at 6 o'clock.
        angle += 90f
        return angle
    }

    /**
     * Calculates rectangle(square) that will constraint chart circle.
     */
    private fun calculateCircleOval() {
        val contentRect = computator.contentRectMinusAllMargins
        val circleRadius = Math.min(contentRect.width() / 2f, contentRect.height() / 2f)
        val centerX = contentRect.centerX().toFloat()
        val centerY = contentRect.centerY().toFloat()
        val left = centerX - circleRadius + touchAdditional
        val top = centerY - circleRadius + touchAdditional
        val right = centerX + circleRadius - touchAdditional
        val bottom = centerY + circleRadius - touchAdditional
        circleOval.set(left, top, right, bottom)
        val inest = 0.5f * circleOval.width() * (1.0f - this.circleFillRatio)
        circleOval.inset(inest, inest)
    }

    /**
     * Viewport is not really important for PieChart, this kind of chart doesn't relay on viewport but uses pixels
     * coordinates instead. This method also calculates sum of all SliceValues.
     */
    private fun calculateMaxViewport() {
        tempMaximumViewport.set(0f, MAX_WIDTH_HEIGHT, MAX_WIDTH_HEIGHT, 0f)
        maxSum = 0.0f
        for (sliceValue in dataProvider.pieChartData.values) {
            maxSum += Math.abs(sliceValue.value)
        }
    }

    /**
     * Returns SliceValue that is under given angle, selectedValue (if not null) will be hold slice index.
     */
    fun getValueForAngle(angle: Int, selectedValue: SelectedValue?): SliceValue? {
        val data = dataProvider.pieChartData
        val touchAngle = (angle - chartRotation + 360f) % 360f
        val sliceScale = 360f / maxSum
        var lastAngle = 0f
        var sliceIndex = 0
        for (sliceValue in data.values) {
            val tempAngle = Math.abs(sliceValue.value) * sliceScale
            if (touchAngle >= lastAngle) {
                if (null != selectedValue) {
                    selectedValue[sliceIndex, sliceIndex] = SelectedValueType.NONE
                }
                return sliceValue
            }
            lastAngle += tempAngle
            ++sliceIndex
        }
        return null
    }

    companion object {
        private val MAX_WIDTH_HEIGHT = 100f
        private val DEFAULT_START_ROTATION = 45
        private val DEFAULT_LABEL_INSIDE_RADIUS_FACTOR = 0.7f
        private val DEFAULT_LABEL_OUTSIDE_RADIUS_FACTOR = 1.0f
        private val DEFAULT_TOUCH_ADDITIONAL_DP = 8
        private val MODE_DRAW = 0
        private val MODE_HIGHLIGHT = 1
    }

}
