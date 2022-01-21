package co.csadev.kellocharts.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.FontMetricsInt
import android.graphics.Rect
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.AxisValue
import co.csadev.kellocharts.util.AxisAutoValues
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.util.ChartUtils.dp2px
import co.csadev.kellocharts.util.FloatUtils
import co.csadev.kellocharts.util.nullIfEmpty
import co.csadev.kellocharts.view.Chart
import kotlin.math.*

/**
 * Default axes renderer. Can draw maximum four axes - two horizontal(top/bottom) and two vertical(left/right).
 */
class AxesRenderer(context: Context, private val chart: Chart) {
    private val computator = chart.chartComputator
    private val axisMargin: Int
    private val density = context.resources.displayMetrics.density
    private val scaledDensity = context.resources.displayMetrics.scaledDensity
    private val labelPaintTab = arrayOf(Paint(), Paint(), Paint(), Paint())
    private val namePaintTab = arrayOf(Paint(), Paint(), Paint(), Paint())
    private val linePaintTab = arrayOf(Paint(), Paint(), Paint(), Paint())
    private val nameBaselineTab = FloatArray(4)
    private val labelBaselineTab = FloatArray(4)
    private val separationLineTab = FloatArray(4)
    private val labelWidthTab = IntArray(4)
    private val labelTextAscentTab = IntArray(4)
    private val labelTextDescentTab = IntArray(4)
    private val labelDimensionForMarginsTab = IntArray(4)
    private val labelDimensionForStepsTab = IntArray(4)
    private val tiltedLabelXTranslation = IntArray(4)
    private val tiltedLabelYTranslation = IntArray(4)
    private val fontMetricsTab =
        arrayOf(FontMetricsInt(), FontMetricsInt(), FontMetricsInt(), FontMetricsInt())

    /**
     * Holds formatted axis value label.
     */
    private val labelBuffer = CharArray(64)

    /**
     * Holds number of values that should be drown for each axis.
     */
    private val valuesToDrawNumTab = IntArray(4)

    /**
     * Holds raw values to draw for each axis.
     */
    private val rawValuesTab = Array(4) { FloatArray(0) }

    /**
     * Holds auto-generated values that should be drawn, i.e if axis is inside not all auto-generated values should be
     * drawn to avoid overdrawing. Used only for auto axes.
     */
    private val autoValuesToDrawTab = Array(4) { FloatArray(0) }

    /**
     * Holds custom values that should be drawn, used only for custom axes.
     */
    private val valuesToDrawTab = Array<Array<AxisValue?>>(4) { arrayOfNulls(0) }

    /**
     * Buffers for axes lines coordinates(to draw grid in the background).
     */
    private val linesDrawBufferTab = Array(4) { FloatArray(0) }

    /**
     * Buffers for auto-generated values for each axis, used only if there are auto axes.
     */
    private val autoValuesBufferTab =
        arrayOf(AxisAutoValues(), AxisAutoValues(), AxisAutoValues(), AxisAutoValues())

    init {
        axisMargin = DEFAULT_AXIS_MARGIN_DP.dp2px(density)
        for (position in 0..3) {
            labelPaintTab[position].style = Paint.Style.FILL
            labelPaintTab[position].isAntiAlias = true
            namePaintTab[position].style = Paint.Style.FILL
            namePaintTab[position].isAntiAlias = true
            linePaintTab[position].style = Paint.Style.STROKE
            linePaintTab[position].isAntiAlias = true
        }
    }

    fun onChartSizeChanged() {
        onChartDataOrSizeChanged()
    }

    fun onChartDataChanged() {
        onChartDataOrSizeChanged()
    }

    private fun onChartDataOrSizeChanged() {
        initAxis(chart.chartData.axisXTop, TOP)
        initAxis(chart.chartData.axisXBottom, BOTTOM)
        initAxis(chart.chartData.axisYLeft, LEFT)
        initAxis(chart.chartData.axisYRight, RIGHT)
    }

    /**
     * Initialize attributes and measurement for axes(left, right, top, bottom);
     */
    private fun initAxis(axis: Axis?, position: Int) {
        if (null == axis) {
            return
        }
        initAxisAttributes(axis, position)
        initAxisMargin(axis, position)
        initAxisMeasurements(axis, position)
    }

