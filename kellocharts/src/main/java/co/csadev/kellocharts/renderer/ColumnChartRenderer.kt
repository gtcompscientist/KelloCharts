package co.csadev.kellocharts.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.PointF
import android.graphics.RectF
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.model.SelectedValue.SelectedValueType
import co.csadev.kellocharts.provider.ColumnChartDataProvider
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart

/**
 * Magic renderer for ColumnChart.
 */
open class ColumnChartRenderer(context: Context, chart: Chart, private val dataProvider: ColumnChartDataProvider) : AbstractChartRenderer(context, chart) {

    /**
     * Additional width for hightlighted column, used to give tauch feedback.
     */
    private val touchAdditionalWidth: Int

    /**
     * Spacing between sub-columns.
     */
    private val subcolumnSpacing: Int

    /**
     * Paint used to draw every column.
     */
    private val columnPaint = Paint()

    /**
     * Holds coordinates for currently processed column/sub-column.
     */
    private val drawRect = RectF()

    /**
     * Coordinated of user tauch.
     */
    private val touchedPoint = PointF()

    private var fillRatio: Float = 0.toFloat()

    private var baseValue: Float = 0.toFloat()

    private val tempMaximumViewport = Viewport()

    init {
        subcolumnSpacing = ChartUtils.dp2px(density, DEFAULT_SUBCOLUMN_SPACING_DP)
        touchAdditionalWidth = ChartUtils.dp2px(density, DEFAULT_COLUMN_TOUCH_ADDITIONAL_WIDTH_DP)

        columnPaint.isAntiAlias = true
        columnPaint.style = Paint.Style.FILL
        columnPaint.strokeCap = Cap.SQUARE
    }

    override fun onChartSizeChanged() {}

    override fun onChartDataChanged() {
        super.onChartDataChanged()
        val data = dataProvider.columnChartData
        fillRatio = data.fillRatio
        baseValue = data.baseValue

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
        val data = dataProvider.columnChartData
        val isHorizontal = data.isHorizontal
        if (data.isStacked) {
            drawColumnForStacked(canvas, isHorizontal)
            if (isTouched) {
                highlightColumnForStacked(canvas, isHorizontal)
            }
        } else {
            drawColumnsForSubcolumns(canvas, isHorizontal)
            if (isTouched) {
                highlightColumnsForSubcolumns(canvas, isHorizontal)
            }
        }
    }

    override fun drawUnclipped(canvas: Canvas) {
        // Do nothing, for this kind of chart there is nothing to draw beyond clipped area
    }

    override fun checkTouch(touchX: Float, touchY: Float): Boolean {
        selectedValue.clear()
        val data = dataProvider.columnChartData
        if (data.isStacked) {
            checkTouchForStacked(touchX, touchY, data.isHorizontal)
        } else {
            checkTouchForSubcolumns(touchX, touchY, data.isHorizontal)
        }
        return isTouched
    }

    private fun calculateMaxViewport() {
        val data = dataProvider.columnChartData
        // Column chart always has X values from 0 to numColumns-1, to add some margin on the left and right I added
        // extra 0.5 to the each side, that margins will be negative scaled according to number of columns, so for more
        // columns there will be less margin.
        if (data.isHorizontal)
            tempMaximumViewport.set(baseValue, data.columns.size - 0.5f, baseValue, -0.5f)
        else
            tempMaximumViewport.set(-0.5f, baseValue, data.columns.size - 0.5f, baseValue)
        if (data.isStacked) {
            calculateMaxViewportForStacked(data)
        } else {
            calculateMaxViewportForSubcolumns(data)
        }
    }

    private fun calculateMaxViewportForSubcolumns(data: ColumnChartData) {
        var tempMax = if (data.isHorizontal) tempMaximumViewport.right else tempMaximumViewport.top
        var tempMin = if (data.isHorizontal) tempMaximumViewport.left else tempMaximumViewport.bottom
        for (column in data.columns) {
            for (columnValue in column.values) {
                if (columnValue.value >= baseValue && columnValue.value > tempMax) {
                    tempMax = columnValue.value
                }
                if (columnValue.value < baseValue && columnValue.value < tempMin) {
                    tempMin = columnValue.value
                }
            }
        }
        if (data.isHorizontal) {
            tempMaximumViewport.right = tempMax
            tempMaximumViewport.left = tempMin
        } else {
            tempMaximumViewport.top = tempMax
            tempMaximumViewport.bottom = tempMin
        }
    }

