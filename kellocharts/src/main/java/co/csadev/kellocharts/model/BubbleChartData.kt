package co.csadev.kellocharts.model

import co.csadev.kellocharts.formatter.BubbleChartValueFormatter
import co.csadev.kellocharts.formatter.SimpleBubbleChartValueFormatter
import co.csadev.kellocharts.model.dsl.bubbleData
import co.csadev.kellocharts.model.dsl.bubbleValue
import co.csadev.kellocharts.view.Chart
import java.util.*

/**
 * Data for BubbleChart.
 */
class BubbleChartData(var values: MutableList<BubbleValue> = ArrayList(), var formatter: BubbleChartValueFormatter = SimpleBubbleChartValueFormatter(), hasLabels: Boolean = false, var hasLabelsOnlyForSelected: Boolean = false, var minBubbleRadius: Int = DEFAULT_MIN_BUBBLE_RADIUS_DP, var bubbleScale: Float = DEFAULT_BUBBLE_SCALE) : AbstractChartData() {
    var hasLabels = hasLabels
        set(value) {
            field = value
            if (field) hasLabelsOnlyForSelected = false
        }

    fun copy() = BubbleChartData(values.map { it.copy() }.toMutableList(), formatter, hasLabels, hasLabelsOnlyForSelected, minBubbleRadius, bubbleScale)

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

        fun generateDummyData() =
                bubbleData {
                    bubbles {
                        bubble {
                            x = 0f
                            y = 20f
                            z = 15000f
                        }
                        bubble {
                            x = 3f
                            y = 22f
                            z = 20000f
                        }
                        bubble {
                            x = 5f
                            y = 25f
                            z = 5000f
                        }
                        bubble {
                            x = 7f
                            y = 30f
                            z = 30000f
                        }
                        bubble {
                            x = 11f
                            y = 22f
                            z = 10f
                        }
                    }
                }
    }
}
