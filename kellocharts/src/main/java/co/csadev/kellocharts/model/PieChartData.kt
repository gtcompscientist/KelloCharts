package co.csadev.kellocharts.model

import android.graphics.Color
import android.graphics.Typeface
import co.csadev.kellocharts.formatter.PieChartValueFormatter
import co.csadev.kellocharts.formatter.SimplePieChartValueFormatter
import co.csadev.kellocharts.model.dsl.pieData
import co.csadev.kellocharts.model.dsl.sliceValue
import java.util.*

/**
 * Data for PieChart, by default it doesn't have axes.
 */
class PieChartData(var values: MutableList<SliceValue> = ArrayList(),
                   override var axisXBottom: Axis? = null,
                   override var axisYLeft: Axis? = null,
                   hasLabels: Boolean = false,
                   hasLabelsOnlyForSelected: Boolean = false,
                   var hasLabelsOutside: Boolean = false,
                   var hasCenterCircle: Boolean = false,
                   var centerCircleColor: Int = Color.TRANSPARENT,
                   var centerCircleScale: Float = DEFAULT_CENTER_CIRCLE_SCALE,
                   var centerText1Color: Int = Color.BLACK,
                   var centerText1FontSize: Int = DEFAULT_CENTER_TEXT1_SIZE_SP,
                   var centerText1Typeface: Typeface? = null,
                   var centerText1: String? = null,
                   var centerText2Color: Int = Color.BLACK,
                   var centerText2FontSize: Int = DEFAULT_CENTER_TEXT2_SIZE_SP,
                   var centerText2Typeface: Typeface? = null,
                   var centerText2: String? = null,
                   var sliceSpacing: Int = DEFAULT_SLICE_SPACING_DP,
                   var formatter: PieChartValueFormatter = SimplePieChartValueFormatter()) : AbstractChartData() {
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

    override fun update(scale: Float) {
        for (value in values) {
            value.update(scale)
        }
    }

    override fun finish() {
        for (value in values) {
            value.finish()
        }
    }

    fun copy() = PieChartData(values.map { it.copy() }.toMutableList(), axisXBottom, axisYLeft, hasLabels, hasLabelsOnlyForSelected, hasLabelsOutside, hasCenterCircle, centerCircleColor, centerCircleScale, centerText1Color, centerText1FontSize, centerText1Typeface, centerText1, centerText2Color, centerText2FontSize, centerText2Typeface, centerText2, sliceSpacing, formatter).withData(this)

    companion object {
        const val DEFAULT_CENTER_TEXT1_SIZE_SP = 42
        const val DEFAULT_CENTER_TEXT2_SIZE_SP = 16
        const val DEFAULT_CENTER_CIRCLE_SCALE = 0.6f
        internal const val DEFAULT_SLICE_SPACING_DP = 2

        fun generateDummyData() =
                pieData {
                    sliceValues {
                        sliceValue {
                            slice { value = 40f }
                        }
                        sliceValue {
                            slice { value = 20f }
                        }
                        sliceValue {
                            slice { value = 30f }
                        }
                        sliceValue {
                            slice { value = 50f }
                        }
                    }
                }
    }
}