    private fun calculateMaxViewportForStacked(data: ColumnChartData) {
        var tempMax = if (data.isHorizontal) tempMaximumViewport.right else tempMaximumViewport.top
        var tempMin = if (data.isHorizontal) tempMaximumViewport.left else tempMaximumViewport.bottom
        data.columns.forEach { column ->
            var sumPositive = baseValue
            var sumNegative = baseValue
            column.values.forEach { columnValue ->
                if (columnValue.value >= baseValue) {
                    sumPositive += columnValue.value
                } else {
                    sumNegative += columnValue.value
                }
            }
            if (sumPositive > tempMax) {
                tempMax = sumPositive
            }
            if (sumNegative < tempMin) {
                tempMin = sumNegative
            }
        }
        for (column in data.columns) {
        }
        if (data.isHorizontal) {
            tempMaximumViewport.right = tempMax
            tempMaximumViewport.left = tempMin
        } else {
            tempMaximumViewport.top = tempMax
            tempMaximumViewport.bottom = tempMin
        }
    }

    private fun drawColumnsForSubcolumns(canvas: Canvas, isHorizontal: Boolean) {
        val data = dataProvider.columnChartData
        val columnWidth = calculateColumnWidth(isHorizontal)
        var columnIndex = 0
        for (column in data.columns) {
            processColumnForSubcolumns(canvas, column, columnWidth, columnIndex, MODE_DRAW, isHorizontal)
            ++columnIndex
        }
    }

    private fun highlightColumnsForSubcolumns(canvas: Canvas, isHorizontal: Boolean) {
        val data = dataProvider.columnChartData
        val columnWidth = calculateColumnWidth(isHorizontal)
        val column = data.columns[selectedValue.firstIndex]
        processColumnForSubcolumns(canvas, column, columnWidth, selectedValue.firstIndex, MODE_HIGHLIGHT, isHorizontal)
    }

    private fun checkTouchForSubcolumns(touchX: Float, touchY: Float, isHorizontal: Boolean) {
        // Using member variable to hold touch point to avoid too much parameters in methods.
        touchedPoint.x = touchX
        touchedPoint.y = touchY
        val data = dataProvider.columnChartData
        val columnWidth = calculateColumnWidth(isHorizontal)
        var columnIndex = 0
        for (column in data.columns) {
            // canvas is not needed for checking touch
            processColumnForSubcolumns(null, column, columnWidth, columnIndex, MODE_CHECK_TOUCH, isHorizontal)
            ++columnIndex
        }
    }

    private fun processColumnForSubcolumns(canvas: Canvas?, column: Column, columnWidth: Float, columnIndex: Int,
                                           mode: Int, isHorizontal: Boolean) {
        // For n subcolumns there will be n-1 spacing and there will be one
        // subcolumn for every columnValue
        var subcolumnWidth = (columnWidth - subcolumnSpacing * (column.values.size - 1)) / column.values.size
        if (subcolumnWidth < 1) {
            subcolumnWidth = 1f
        }
        // Columns are indexes from 0 to n, column index is also column X value
        val rawX = if (isHorizontal) computator.computeRawY(columnIndex.toFloat()) else computator.computeRawX(columnIndex.toFloat())
        val halfColumnWidth = columnWidth / 2
        val baseRawY = if (isHorizontal) computator.computeRawX(baseValue) else computator.computeRawY(baseValue)
        // First subcolumn will starts at the left edge of current column,
        // rawValueX is horizontal center of that column
        var subcolumnRawX = rawX - halfColumnWidth
        var valueIndex = 0
        for (columnValue in column.values) {
            columnPaint.color = columnValue.color
            if (subcolumnRawX > rawX + halfColumnWidth) {
                break
            }
            val rawY = if (isHorizontal) computator.computeRawX(columnValue.value) else computator.computeRawY(columnValue.value)
            calculateRectToDraw(isHorizontal, columnValue, subcolumnRawX, subcolumnRawX + subcolumnWidth, baseRawY, rawY)
            when (mode) {
                MODE_DRAW -> drawSubcolumn(canvas, column, columnValue, false)
                MODE_HIGHLIGHT -> highlightSubcolumn(canvas, column, columnValue, valueIndex, false)
                MODE_CHECK_TOUCH -> checkRectToDraw(columnIndex, valueIndex)
                else ->
                    // There no else, every case should be handled or exception will
                    // be thrown
                    throw IllegalStateException("Cannot process column in mode: " + mode)
            }
            subcolumnRawX += subcolumnWidth + subcolumnSpacing
            ++valueIndex
        }
    }

