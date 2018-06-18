package co.csadev.kellocharts.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import co.csadev.kellocharts.BuildConfig
import co.csadev.kellocharts.listener.ColumnChartOnValueSelectListener
import co.csadev.kellocharts.listener.DummyColumnChartOnValueSelectListener
import co.csadev.kellocharts.model.ChartData
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.provider.ColumnChartDataProvider
import co.csadev.kellocharts.renderer.ColumnChartRenderer

/**
 * ColumnChart/BarChart, supports subcolumns, stacked columns, horizontal mode, and negative values.
 *
 * @author Leszek Wach
 */
open class ColumnChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AbstractChartView(context, attrs, defStyle), ColumnChartDataProvider {
    override var columnChartData: ColumnChartData = ColumnChartData.generateDummyData()
        set(value) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Setting data for ColumnChartView")
            }
            field = value
            super.onChartDataChange()

        }

    override val chartData: ChartData
        get() = columnChartData

    var onValueTouchListener: ColumnChartOnValueSelectListener? = DummyColumnChartOnValueSelectListener()
        set(touchListener) {
            if (null != touchListener) {
                field = touchListener
            }
        }

    init {
        chartRenderer = ColumnChartRenderer(context, this, this)
    }

    override fun callTouchListener() {
        val selectedValue = chartRenderer.selectedValue

        if (selectedValue.isSet) {
            val value = columnChartData.columns[selectedValue.firstIndex].values[selectedValue.secondIndex]
            this.onValueTouchListener?.onValueSelected(selectedValue.firstIndex, selectedValue.secondIndex, value)
        } else {
            this.onValueTouchListener?.onValueDeselected()
        }
    }

    companion object {
        private val TAG = "ColumnChartView"
    }
}
