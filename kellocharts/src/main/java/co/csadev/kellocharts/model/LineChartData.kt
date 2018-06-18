package co.csadev.kellocharts.model

import co.csadev.kellocharts.model.dsl.line
import co.csadev.kellocharts.model.dsl.lineData
import co.csadev.kellocharts.model.dsl.pointValue
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

    fun copy() = LineChartData(lines.map { it.copy() }.toMutableList(), baseValue).withData(this) as LineChartData

    companion object {
        val DEFAULT_BASE_VALUE = 0.0f

        fun generateDummyData() =
                lineData {
                    lines {
                        line {
                            pointValue {
                                x = 0f
                                y = 2f
                            }
                            pointValue {
                                x = 1f
                                y = 4f
                            }
                            pointValue {
                                x = 2f
                                y = 3f
                            }
                            pointValue {
                                x = 3f
                                y = 4f
                            }
                        }
                    }
                }
    }
}
