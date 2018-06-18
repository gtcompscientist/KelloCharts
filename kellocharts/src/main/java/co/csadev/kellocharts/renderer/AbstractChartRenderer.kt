package co.csadev.kellocharts.renderer

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.graphics.Paint.FontMetricsInt
import co.csadev.kellocharts.computator.ChartComputator
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart

/**
 * Abstract renderer implementation, every chart renderer extends this class(although it is not required it helps).
 */
abstract class AbstractChartRenderer(context: Context, protected var chart: Chart) : ChartRenderer {
    var DEFAULT_LABEL_MARGIN_DP = 4
    protected var computator: ChartComputator
    /**
     * Paint for value labels.
     */
    protected var labelPaint = Paint()
    /**
     * Paint for labels background.
     */
    protected var labelBackgroundPaint = Paint()
    /**
     * Holds coordinates for label background rect.
     */
    protected var labelBackgroundRect = RectF()
    /**
     * Font metrics for label paint, used to determine text height.
     */
    protected var fontMetrics = FontMetricsInt()
    /**
     * If true maximum and current viewport will be calculated when chart data change or during data animations.
     */
    override var isViewportCalculationEnabled = true
    protected var density: Float = 0.toFloat()
    protected var scaledDensity: Float = 0.toFloat()
    override var selectedValue = SelectedValue()
    protected var labelBuffer = CharArray(64)
    protected var labelOffset: Int = 0
    protected var labelMargin: Int = 0
    protected var isValueLabelBackgroundEnabled: Boolean = false
    protected var isValueLabelBackgroundAuto: Boolean = false
    override val isTouched: Boolean
        get() = selectedValue.isSet
    override var currentViewport: Viewport
        get() = computator.currentViewport
        set(value) { computator.currentViewport = value }
    override var maximumViewport: Viewport
        get() = computator.maximumViewport
        set(value) { computator.maximumViewport = value }

    init {
        this.density = context.resources.displayMetrics.density
        this.scaledDensity = context.resources.displayMetrics.scaledDensity
        this.computator = chart.chartComputator

        labelMargin = ChartUtils.dp2px(density, DEFAULT_LABEL_MARGIN_DP)
        labelOffset = labelMargin

        labelPaint.isAntiAlias = true
        labelPaint.style = Paint.Style.FILL
        labelPaint.textAlign = Align.LEFT
        labelPaint.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        labelPaint.color = Color.WHITE

        labelBackgroundPaint.isAntiAlias = true
        labelBackgroundPaint.style = Paint.Style.FILL
    }

    override fun resetRenderer() {
        this.computator = chart.chartComputator
    }

    override fun onChartDataChanged() {
        val data = chart.chartData

        val typeface = chart.chartData.valueLabelTypeface
        if (null != typeface) {
            labelPaint.typeface = typeface
        }

        labelPaint.color = data.valueLabelTextColor
        labelPaint.textSize = ChartUtils.sp2px(scaledDensity, data.valueLabelTextSize).toFloat()
        labelPaint.getFontMetricsInt(fontMetrics)

        this.isValueLabelBackgroundEnabled = data.isValueLabelBackgroundEnabled
        this.isValueLabelBackgroundAuto = data.isValueLabelBackgroundAuto
        this.labelBackgroundPaint.color = data.valueLabelBackgroundColor

        // Important - clear selection when data changed.
        selectedValue.clear()

    }

    /**
     * Draws label text and label background if isValueLabelBackgroundEnabled is true.
     */
    protected fun drawLabelTextAndBackground(canvas: Canvas, labelBuffer: CharArray, startIndex: Int, numChars: Int,
                                             autoBackgroundColor: Int) {
        val textX: Float
        val textY: Float

        if (isValueLabelBackgroundEnabled) {

            if (isValueLabelBackgroundAuto) {
                labelBackgroundPaint.color = autoBackgroundColor
            }

            canvas.drawRect(labelBackgroundRect, labelBackgroundPaint)

            textX = labelBackgroundRect.left + labelMargin
            textY = labelBackgroundRect.bottom - labelMargin
        } else {
            textX = labelBackgroundRect.left
            textY = labelBackgroundRect.bottom
        }

        canvas.drawText(labelBuffer, startIndex, numChars, textX, textY, labelPaint)
    }

    override fun clearTouch() {
        selectedValue.clear()
    }

}

class InternalChartRendererBase(context: Context, chart: Chart) : AbstractChartRenderer(context, chart) {
    override fun onChartSizeChanged() { }
    override fun onChartViewportChanged() { }
    override fun draw(canvas: Canvas) { }
    override fun drawUnclipped(canvas: Canvas) { }
    override fun checkTouch(touchX: Float, touchY: Float): Boolean { return false }
}