package co.csadev.kellocharts.listener

import co.csadev.kellocharts.model.*

class DummyBubbleChartOnValueSelectListener : BubbleChartOnValueSelectListener {
    override fun onValueSelected(bubbleIndex: Int, value: BubbleValue) = Unit
    override fun onValueDeselected() = Unit
}

class DummyColumnChartOnValueSelectListener : ColumnChartOnValueSelectListener {
    override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) = Unit
    override fun onValueDeselected() = Unit
}

class DummyCompoLineColumnChartOnValueSelectListener : ComboLineColumnChartOnValueSelectListener {
    override fun onColumnValueSelected(
        columnIndex: Int,
        subcolumnIndex: Int,
        value: SubcolumnValue
    ) {
    }

    override fun onPointValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) = Unit
    override fun onValueDeselected() = Unit
}

class DummyLineChartOnValueSelectListener : LineChartOnValueSelectListener {
    override fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) = Unit
    override fun onValueDeselected() = Unit
}

class DummyPieChartOnValueSelectListener : PieChartOnValueSelectListener {
    override fun onValueSelected(arcIndex: Int, value: SliceValue) = Unit
    override fun onValueDeselected() = Unit
}

class DummyViewportChangeListener : ViewportChangeListener {
    override fun onViewportChanged(viewport: Viewport) = Unit
}