    private fun initAxisAttributes(axis: Axis, position: Int) {
        initAxisPaints(axis, position)
        initAxisTextAlignment(axis, position)
        if (axis.hasTiltedLabels) {
            initAxisDimensionForTiltedLabels(position)
            intiTiltedLabelsTranslation(axis, position)
        } else {
            initAxisDimension(position)
        }
    }

    private fun initAxisPaints(axis: Axis, position: Int) {
        val typeface = axis.typeface
        if (null != typeface) {
            labelPaintTab[position].typeface = typeface
            namePaintTab[position].typeface = typeface
        }
        labelPaintTab[position].color = axis.textColor
        labelPaintTab[position].textSize = ChartUtils.sp2px(scaledDensity, axis.textSize).toFloat()
        labelPaintTab[position].getFontMetricsInt(fontMetricsTab[position])
        namePaintTab[position].color = axis.textColor
        namePaintTab[position].textSize = ChartUtils.sp2px(scaledDensity, axis.textSize).toFloat()
        linePaintTab[position].color = axis.lineColor

        labelTextAscentTab[position] = abs(fontMetricsTab[position].ascent)
        labelTextDescentTab[position] = abs(fontMetricsTab[position].descent)
        labelWidthTab[position] = labelPaintTab[position].measureText(
            labelWidthChars, 0,
            axis.maxLabelChars
        ).toInt()
    }

    private fun initAxisTextAlignment(axis: Axis, position: Int) {
        namePaintTab[position].textAlign = Align.CENTER
        if (TOP == position || BOTTOM == position) {
            labelPaintTab[position].textAlign = Align.CENTER
        } else if (LEFT == position) {
            if (axis.isInside) {
                labelPaintTab[position].textAlign = Align.LEFT
            } else {
                labelPaintTab[position].textAlign = Align.RIGHT
            }
        } else if (RIGHT == position) {
            if (axis.isInside) {
                labelPaintTab[position].textAlign = Align.RIGHT
            } else {
                labelPaintTab[position].textAlign = Align.LEFT
            }
        }
    }

    private fun initAxisDimensionForTiltedLabels(position: Int) {
        val pythagoreanFromLabelWidth =
            sqrt(labelWidthTab[position].toDouble().pow(2.0) / 2).toInt()
        val pythagoreanFromAscent =
            sqrt(labelTextAscentTab[position].toDouble().pow(2.0) / 2).toInt()
        labelDimensionForMarginsTab[position] = pythagoreanFromAscent + pythagoreanFromLabelWidth
        labelDimensionForStepsTab[position] =
            (labelDimensionForMarginsTab[position] * 0.75f).roundToInt()
    }

    private fun initAxisDimension(position: Int) {
        if (LEFT == position || RIGHT == position) {
            labelDimensionForMarginsTab[position] = labelWidthTab[position]
            labelDimensionForStepsTab[position] = labelTextAscentTab[position]
        } else if (TOP == position || BOTTOM == position) {
            labelDimensionForMarginsTab[position] =
                labelTextAscentTab[position] + labelTextDescentTab[position]
            labelDimensionForStepsTab[position] = labelWidthTab[position]
        }
    }

    private fun intiTiltedLabelsTranslation(axis: Axis, position: Int) {
        val pythagoreanFromLabelWidth =
            sqrt(labelWidthTab[position].toDouble().pow(2.0) / 2).toInt()
        val pythagoreanFromAscent =
            sqrt(labelTextAscentTab[position].toDouble().pow(2.0) / 2).toInt()
        var dx = 0
        var dy = 0
        if (axis.isInside) {
            when (position) {
                LEFT -> dx = pythagoreanFromAscent
                RIGHT -> dy = -pythagoreanFromLabelWidth / 2
                TOP ->
                    dy =
                        pythagoreanFromAscent + pythagoreanFromLabelWidth / 2 - labelTextAscentTab[position]
                BOTTOM -> dy = -pythagoreanFromLabelWidth / 2
            }
        } else {
            when (position) {
                LEFT -> dy = -pythagoreanFromLabelWidth / 2
                RIGHT -> dx = pythagoreanFromAscent
                TOP -> dy = -pythagoreanFromLabelWidth / 2
                BOTTOM ->
                    dy =
                        pythagoreanFromAscent + pythagoreanFromLabelWidth / 2 - labelTextAscentTab[position]
            }
        }
        tiltedLabelXTranslation[position] = dx
        tiltedLabelYTranslation[position] = dy
    }

