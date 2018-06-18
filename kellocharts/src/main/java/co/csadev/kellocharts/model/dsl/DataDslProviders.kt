package co.csadev.kellocharts.model.dsl

import android.graphics.Color
import android.graphics.Typeface
import co.csadev.kellocharts.formatter.BubbleChartValueFormatter
import co.csadev.kellocharts.formatter.PieChartValueFormatter
import co.csadev.kellocharts.formatter.SimpleBubbleChartValueFormatter
import co.csadev.kellocharts.formatter.SimplePieChartValueFormatter
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.model.BubbleChartData.Companion.DEFAULT_BUBBLE_SCALE
import co.csadev.kellocharts.model.BubbleChartData.Companion.DEFAULT_MIN_BUBBLE_RADIUS_DP
import co.csadev.kellocharts.model.LineChartData.Companion.DEFAULT_BASE_VALUE
import java.util.*

@DslMarker
annotation class BubbleDataDsl

fun bubbleData(block: BubbleChartDataBuilder.() -> Unit): BubbleChartData = BubbleChartDataBuilder().apply(block).build()
@BubbleDataDsl
class BubbleChartDataBuilder {
    private var values: MutableList<BubbleValue> = ArrayList()
    var formatter: BubbleChartValueFormatter = SimpleBubbleChartValueFormatter()
    var hasLabels: Boolean = false
    var hasLabelsOnlyForSelected: Boolean = false
    var minBubbleRadius: Int = DEFAULT_MIN_BUBBLE_RADIUS_DP
    var bubbleScale: Float = DEFAULT_BUBBLE_SCALE

    fun bubbles(block: BUBBLEVALUES.() -> Unit) {
        values.addAll(BUBBLEVALUES().apply(block))
    }

    fun build(): BubbleChartData = BubbleChartData(
            values,
            formatter,
            hasLabels,
            hasLabelsOnlyForSelected,
            minBubbleRadius,
            bubbleScale
    )
}

@DslMarker
annotation class ColumnDataDsl

fun columnData(block: ColumnChartDataBuilder.() -> Unit): ColumnChartData = ColumnChartDataBuilder().apply(block).build()
@ColumnDataDsl
class ColumnChartDataBuilder {
    private var columns: MutableList<Column> = ArrayList()
    var isStacked: Boolean = false
    var isHorizontal: Boolean = true

    fun columns(block: COLUMNS.() -> Unit) {
        columns.addAll(COLUMNS().apply(block))
    }

    fun build(): ColumnChartData = ColumnChartData(columns, isStacked, isHorizontal)
}

@DslMarker
annotation class LineDataDsl

fun lineData(block: LineChartDataBuilder.() -> Unit): LineChartData = LineChartDataBuilder().apply(block).build()
@LineDataDsl
class LineChartDataBuilder {
    private var lines: MutableList<Line> = ArrayList()
    var baseValue: Float = DEFAULT_BASE_VALUE

    fun lines(block: LINES.() -> Unit) {
        lines.addAll(LINES().apply(block))
    }

    fun build(): LineChartData = LineChartData(lines, baseValue)
}

@DslMarker
annotation class PieDataDsl

fun pieData(block: PieChartDataBuilder.() -> Unit): PieChartData = PieChartDataBuilder().apply(block).build()
@PieDataDsl
class PieChartDataBuilder {
    private var values: MutableList<SliceValue> = ArrayList()
    var axisXBottom: Axis? = null
    var axisYLeft: Axis? = null
    var hasLabels: Boolean = false
    var hasLabelsOnlyForSelected: Boolean = false
    var hasLabelsOutside: Boolean = false
    var hasCenterCircle: Boolean = false
    var centerCircleColor: Int = Color.TRANSPARENT
    var centerCircleScale: Float = PieChartData.DEFAULT_CENTER_CIRCLE_SCALE
    var centerText1Color: Int = Color.BLACK
    var centerText1FontSize: Int = PieChartData.DEFAULT_CENTER_TEXT1_SIZE_SP
    var centerText1Typeface: Typeface? = null
    var centerText1: String? = null
    var centerText2Color: Int = Color.BLACK
    var centerText2FontSize: Int = PieChartData.DEFAULT_CENTER_TEXT2_SIZE_SP
    var centerText2Typeface: Typeface? = null
    var centerText2: String? = null
    var sliceSpacing: Int = PieChartData.DEFAULT_SLICE_SPACING_DP
    var formatter: PieChartValueFormatter = SimplePieChartValueFormatter()

    fun sliceValues(block: SLICEVALUES.() -> Unit) {
        values.addAll(SLICEVALUES().apply(block))
    }

    fun build(): PieChartData = PieChartData(
            values,
            axisXBottom,
            axisYLeft,
            hasLabels,
            hasLabelsOnlyForSelected,
            hasLabelsOutside,
            hasCenterCircle,
            centerCircleColor,
            centerCircleScale,
            centerText1Color,
            centerText1FontSize,
            centerText1Typeface,
            centerText1,
            centerText2Color,
            centerText2FontSize,
            centerText2Typeface,
            centerText2,
            sliceSpacing,
            formatter
    )
}
