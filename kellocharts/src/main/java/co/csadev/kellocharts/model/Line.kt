package co.csadev.kellocharts.model

import android.graphics.PathEffect
import co.csadev.kellocharts.formatter.LineChartValueFormatter
import co.csadev.kellocharts.formatter.SimpleLineChartValueFormatter
import co.csadev.kellocharts.util.ChartUtils
import java.util.*

/**
 * Single line for line chart.
 */
class Line(var values: MutableList<PointValue> = ArrayList(),
           color: Int = ChartUtils.DEFAULT_COLOR,
           pointColor: Int = color,
           darkenColor: Int = ChartUtils.darkenColor(color),
           var formatter: LineChartValueFormatter = SimpleLineChartValueFormatter(),
           var shape: ValueShape = ValueShape.CIRCLE,
           var isFilled: Boolean = false,
           isSquare: Boolean = false,
           isCubic: Boolean = false,
           var pointRadius: Int = DEFAULT_POINT_RADIUS_DP,
           var areaTransparency: Int = DEFAULT_AREA_TRANSPARENCY,
           var strokeWidth: Int = DEFAULT_LINE_STROKE_WIDTH_DP,
           var hasPoints: Boolean = true,
           var hasLines: Boolean = true,
           hasLabels: Boolean = true,
           hasLabelsOnlyForSelected: Boolean = true,
           var pathEffect: PathEffect? = null) {
    var color = color
        set(value) {
            field = value
            if (pointColor == UNINITIALIZED) darkenColor = ChartUtils.darkenColor(field)
        }

    var pointColor = pointColor
        get() = if (field == UNINITIALIZED) color else field
        set(value) {
            field = value
            darkenColor = ChartUtils.darkenColor(if (field == UNINITIALIZED) color else field)
        }

    var darkenColor = darkenColor
        private set

    var isSquare = isSquare
        set(value) {
            field = value
            if (field) isCubic = false
        }
    var isCubic = isCubic
        set(value) {
            field = value
            if (field) isSquare = false
        }

    var hasLabels = hasLabels
        set(value) {
            field = value
            if (field) hasLabelsOnlyForSelected = false
        }
    var hasLabelsOnlyForSelected = hasLabelsOnlyForSelected
        set(value) {
            field = value
            if (field) hasLabels = false
        }

    fun copy() = Line(values.map { it.copy() }.toMutableList(), color, pointColor, darkenColor, formatter, shape, isFilled, isSquare, isCubic, pointRadius, areaTransparency, strokeWidth, hasPoints, hasLines, hasLabels, hasLabelsOnlyForSelected, pathEffect)

    fun update(scale: Float) = values.forEach { it.update(scale) }

    fun finish() = values.forEach { it.finish() }

    companion object {
        internal val DEFAULT_LINE_STROKE_WIDTH_DP = 3
        internal val DEFAULT_POINT_RADIUS_DP = 6
        internal val DEFAULT_AREA_TRANSPARENCY = 64
        val UNINITIALIZED = 0
    }
}
