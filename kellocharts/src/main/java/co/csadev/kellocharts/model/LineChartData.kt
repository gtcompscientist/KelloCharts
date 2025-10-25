package co.csadev.kellocharts.model

import androidx.compose.runtime.Immutable
import co.csadev.kellocharts.model.dsl.lineData
import java.util.*

/**
 * Data model for LineChartView.
 *
 * Note: Marked as @Immutable for Compose optimization. While the class has mutable
 * fields, in practice it should be treated as immutable after creation for best
 * Compose performance. Create new instances for data changes.
 */
@Immutable
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
