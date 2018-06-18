package co.csadev.kellocharts.model

import co.csadev.kellocharts.model.dsl.lineData
import java.util.*

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

    fun copy() = LineChartData(lines.map { it.copy() }.toMutableList(), baseValue).withData(this) as LineChartData

    companion object {
        const val DEFAULT_BASE_VALUE = 0.0f

        fun generateDummyData() =
                lineData {
                    lines {
                        line {
                            pointValues {
                                point {
                                    x = 0f
                                    y = 2f
                                }
                                point {
                                    x = 1f
                                    y = 4f
                                }
                                point {
                                    x = 2f
                                    y = 3f
                                }
                                point {
                                    x = 3f
                                    y = 4f
                                }
                            }
                        }
                    }
                }
    }
}
