package co.csadev.kellocharts.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import co.csadev.kellocharts.provider.LineChartDataProvider
import co.csadev.kellocharts.util.ChartUtils.dp2px
import co.csadev.kellocharts.view.Chart

/**
 * Renderer for preview chart based on LineChart. In addition to drawing chart data it also draw current viewport as
 * preview area.
 */
class PreviewLineChartRenderer(
    context: Context,
    chart: Chart,
    dataProvider: LineChartDataProvider
) : LineChartRenderer(context, chart, dataProvider) {

    private val previewPaint = Paint().apply {
        isAntiAlias = true
        color = Color.LTGRAY
        strokeWidth = DEFAULT_PREVIEW_STROKE_WIDTH_DP.dp2px(density).toFloat()
    }

    var previewColor: Int
        get() = previewPaint.color
        set(color) {
            previewPaint.color = color
        }

    override fun drawUnclipped(canvas: Canvas) {
        super.drawUnclipped(canvas)
        val currentViewport = computator.currentViewport
        val left = computator.computeRawX(currentViewport.left)
        val top = computator.computeRawY(currentViewport.top)
        val right = computator.computeRawX(currentViewport.right)
        val bottom = computator.computeRawY(currentViewport.bottom)
        previewPaint.alpha = DEFAULT_PREVIEW_TRANSPARENCY
        previewPaint.style = Paint.Style.FILL
        canvas.drawRect(left, top, right, bottom, previewPaint)
        previewPaint.style = Paint.Style.STROKE
        previewPaint.alpha = FULL_ALPHA
        canvas.drawRect(left, top, right, bottom, previewPaint)
    }

    companion object {
        private const val DEFAULT_PREVIEW_TRANSPARENCY = 64
        private const val FULL_ALPHA = 255
        private const val DEFAULT_PREVIEW_STROKE_WIDTH_DP = 2
    }
}