    private fun initAxisMargin(axis: Axis, position: Int) {
        var margin = 0
        if (!axis.isInside && (axis.isAutoGenerated || axis.values.isNotEmpty())) {
            margin += axisMargin + labelDimensionForMarginsTab[position]
        }
        margin += getAxisNameMargin(axis, position)
        insetContentRectWithAxesMargins(margin, position)
    }

    private fun getAxisNameMargin(axis: Axis, position: Int): Int {
        var margin = 0
        if (!axis.name.isNullOrEmpty()) {
            margin += labelTextAscentTab[position]
            margin += labelTextDescentTab[position]
            margin += axisMargin
        }
        return margin
    }

    private fun insetContentRectWithAxesMargins(axisMargin: Int, position: Int) {
        when (position) {
            LEFT -> chart.chartComputator.insetContentRect(axisMargin, 0, 0, 0)
            RIGHT -> chart.chartComputator.insetContentRect(0, 0, axisMargin, 0)
            TOP -> chart.chartComputator.insetContentRect(0, axisMargin, 0, 0)
            BOTTOM -> chart.chartComputator.insetContentRect(0, 0, 0, axisMargin)
        }
    }

    private fun initAxisMeasurements(axis: Axis, position: Int) {
        when (position) {
            LEFT -> initAxis(axis, position, computator.contentRectMinusAllMargins.left)
            RIGHT -> initAxis(axis, position, computator.contentRectMinusAllMargins.right)
            BOTTOM -> initAxis(axis, position, computator.contentRectMinusAllMargins.bottom)
            TOP -> initAxis(axis, position, computator.contentRectMinusAllMargins.top)
            else -> throw IllegalArgumentException("Invalid axis position: $position")
        }
    }

    private fun initAxis(axis: Axis, position: Int, marginPos: Int) {
        if (axis.isInside) {
            labelBaselineTab[position] =
                (marginPos + axisMargin + labelTextAscentTab[position]).toFloat()
            nameBaselineTab[position] =
                (marginPos - axisMargin - labelTextDescentTab[position]).toFloat()
        } else {
            labelBaselineTab[position] =
                (marginPos - axisMargin - labelTextDescentTab[position]).toFloat()
            nameBaselineTab[position] =
                labelBaselineTab[position] - axisMargin.toFloat() - labelDimensionForMarginsTab[position].toFloat()
        }
        separationLineTab[position] = marginPos.toFloat()
    }

    /**
     * Prepare axes coordinates and draw axes lines(if enabled) in the background.
     *
     * @param canvas
     */
    fun drawInBackground(canvas: Canvas) {
        var axis = chart.chartData.axisYLeft
        if (null != axis) {
            prepareAxisToDraw(axis, LEFT)
            drawAxisLines(canvas, axis, LEFT)
        }

        axis = chart.chartData.axisYRight
        if (null != axis) {
            prepareAxisToDraw(axis, RIGHT)
            drawAxisLines(canvas, axis, RIGHT)
        }

        axis = chart.chartData.axisXBottom
        if (null != axis) {
            prepareAxisToDraw(axis, BOTTOM)
            drawAxisLines(canvas, axis, BOTTOM)
        }

        axis = chart.chartData.axisXTop
        if (null != axis) {
            prepareAxisToDraw(axis, TOP)
            drawAxisLines(canvas, axis, TOP)
        }
    }

    private fun prepareAxisToDraw(axis: Axis, position: Int) {
        if (axis.isAutoGenerated) {
            prepareAutoGeneratedAxis(axis, position)
        } else {
            prepareCustomAxis(axis, position)
        }
    }

