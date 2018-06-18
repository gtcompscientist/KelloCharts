package co.csadev.kellocharts.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.core.view.ViewCompat
import co.csadev.kellocharts.BuildConfig
import co.csadev.kellocharts.computator.ChartComputator
import co.csadev.kellocharts.computator.PreviewChartComputator
import co.csadev.kellocharts.gesture.PreviewChartTouchHandler
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.renderer.PreviewLineChartRenderer

/**
 * Preview chart that can be used as overview for other LineChart. When you change Viewport of this chart, visible area
 * of other chart will change. For that you need also to use
 * [Chart.setViewportChangeListener]
 *
 * @author Leszek Wach
 */
class PreviewLineChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LineChartView(context, attrs, defStyle) {
    override val chartComputator: ChartComputator = PreviewChartComputator()
    protected var previewChartRenderer: PreviewLineChartRenderer = PreviewLineChartRenderer(context, this, this)

    var previewColor: Int
        get() = previewChartRenderer.previewColor
        set(color) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Changing preview area color")
            }

            previewChartRenderer.previewColor = color
            ViewCompat.postInvalidateOnAnimation(this)
        }

    init {
        touchHandler = PreviewChartTouchHandler(context, this)
        chartRenderer = previewChartRenderer
        lineChartData = LineChartData.generateDummyData()
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        val offset = computeHorizontalScrollOffset()
        val range = computeHorizontalScrollRange() - computeHorizontalScrollExtent()
        if (range == 0) return false
        return if (direction < 0) {
            offset > 0
        } else {
            offset < range - 1
        }
    }

    companion object {
        private val TAG = "PreviewLineChartView"
    }
}
