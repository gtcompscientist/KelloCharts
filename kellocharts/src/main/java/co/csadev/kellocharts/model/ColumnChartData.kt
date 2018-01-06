package co.csadev.kellocharts.model

import java.util.ArrayList

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

    override var axisXBottom: Axis? = if (isHorizontal) super.axisYLeft else super.axisXBottom
    override var axisYLeft: Axis? = if (isHorizontal) super.axisXBottom?.apply { isReversed = true } else super.axisYLeft
    override var axisXTop: Axis? = if (isHorizontal) super.axisYRight else super.axisXTop
    override var axisYRight: Axis? = if (isHorizontal) super.axisXTop?.apply { isReversed = true } else super.axisYRight

    fun copy() = ColumnChartData(columns.map { it.copy() }.toMutableList(), isStacked, isHorizontal).withData(this) as ColumnChartData

    override fun update(scale: Float) {
        for (column in columns) {
            column.update(scale)
        }

    }

    override fun finish() = columns.forEach { it.finish() }

    companion object {
        val DEFAULT_FILL_RATIO = 0.75f
        val DEFAULT_BASE_VALUE = 0.0f

        fun generateDummyData(): ColumnChartData {
            val numColumns = 4
            val data = ColumnChartData()
            val columns = ArrayList<Column>(numColumns)
            var values: MutableList<SubcolumnValue>
            var column: Column
            for (i in 1..numColumns) {
                values = ArrayList(numColumns)
                values.add(SubcolumnValue(i.toFloat()))
                column = Column(values)
                columns.add(column)
            }

            data.columns = columns
            return data
        }
    }
}
