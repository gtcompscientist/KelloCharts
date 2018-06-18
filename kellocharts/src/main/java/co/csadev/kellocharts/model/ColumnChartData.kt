package co.csadev.kellocharts.model

import co.csadev.kellocharts.model.dsl.columnData
import java.util.*

/**
 * Data model for column chart. Note: you can set X value for columns or sub-columns, columns are by default indexed
 * from 0 to numOfColumns-1 and
 * column index is used as column X value, so first column has X value 0, second clumn has X value 1 etc.
 * If you want to display AxisValue for given column you should initialize AxisValue with X value of that column.
 */
class ColumnChartData(var columns: MutableList<Column> = ArrayList(), var isStacked: Boolean = false, var isHorizontal: Boolean = true) : AbstractChartData() {
    var fillRatio = DEFAULT_FILL_RATIO
        set(value) {
            field = Math.min(1f, Math.max(0f, value))
        }
    var baseValue = DEFAULT_BASE_VALUE


    var _axisXTop: Axis? = null
    override var axisXTop: Axis?
        get() {
            return if (isHorizontal) {
                _axisYRight
            } else _axisXTop
        }
        set(value) { _axisXTop = value }
    var _axisXBottom: Axis? = null
    override var axisXBottom: Axis?
        get() {
            return if (isHorizontal) {
                _axisYLeft
            } else _axisXBottom
        }
        set(value) { _axisXBottom = value }
    var _axisYLeft: Axis? = null
    override var axisYLeft: Axis?
        get() {
            return if (isHorizontal) {
                _axisXBottom
            } else _axisYLeft
        }
        set(value) { _axisYLeft = value }
    var _axisYRight: Axis? = null
    override var axisYRight: Axis?
        get() {
            return if (isHorizontal) {
                _axisXTop
            } else _axisYRight
        }
        set(value) { _axisYRight = value }

    fun copy() = ColumnChartData(columns.map { it.copy() }.toMutableList(), isStacked, isHorizontal).withData(this) as ColumnChartData

    override fun update(scale: Float) {
        for (column in columns) {
            column.update(scale)
        }

    }

    override fun finish() = columns.forEach { it.finish() }

    companion object {
        const val DEFAULT_FILL_RATIO = 0.75f
        const val DEFAULT_BASE_VALUE = 0.0f

        fun generateDummyData() =
                columnData {
                    columns {
                        column {
                            columnValues {
                                subcolumn {
                                    value = 4f
                                }
                                subcolumn {
                                    value = 3f
                                }
                                subcolumn {
                                    value = 2f
                                }
                                subcolumn {
                                    value = 1f
                                }
                            }
                        }
                    }
                }
    }
}