    private fun drawColumnForStacked(canvas: Canvas, isHorizontal: Boolean) {
        val data = dataProvider.columnChartData
        val columnWidth = calculateColumnWidth(isHorizontal)
        // Columns are indexes from 0 to n, column index is also column X value
        var columnIndex = 0
        for (column in data.columns) {
            processColumnForStacked(canvas, column, columnWidth, columnIndex, MODE_DRAW, isHorizontal)
            ++columnIndex
        }
    }

    private fun highlightColumnForStacked(canvas: Canvas, isHorizontal: Boolean) {
        val data = dataProvider.columnChartData
        val columnWidth = calculateColumnWidth(isHorizontal)
        // Columns are indexes from 0 to n, column index is also column X value
        val column = data.columns[selectedValue.firstIndex]
        processColumnForStacked(canvas, column, columnWidth, selectedValue.firstIndex, MODE_HIGHLIGHT, isHorizontal)
    }

    private fun checkTouchForStacked(touchX: Float, touchY: Float, isHorizontal: Boolean) {
        touchedPoint.x = touchX
        touchedPoint.y = touchY
        val data = dataProvider.columnChartData
        val columnWidth = calculateColumnWidth(isHorizontal)
        var columnIndex = 0
        for (column in data.columns) {
            // canvas is not needed for checking touch
            processColumnForStacked(null, column, columnWidth, columnIndex, MODE_CHECK_TOUCH, isHorizontal)
            ++columnIndex
        }
    }

    private fun processColumnForStacked(canvas: Canvas?, column: Column, columnWidth: Float, columnIndex: Int, mode: Int, isHorizontal: Boolean) {
        val rawX = if (isHorizontal) computator.computeRawY(columnIndex.toFloat()) else computator.computeRawX(columnIndex.toFloat())
        val halfColumnWidth = columnWidth / 2
        var mostPositiveValue = baseValue
        var mostNegativeValue = baseValue
        var subcolumnBaseValue = baseValue
        var valueIndex = 0
        for (columnValue in column.values) {
            columnPaint.color = columnValue.color
            if (columnValue.value >= baseValue) {
                // Using values instead of raw pixels make code easier to
                // understand(for me)
                subcolumnBaseValue = mostPositiveValue
                mostPositiveValue += columnValue.value
            } else {
                subcolumnBaseValue = mostNegativeValue
                mostNegativeValue += columnValue.value
            }
            val rawBaseY = if (isHorizontal) computator.computeRawX(subcolumnBaseValue) else computator.computeRawY(subcolumnBaseValue)
            val rawY = if (isHorizontal) computator.computeRawX(subcolumnBaseValue + columnValue.value) else computator.computeRawY(subcolumnBaseValue + columnValue.value)
            calculateRectToDraw(isHorizontal, columnValue, rawX - halfColumnWidth, rawX + halfColumnWidth, rawBaseY, rawY)
            when (mode) {
                MODE_DRAW -> drawSubcolumn(canvas, column, columnValue, true)
                MODE_HIGHLIGHT -> highlightSubcolumn(canvas, column, columnValue, valueIndex, true)
                MODE_CHECK_TOUCH -> checkRectToDraw(columnIndex, valueIndex)
                else ->
                    // There no else, every case should be handled or exception will
                    // be thrown
                    throw IllegalStateException("Cannot process column in mode: " + mode)
            }
            ++valueIndex
        }
    }

    private fun drawSubcolumn(canvas: Canvas?, column: Column, columnValue: SubcolumnValue, isStacked: Boolean) {
        canvas!!.drawRect(drawRect, columnPaint)
        if (column.hasLabels) {
            drawLabel(canvas, column, columnValue, isStacked, labelOffset.toFloat())
        }
    }

    private fun highlightSubcolumn(canvas: Canvas?, column: Column, columnValue: SubcolumnValue, valueIndex: Int,
                                   isStacked: Boolean) {
        if (selectedValue.secondIndex == valueIndex) {
            columnPaint.color = columnValue.darkenColor
            canvas!!.drawRect(drawRect.left - touchAdditionalWidth, drawRect.top, drawRect.right + touchAdditionalWidth,
                    drawRect.bottom, columnPaint)
            if (column.hasLabels || column.hasLabelsOnlyForSelected) {
                drawLabel(canvas, column, columnValue, isStacked, labelOffset.toFloat())
            }
        }
    }

