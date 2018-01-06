package co.csadev.kellocharts.model

import java.util.ArrayList

import co.csadev.kellocharts.formatter.BubbleChartValueFormatter
import co.csadev.kellocharts.formatter.SimpleBubbleChartValueFormatter
import co.csadev.kellocharts.view.Chart

/**
 * Data for BubbleChart.
 */
class BubbleChartData(var formatter: BubbleChartValueFormatter = SimpleBubbleChartValueFormatter(), var values: MutableList<BubbleValue> = ArrayList(), hasLabels: Boolean = false, var hasLabelsOnlyForSelected: Boolean = false, var minBubbleRadius: Int = DEFAULT_MIN_BUBBLE_RADIUS_DP, var bubbleScale: Float = DEFAULT_BUBBLE_SCALE) : AbstractChartData() {
    var hasLabels = hasLabels
        set(value) {
            field = value
            if (field) hasLabelsOnlyForSelected = false
        }

    fun copy() = BubbleChartData(formatter, values.map { it.copy() }.toMutableList(), hasLabels, hasLabelsOnlyForSelected, minBubbleRadius, bubbleScale)

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

    /**
     * Set true if you want to show value labels only for selected value, works best when chart has
     * isValueSelectionEnabled set to true [Chart.setValueSelectionEnabled].
     */
    fun setHasLabelsOnlyForSelected(hasLabelsOnlyForSelected: Boolean): BubbleChartData {
        this.hasLabelsOnlyForSelected = hasLabelsOnlyForSelected
        if (hasLabelsOnlyForSelected) {
            this.hasLabels = false
        }
        return this
    }

    companion object {
        val DEFAULT_MIN_BUBBLE_RADIUS_DP = 6
        val DEFAULT_BUBBLE_SCALE = 1f

        fun generateDummyData(): BubbleChartData {
            val numValues = 4
            val data = BubbleChartData()
            val values = ArrayList<BubbleValue>(numValues)
            values.add(BubbleValue(0f, 20f, 15000f))
            values.add(BubbleValue(3f, 22f, 20000f))
            values.add(BubbleValue(5f, 25f, 5000f))
            values.add(BubbleValue(7f, 30f, 30000f))
            values.add(BubbleValue(11f, 22f, 10f))
            data.values = values
            return data
        }
    }
}
