package co.csadev.kellocharts.sample

import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.BubbleChartData
import co.csadev.kellocharts.model.BubbleValue
import co.csadev.kellocharts.model.ValueShape
import co.csadev.kellocharts.util.ChartUtils
import java.lang.Math.random
import kotlin.math.roundToInt

private const val SAMPLE_COUNT = 50

private val randomF: Float
    get() = random().toFloat()

private val pickColor: Int
    get() = ChartUtils.COLORS[(random() * (ChartUtils.COLORS.size - 1)).roundToInt()]

internal fun generateData(
    shape: ValueShape,
    hasLabels: Boolean,
    hasLabelsForSelected: Boolean,
    hasAxes: Boolean,
    hasAxesNames: Boolean
) {
    val values = (0 until SAMPLE_COUNT).map {
        BubbleValue(it.toFloat(), randomF * 100, randomF * 1000, pickColor, shape = shape)
    }.toMutableList()

    val newData = BubbleChartData(
        values = values,
        hasLabels = hasLabels,
        hasLabelsOnlyForSelected = hasLabelsForSelected
    )

    if (hasAxes) {
        val axisX = Axis(hasLines = true)
        val axisY = Axis(hasLines = true)
        if (hasAxesNames) {
            axisX.name = "Axis X"
            axisY.name = "Axis Y"
        }
        newData.axisXBottom = axisX
        newData.axisYLeft = axisY
    }
}
