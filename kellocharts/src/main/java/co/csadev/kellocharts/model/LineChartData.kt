package co.csadev.kellocharts.model

import java.util.ArrayList

/**
 * Data model for LineChartView.
 */
class LineChartData(var lines: MutableList<Line> = ArrayList(),  var baseValue: Float = DEFAULT_BASE_VALUE) : AbstractChartData() {

    override fun update(scale: Float) {
        for (line in lines) {
            line.update(scale)
        }
    }

    override fun finish() {
        for (line in lines) {
            line.finish()
        }
    }

    fun copy() = LineChartData(lines.map { it.copy() }.toMutableList(), baseValue).withData(this)

    companion object {
        val DEFAULT_BASE_VALUE = 0.0f

        fun generateDummyData(): LineChartData {
            return LineChartData(arrayListOf(Line(arrayListOf(
                    PointValue(0f, 2f),
                    PointValue(1f, 4f),
                    PointValue(2f, 3f),
                    PointValue(3f,4f)
            ))))
        }
    }
}
