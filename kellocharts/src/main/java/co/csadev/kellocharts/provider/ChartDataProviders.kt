package co.csadev.kellocharts.provider

import co.csadev.kellocharts.model.BubbleChartData
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.model.ComboLineColumnChartData
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PieChartData

interface BubbleChartDataProvider {
    var bubbleChartData: BubbleChartData
}

interface ColumnChartDataProvider {
    var columnChartData: ColumnChartData
}

interface ComboLineColumnChartDataProvider {
    var comboLineColumnChartData: ComboLineColumnChartData
}

interface LineChartDataProvider {
    var lineChartData: LineChartData
}

interface PieChartDataProvider {
    var pieChartData: PieChartData
}
