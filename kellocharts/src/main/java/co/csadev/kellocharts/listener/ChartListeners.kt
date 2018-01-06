package co.csadev.kellocharts.listener

import co.csadev.kellocharts.model.*

interface BubbleChartOnValueSelectListener : OnValueDeselectListener {
    fun onValueSelected(bubbleIndex: Int, value: BubbleValue)
}

interface ColumnChartOnValueSelectListener : OnValueDeselectListener {
    fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue)
}

interface ComboLineColumnChartOnValueSelectListener : OnValueDeselectListener {
    fun onColumnValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue)
    fun onPointValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue)
}

interface LineChartOnValueSelectListener : OnValueDeselectListener {
    fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue)
}

interface OnValueDeselectListener {
    fun onValueDeselected()
}

interface PieChartOnValueSelectListener : OnValueDeselectListener {
    fun onValueSelected(arcIndex: Int, value: SliceValue)
}

interface ViewportChangeListener {
    fun onViewportChanged(viewport: Viewport)

}
