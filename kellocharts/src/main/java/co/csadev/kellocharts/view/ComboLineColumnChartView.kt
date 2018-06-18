package co.csadev.kellocharts.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import co.csadev.kellocharts.BuildConfig
import co.csadev.kellocharts.listener.ComboLineColumnChartOnValueSelectListener
import co.csadev.kellocharts.listener.DummyCompoLineColumnChartOnValueSelectListener
import co.csadev.kellocharts.model.ChartData
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.model.ComboLineColumnChartData
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.SelectedValue.SelectedValueType
import co.csadev.kellocharts.provider.ColumnChartDataProvider
import co.csadev.kellocharts.provider.ComboLineColumnChartDataProvider
import co.csadev.kellocharts.provider.LineChartDataProvider
import co.csadev.kellocharts.renderer.ColumnChartRenderer
import co.csadev.kellocharts.renderer.ComboLineColumnChartRenderer
import co.csadev.kellocharts.renderer.LineChartRenderer

/**
 * ComboChart, supports ColumnChart combined with LineChart. Lines are always drawn on top.
 *
 * @author Leszek Wach
 */
class ComboLineColumnChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AbstractChartView(context, attrs, defStyle), ComboLineColumnChartDataProvider {
    var columnChartDataProvider: ColumnChartDataProvider = ComboColumnChartDataProvider()
    var lineChartDataProvider: LineChartDataProvider = ComboLineChartDataProvider()
    var onValueTouchListener: ComboLineColumnChartOnValueSelectListener = DummyCompoLineColumnChartOnValueSelectListener()

    override// generateDummyData();
    var comboLineColumnChartData: ComboLineColumnChartData = ComboLineColumnChartData.generateDummyData()
        set(value) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Setting data for ComboLineColumnChartView")
            }
            field = value
            super.onChartDataChange()
        }

    override val chartData: ChartData
        get() = comboLineColumnChartData

    init {
        chartRenderer = ComboLineColumnChartRenderer(context, this, columnChartDataProvider, lineChartDataProvider)
    }

    override fun callTouchListener() {
        val selectedValue = chartRenderer.selectedValue

        if (selectedValue.isSet) {

            if (SelectedValueType.COLUMN == selectedValue.type) {

                val value = comboLineColumnChartData.columnChartData.columns[selectedValue.firstIndex].values[selectedValue.secondIndex]
                onValueTouchListener.onColumnValueSelected(selectedValue.firstIndex,
                        selectedValue.secondIndex, value)

            } else if (SelectedValueType.LINE == selectedValue.type) {

                val value = comboLineColumnChartData.lineChartData.lines[selectedValue.firstIndex].values[selectedValue.secondIndex]
                onValueTouchListener.onPointValueSelected(selectedValue.firstIndex, selectedValue.secondIndex,
                        value)

            } else {
                throw IllegalArgumentException("Invalid selected value type " + selectedValue.type!!.name)
            }
        } else {
            onValueTouchListener.onValueDeselected()
        }
    }

    fun setColumnChartRenderer(context: Context, columnChartRenderer: ColumnChartRenderer) {
        chartRenderer = ComboLineColumnChartRenderer(context, this, columnChartRenderer, lineChartDataProvider)
    }

    fun setLineChartRenderer(context: Context, lineChartRenderer: LineChartRenderer) {
        chartRenderer = ComboLineColumnChartRenderer(context, this, columnChartDataProvider, lineChartRenderer)
    }

    private inner class ComboLineChartDataProvider : LineChartDataProvider {

        override var lineChartData: LineChartData
            get() = comboLineColumnChartData.lineChartData
            set(data) { comboLineColumnChartData.lineChartData = data }

    }

    private inner class ComboColumnChartDataProvider : ColumnChartDataProvider {

        override var columnChartData: ColumnChartData
            get() = comboLineColumnChartData.columnChartData
            set(data) { comboLineColumnChartData.columnChartData = data }

    }

    companion object {
        private val TAG = "ComboLCChartView"
    }

}
