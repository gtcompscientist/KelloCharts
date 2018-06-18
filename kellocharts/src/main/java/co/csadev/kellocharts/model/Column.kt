package co.csadev.kellocharts.model

import co.csadev.kellocharts.formatter.ColumnChartValueFormatter
import co.csadev.kellocharts.formatter.SimpleColumnChartValueFormatter
import java.util.*

/**
 * Single column for ColumnChart. One column can be divided into multiple sub-columns(ColumnValues) especially for
 * stacked ColumnChart.
 * Note: you can set X value for columns or sub-columns, columns are by default indexed from 0 to numOfColumns-1 and
 * column index is used as column X value, so first column has X value 0, second clumn has X value 1 etc.
 * If you want to display AxisValue for given column you should initialize AxisValue with X value of that column.
 */
class Column(var values: MutableList<SubcolumnValue> = ArrayList(), hasLabels: Boolean = false, hasLabelsOnlyForSelected: Boolean = false, var formatter: ColumnChartValueFormatter = SimpleColumnChartValueFormatter()) {
    var hasLabels: Boolean = hasLabels
        set(value) {
            field = value
            if (field) hasLabelsOnlyForSelected = false
        }

    var hasLabelsOnlyForSelected: Boolean = hasLabelsOnlyForSelected
        set(value) {
            field = value
            if (field) hasLabels = false
        }

    fun update(scale: Float) = values.forEach { it.update(scale) }

    fun finish() = values.forEach { it.finish() }

    fun copy() = Column(values.map { it.copy() }.toMutableList(), hasLabels, hasLabelsOnlyForSelected, formatter)
}
