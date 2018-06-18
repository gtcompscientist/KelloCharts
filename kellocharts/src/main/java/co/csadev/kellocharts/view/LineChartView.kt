package co.csadev.kellocharts.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import co.csadev.kellocharts.BuildConfig
import co.csadev.kellocharts.listener.DummyLineChartOnValueSelectListener
import co.csadev.kellocharts.listener.LineChartOnValueSelectListener
import co.csadev.kellocharts.model.ChartData
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.provider.LineChartDataProvider
import co.csadev.kellocharts.renderer.LineChartRenderer

/**
 * LineChart, supports cubic lines, filled lines, circle and square points. Point radius and stroke width can be
 * adjusted using LineChartData attributes.
 *
 * @author Leszek Wach
 */
open class LineChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AbstractChartView(context, attrs, defStyle), LineChartDataProvider {
    var onValueTouchListener: LineChartOnValueSelectListener = DummyLineChartOnValueSelectListener()

    override var lineChartData: LineChartData = LineChartData.generateDummyData()
        set(value) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Setting data for LineChartView")
            }
            field = value
            super.onChartDataChange()
        }

    override val chartData: ChartData
        get() = lineChartData

    init {
        chartRenderer = LineChartRenderer(context, this, this)
    }

    override fun callTouchListener() {
        val selectedValue = chartRenderer.selectedValue

        if (selectedValue.isSet) {
            val point = lineChartData.lines[selectedValue.firstIndex].values[selectedValue.secondIndex]
            onValueTouchListener.onValueSelected(selectedValue.firstIndex, selectedValue.secondIndex, point)
        } else {
            onValueTouchListener.onValueDeselected()
        }
    }

    companion object {
        private val TAG = "LineChartView"
    }
}
