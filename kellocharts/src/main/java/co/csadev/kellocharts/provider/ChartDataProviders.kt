package co.csadev.kellocharts.provider

import co.csadev.kellocharts.model.*

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