    private fun checkRectToDraw(columnIndex: Int, valueIndex: Int) {
        if (drawRect.contains(touchedPoint.x, touchedPoint.y)) {
            selectedValue[columnIndex, valueIndex] = SelectedValueType.COLUMN
        }
    }

    private fun calculateColumnWidth(isHorizontal: Boolean): Float {
        // columnWidth should be at least 2 px
        val rawRect = computator.contentRectMinusAllMargins
        val rawViewport = computator.visibleViewport
        var columnWidth = fillRatio * (if (isHorizontal) rawRect.height() else rawRect.width()) / if (isHorizontal) rawViewport.height() else rawViewport.width()
        if (columnWidth < 2) {
            columnWidth = 2f
        }
        return columnWidth
    }

    private fun calculateRectToDraw(isHorizontal: Boolean, columnValue: SubcolumnValue, left: Float, right: Float, rawBaseY: Float, rawY: Float) {
        // Calculate rect that will be drawn as column, subcolumn or label background.
        if (isHorizontal) {
            if (columnValue.value >= baseValue) {
                drawRect.top = left
                drawRect.bottom = right - subcolumnSpacing
            } else {
                drawRect.top = left
                drawRect.bottom = right + subcolumnSpacing
            }
            drawRect.left = rawBaseY
            drawRect.right = rawY
            return
        }
        drawRect.left = left
        drawRect.right = right
        if (columnValue.value >= baseValue) {
            drawRect.top = rawY
            drawRect.bottom = rawBaseY - subcolumnSpacing
        } else {
            drawRect.bottom = rawY
            drawRect.top = rawBaseY + subcolumnSpacing
        }
    }

    private fun drawLabel(canvas: Canvas, column: Column, columnValue: SubcolumnValue, isStacked: Boolean, offset: Float) {
        val numChars = column.formatter.formatChartValue(labelBuffer, columnValue)

        if (numChars == 0) {
            // No need to draw empty label
            return
        }

        val labelWidth = labelPaint.measureText(labelBuffer, labelBuffer.size - numChars, numChars)
        val labelHeight = Math.abs(fontMetrics.ascent)
        val left = drawRect.centerX() - labelWidth / 2 - labelMargin.toFloat()
        val right = drawRect.centerX() + labelWidth / 2 + labelMargin.toFloat()
        var top: Float
        var bottom: Float
        if (isStacked && labelHeight < drawRect.height() - 2 * labelMargin) {
            // For stacked columns draw label only if label height is less than subcolumn height - (2 * labelMargin).
            if (columnValue.value >= baseValue) {
                top = drawRect.top
                bottom = drawRect.top + labelHeight.toFloat() + (labelMargin * 2).toFloat()
            } else {
                top = drawRect.bottom - labelHeight.toFloat() - (labelMargin * 2).toFloat()
                bottom = drawRect.bottom
            }
        } else if (!isStacked) {
            // For not stacked draw label at the top for positive and at the bottom for negative values
            if (columnValue.value >= baseValue) {
                top = drawRect.top - offset - labelHeight.toFloat() - (labelMargin * 2).toFloat()
                if (top < computator.contentRectMinusAllMargins.top) {
                    top = drawRect.top + offset
                    bottom = drawRect.top + offset + labelHeight.toFloat() + (labelMargin * 2).toFloat()
                } else {
                    bottom = drawRect.top - offset
                }
            } else {
                bottom = drawRect.bottom + offset + labelHeight.toFloat() + (labelMargin * 2).toFloat()
                if (bottom > computator.contentRectMinusAllMargins.bottom) {
                    top = drawRect.bottom - offset - labelHeight.toFloat() - (labelMargin * 2).toFloat()
                    bottom = drawRect.bottom - offset
                } else {
                    top = drawRect.bottom + offset
                }
            }
        } else {
            // Draw nothing.
            return
        }

        labelBackgroundRect.set(left, top, right, bottom)
        drawLabelTextAndBackground(canvas, labelBuffer, labelBuffer.size - numChars, numChars,
                columnValue.darkenColor)

    }

    companion object {
        val DEFAULT_SUBCOLUMN_SPACING_DP = 1
        val DEFAULT_COLUMN_TOUCH_ADDITIONAL_WIDTH_DP = 4

        private val MODE_DRAW = 0
        private val MODE_CHECK_TOUCH = 1
        private val MODE_HIGHLIGHT = 2
    }

}
