package co.csadev.kellocharts.listener

import co.csadev.kellocharts.model.*

class DummyBubbleChartOnValueSelectListener : BubbleChartOnValueSelectListener {
    override fun onValueSelected(bubbleIndex: Int, value: BubbleValue) { }
    override fun onValueDeselected() { }
}

class DummyColumnChartOnValueSelectListener : ColumnChartOnValueSelectListener {
    override fun onValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {}
    override fun onValueDeselected() {}
}


class DummyCompoLineColumnChartOnValueSelectListener : ComboLineColumnChartOnValueSelectListener {
    override fun onColumnValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) { }
    override fun onPointValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) { }
    override fun onValueDeselected() { }
}

class DummyLineChartOnValueSelectListener : LineChartOnValueSelectListener {
    override fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) { }
    override fun onValueDeselected() { }
}

class DummyPieChartOnValueSelectListener : PieChartOnValueSelectListener {
    override fun onValueSelected(arcIndex: Int, value: SliceValue) { }
    override fun onValueDeselected() { }
}

class DummyViewportChangeListener : ViewportChangeListener {
    override fun onViewportChanged(viewport: Viewport) { }
}