    /**
     * Draw axes labels and names in the foreground.
     *
     * @param canvas
     */
    fun drawInForeground(canvas: Canvas) {
        var axis = chart.chartData.axisYLeft
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, LEFT)
        }

        axis = chart.chartData.axisYRight
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, RIGHT)
        }

        axis = chart.chartData.axisXBottom
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, BOTTOM)
        }

        axis = chart.chartData.axisXTop
        if (null != axis) {
            drawAxisLabelsAndName(canvas, axis, TOP)
        }
    }

    private fun prepareCustomAxis(axis: Axis, position: Int) {
        val maxViewport = computator.maximumViewport
        val visibleViewport = computator.visibleViewport
        val contentRect = computator.contentRectMinusAllMargins
        val isAxisVertical = isAxisVertical(position)
        val viewportMin: Float
        val viewportMax: Float
        var scale = 1f
        if (isAxisVertical) {
            if (maxViewport.height() > 0 && visibleViewport.height() > 0) {
                scale = contentRect.height() * (maxViewport.height() / visibleViewport.height())
            }
            viewportMin = visibleViewport.bottom
            viewportMax = visibleViewport.top
        } else {
            if (maxViewport.width() > 0 && visibleViewport.width() > 0) {
                scale = contentRect.width() * (maxViewport.width() / visibleViewport.width())
            }
            viewportMin = visibleViewport.left
            viewportMax = visibleViewport.right
        }
        if (scale == 0f) {
            scale = 1f
        }
        val module =
            1.0.coerceAtLeast(ceil(axis.values.size.toDouble() * labelDimensionForStepsTab[position].toDouble() * 1.5 / scale))
                .toInt()
        // Reinitialize tab to hold lines coordinates.
        if (axis.hasLines && linesDrawBufferTab[position].size < axis.values.size * 4) {
            linesDrawBufferTab[position] = FloatArray(axis.values.size * 4)
        }
        // Reinitialize tabs to hold all raw values to draw.
        if (rawValuesTab[position].size < axis.values.size) {
            rawValuesTab[position] = FloatArray(axis.values.size)
        }
        // Reinitialize tabs to hold all raw values to draw.
        if (valuesToDrawTab[position].size < axis.values.size) {
            valuesToDrawTab[position] = arrayOfNulls(axis.values.size)
        }

        var rawValue: Float
        var valueIndex = 0
        var valueToDrawIndex = 0
        for (axisValue in axis.values) {
            // Draw axis values that are within visible viewport.
            val value = axisValue.value
            if (value in viewportMin..viewportMax) {
                // Draw axis values that have 0 module value, this will hide some labels if there is no place for them.
                if (0 == valueIndex % module) {
                    rawValue = if (isAxisVertical) {
                        computator.computeRawY(value)
                    } else {
                        computator.computeRawX(value)
                    }
                    if (checkRawValue(
                            contentRect,
                            rawValue,
                            axis.isInside,
                            position,
                            isAxisVertical
                        )
                    ) {
                        rawValuesTab[position][valueToDrawIndex] = rawValue
                        valuesToDrawTab[position][valueToDrawIndex] = axisValue
                        ++valueToDrawIndex
                    }
                }
                // If within viewport - increment valueIndex;
                ++valueIndex
            }
        }
        valuesToDrawNumTab[position] = valueToDrawIndex
    }

    private fun prepareAutoGeneratedAxis(axis: Axis, position: Int) {
        val visibleViewport = computator.visibleViewport
        val contentRect = computator.contentRectMinusAllMargins
        val isAxisVertical = isAxisVertical(position)
        val maxLabels = axis.maxLabels
        val start: Float
        val stop: Float
        val contentRectDimension: Int
        if (isAxisVertical) {
            start = visibleViewport.bottom
            stop = visibleViewport.top
            contentRectDimension = contentRect.height()
        } else {
            start = visibleViewport.left
            stop = visibleViewport.right
            contentRectDimension = contentRect.width()
        }
        FloatUtils.computeAutoGeneratedAxisValues(
            start, stop,
            if (maxLabels > 0)
                maxLabels
            else
                abs(contentRectDimension) /
                    labelDimensionForStepsTab[position] / 2,
            autoValuesBufferTab[position]
        )
        // Reinitialize tab to hold lines coordinates.
        if (axis.hasLines && linesDrawBufferTab[position].size < autoValuesBufferTab[position].valuesNumber * 4) {
            linesDrawBufferTab[position] =
                FloatArray(autoValuesBufferTab[position].valuesNumber * 4)
        }
        // Reinitialize tabs to hold all raw and auto values.
        if (rawValuesTab[position].size < autoValuesBufferTab[position].valuesNumber) {
            rawValuesTab[position] = FloatArray(autoValuesBufferTab[position].valuesNumber)
        }
        if (autoValuesToDrawTab[position].size < autoValuesBufferTab[position].valuesNumber) {
            autoValuesToDrawTab[position] = FloatArray(autoValuesBufferTab[position].valuesNumber)
        }

        var rawValue: Float
        var valueToDrawIndex = 0
        for (i in 0 until autoValuesBufferTab[position].valuesNumber) {
            rawValue = if (isAxisVertical) {
                computator.computeRawY(autoValuesBufferTab[position].values[i])
            } else {
                computator.computeRawX(autoValuesBufferTab[position].values[i])
            }
            if (checkRawValue(contentRect, rawValue, axis.isInside, position, isAxisVertical)) {
                rawValuesTab[position][valueToDrawIndex] = rawValue
                autoValuesToDrawTab[position][valueToDrawIndex] =
                    autoValuesBufferTab[position].values[i]
                ++valueToDrawIndex
            }
        }
        valuesToDrawNumTab[position] = valueToDrawIndex
    }

    private fun checkRawValue(
        rect: Rect,
        rawValue: Float,
        axisInside: Boolean,
        position: Int,
        isVertical: Boolean
    ): Boolean {
        if (axisInside) {
            return if (isVertical) {
                val marginBottom = (labelTextAscentTab[BOTTOM] + axisMargin).toFloat()
                val marginTop = (labelTextAscentTab[TOP] + axisMargin).toFloat()
                rawValue <= rect.bottom - marginBottom && rawValue >= rect.top + marginTop
            } else {
                val margin = (labelWidthTab[position] / 2).toFloat()
                rawValue >= rect.left + margin && rawValue <= rect.right - margin
            }
        }
        return true
    }

    private fun drawAxisLines(canvas: Canvas, axis: Axis, position: Int) {
        val contentRectMargins = computator.contentRectMinusAxesMargins
        var separationX1 = 0f
        var separationY1 = 0f
        var separationX2 = 0f
        var separationY2 = 0f
        var lineX1 = 0f
        var lineY1 = 0f
        var lineX2 = 0f
        var lineY2 = 0f
        val isAxisVertical = isAxisVertical(position)
        if (LEFT == position || RIGHT == position) {
            separationX2 = separationLineTab[position]
            separationX1 = separationX2
            separationY1 = contentRectMargins.bottom.toFloat()
            separationY2 = contentRectMargins.top.toFloat()
            lineX1 = contentRectMargins.left.toFloat()
            lineX2 = contentRectMargins.right.toFloat()
        } else if (TOP == position || BOTTOM == position) {
            separationX1 = contentRectMargins.left.toFloat()
            separationX2 = contentRectMargins.right.toFloat()
            separationY2 = separationLineTab[position]
            separationY1 = separationY2
            lineY1 = contentRectMargins.top.toFloat()
            lineY2 = contentRectMargins.bottom.toFloat()
        }
        // Draw separation line with the same color as axis labels and name.
        if (axis.hasSeparationLine) {
            canvas.drawLine(
                separationX1,
                separationY1,
                separationX2,
                separationY2,
                labelPaintTab[position]
            )
        }

        if (axis.hasLines) {
            var valueToDrawIndex = 0
            while (valueToDrawIndex < valuesToDrawNumTab[position]) {
                if (isAxisVertical) {
                    lineY2 = rawValuesTab[position][valueToDrawIndex]
                    lineY1 = lineY2
                } else {
                    lineX2 = rawValuesTab[position][valueToDrawIndex]
                    lineX1 = lineX2
                }
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 0] = lineX1
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 1] = lineY1
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 2] = lineX2
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 3] = lineY2
                ++valueToDrawIndex
            }
            canvas.drawLines(
                linesDrawBufferTab[position],
                0,
                valueToDrawIndex * 4,
                linePaintTab[position]
            )
        }
    }

    private fun drawAxisLabelsAndName(canvas: Canvas, axis: Axis, position: Int) {
        var labelX = 0f
        var labelY = 0f
        val isAxisVertical = isAxisVertical(position)
        val isAxisReversed = axis.isReversed
        if (LEFT == position || RIGHT == position) {
            labelX = labelBaselineTab[position]
        } else if (TOP == position || BOTTOM == position) {
            labelY = labelBaselineTab[position]
        }
        var reverseIndex = if (isAxisReversed) 0 else valuesToDrawNumTab[position]
        var valueToDrawIndex = if (isAxisReversed) valuesToDrawNumTab[position] - 1 else 0
        while (if (isAxisReversed) valueToDrawIndex >= 0 else valueToDrawIndex < valuesToDrawNumTab[position]) {
            var charsNumber = 0
            if (axis.isAutoGenerated) {
                val value = autoValuesToDrawTab[position][valueToDrawIndex]
                charsNumber = axis.formatter.formatValueForAutoGeneratedAxis(
                    labelBuffer, value,
                    autoValuesBufferTab[position].decimals
                )
            } else {
                val axisValue = valuesToDrawTab[position][valueToDrawIndex]
                if (axisValue != null)
                    charsNumber = axis.formatter.formatValueForManualAxis(labelBuffer, axisValue)
            }

            if (isAxisVertical) {
                labelY =
                    rawValuesTab[position][if (isAxisReversed) reverseIndex else valueToDrawIndex]
            } else {
                labelX =
                    rawValuesTab[position][if (isAxisReversed) reverseIndex else valueToDrawIndex]
            }

            if (axis.hasTiltedLabels) {
                canvas.save()
                canvas.translate(
                    tiltedLabelXTranslation[position].toFloat(),
                    tiltedLabelYTranslation[position].toFloat()
                )
                canvas.rotate(-45f, labelX, labelY)
                canvas.drawText(
                    labelBuffer, labelBuffer.size - charsNumber, charsNumber, labelX, labelY,
                    labelPaintTab[position]
                )
                canvas.restore()
            } else {
                canvas.drawText(
                    labelBuffer, labelBuffer.size - charsNumber, charsNumber, labelX, labelY,
                    labelPaintTab[position]
                )
            }
            if (isAxisReversed) {
                --valueToDrawIndex
                ++reverseIndex
            } else {
                ++valueToDrawIndex
                --reverseIndex
            }
        }

        // Drawing axis name
        val contentRectMargins = computator.contentRectMinusAxesMargins
        axis.name?.nullIfEmpty()?.let { axisName ->
            if (isAxisVertical) {
                canvas.save()
                canvas.rotate(
                    -90f,
                    contentRectMargins.centerY().toFloat(),
                    contentRectMargins.centerY().toFloat()
                )
                canvas.drawText(
                    axisName, contentRectMargins.centerY().toFloat(), nameBaselineTab[position],
                    namePaintTab[position]
                )
                canvas.restore()
            } else {
                canvas.drawText(
                    axisName, contentRectMargins.centerX().toFloat(), nameBaselineTab[position],
                    namePaintTab[position]
                )
            }
        }
    }

    private fun isAxisVertical(position: Int) = when (position) {
        LEFT, RIGHT -> true
        TOP, BOTTOM -> false
        else -> throw IllegalArgumentException("Invalid axis position $position")
    }

    companion object {
        private const val DEFAULT_AXIS_MARGIN_DP = 2

        /**
         * Axis positions indexes, used for indexing tabs that holds axes parameters, see below.
         */
        private const val TOP = 0
        private const val LEFT = 1
        private const val RIGHT = 2
        private const val BOTTOM = 3

        /**
         * Used to measure label width. If label has mas 5 characters only 5 first characters of this array are used to
         * measure text width.
         */
        private val labelWidthChars = (0 until 64).map { '0' }.toCharArray()
    }
}
