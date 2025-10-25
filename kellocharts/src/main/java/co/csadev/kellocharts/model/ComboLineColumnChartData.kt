package co.csadev.kellocharts.model

import androidx.compose.runtime.Immutable

/**
 * Data model for combo line-column chart. It uses ColumnChartData and LineChartData internally.
 *
 * Note: Marked as @Immutable for Compose optimization. Create new instances for data changes.
 */
@Immutable
class ComboLineColumnChartData(var columnChartData: ColumnChartData = ColumnChartData(), var lineChartData: LineChartData = LineChartData()) : AbstractChartData() {
    override fun update(scale: Float) {
        columnChartData.update(scale)
        lineChartData.update(scale)
    }

    override fun finish() {
        columnChartData.finish()
        lineChartData.finish()
    }

    companion object {
        fun generateDummyData() = ComboLineColumnChartData(ColumnChartData.generateDummyData(), LineChartData.generateDummyData())
        fun fromComboData(data: ComboLineColumnChartData) = ComboLineColumnChartData(data.columnChartData, data.lineChartData).withData(data) as ComboLineColumnChartData
    }
}
