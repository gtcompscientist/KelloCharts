package co.csadev.kellocharts.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.core.view.ViewCompat
import co.csadev.kellocharts.BuildConfig
import co.csadev.kellocharts.listener.BubbleChartOnValueSelectListener
import co.csadev.kellocharts.listener.DummyBubbleChartOnValueSelectListener
import co.csadev.kellocharts.model.BubbleChartData
import co.csadev.kellocharts.model.ChartData
import co.csadev.kellocharts.provider.BubbleChartDataProvider
import co.csadev.kellocharts.renderer.BubbleChartRenderer

/**
 * BubbleChart, supports circle bubbles and square bubbles.
 *
 * @author lecho
 */
class BubbleChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : AbstractChartView(context, attrs, defStyle), BubbleChartDataProvider {
    var onValueTouchListener: BubbleChartOnValueSelectListener = DummyBubbleChartOnValueSelectListener()

    var bubbleChartRenderer: BubbleChartRenderer = BubbleChartRenderer(context, this, this)

    override var bubbleChartData: BubbleChartData = BubbleChartData.generateDummyData()
        set(value) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Setting data for BubbleChartView")
            }
            field = value
            super.onChartDataChange()
        }

    override val chartData: ChartData
        get() = bubbleChartData

    init {
        chartRenderer = bubbleChartRenderer
        bubbleChartData = BubbleChartData.generateDummyData()
    }

    override fun callTouchListener() {
        val selectedValue = chartRenderer.selectedValue

        if (selectedValue.isSet) {
            val value = bubbleChartData.values[selectedValue.firstIndex]
            onValueTouchListener.onValueSelected(selectedValue.firstIndex, value)
        } else {
            onValueTouchListener.onValueDeselected()
        }
    }

    /**
     * Removes empty spaces, top-bottom for portrait orientation and left-right for landscape. This method has to be
     * called after view View#onSizeChanged() method is called and chart data is set. This method may be inaccurate.
     *
     * @see BubbleChartRenderer.removeMargins
     */
    fun removeMargins() {
        bubbleChartRenderer.removeMargins()
        ViewCompat.postInvalidateOnAnimation(this)
    }

    companion object {
        private val TAG = "BubbleChartView"
    }
}
