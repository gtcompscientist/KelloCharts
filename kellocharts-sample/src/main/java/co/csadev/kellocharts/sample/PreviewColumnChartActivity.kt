package co.csadev.kellocharts.sample

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.listener.ViewportChangeListener
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.ColumnChartView
import co.csadev.kellocharts.view.PreviewColumnChartView

class PreviewColumnChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_column_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment()).commit()
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : androidx.fragment.app.Fragment() {

        private var chart: ColumnChartView? = null
        private var previewChart: PreviewColumnChartView? = null
        private var data: ColumnChartData = generateDefaultData()
        /**
         * Deep copy of data.
         */
        private var previewData: ColumnChartData = darkenDefaultData()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            setHasOptionsMenu(true)
            val rootView = inflater.inflate(R.layout.fragment_preview_column_chart, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as ColumnChartView
            previewChart = rootView.findViewById<View>(R.id.chart_preview) as PreviewColumnChartView

            // Generate data for previewed chart and copy of that data for preview chart.
            generateDefaultData()

            chart?.columnChartData = data!!
            // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
            // zoom/scroll is unnecessary.
            chart?.isZoomEnabled = false
            chart?.isScrollEnabled = false

            previewChart?.columnChartData = previewData
            previewChart?.setViewportChangeListener(ViewportListener())

            previewX(false)

            return rootView
        }

        // MENU
        override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
            inflater!!.inflate(R.menu.preview_column_chart, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            val id = item!!.itemId
            if (id == R.id.action_reset) {
                generateDefaultData()
                chart?.columnChartData = data
                previewChart?.columnChartData = previewData
                previewX(true)
                return true
            }
            if (id == R.id.action_preview_both) {
                previewXY()
                previewChart?.zoomType = ZoomType.HORIZONTAL_AND_VERTICAL
                return true
            }
            if (id == R.id.action_preview_horizontal) {
                previewX(true)
                return true
            }
            if (id == R.id.action_preview_vertical) {
                previewY()
                return true
            }
            if (id == R.id.action_change_color) {
                var color = ChartUtils.pickColor()
                while (color == previewChart?.previewColor) {
                    color = ChartUtils.pickColor()
                }
                previewChart?.previewColor = color
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun generateDefaultData(): ColumnChartData {
            val numSubcolumns = 1
            val numColumns = 50
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            for (i in 0 until numColumns) {

                values = ArrayList()
                for (j in 0 until numSubcolumns) {
                    values.add(SubcolumnValue(Math.random().toFloat() * 50f + 5, ChartUtils.pickColor()))
                }

                columns.add(Column(values))
            }

            val data = ColumnChartData(columns)
            data.axisXBottom = Axis(hasLines = true)
            data.axisYLeft = Axis(hasLines = true)
            return data
        }

        // prepare preview data, is better to use separate deep copy for preview chart.
        // set color to grey to make preview area more visible.
        private fun darkenDefaultData(): ColumnChartData {
            val darkData = data.copy()
            darkData.columns.forEach { column ->
                column.values.forEach { value ->
                    value.color = ChartUtils.DEFAULT_DARKEN_COLOR
                }
            }
            return darkData
        }

        private fun previewY() {
            val tempViewport = chart?.maximumViewport.copy()
            val dy = tempViewport.height() / 4
            tempViewport.inset(0f, dy)
            previewChart?.setCurrentViewportWithAnimation(tempViewport)
            previewChart?.zoomType = ZoomType.VERTICAL
        }

        private fun previewX(animate: Boolean) {
            val tempViewport = chart?.maximumViewport.copy()
            val dx = tempViewport.width() / 4
            tempViewport.inset(dx, 0f)
            if (animate) {
                previewChart?.setCurrentViewportWithAnimation(tempViewport)
            } else {
                previewChart?.currentViewport = tempViewport
            }
            previewChart?.zoomType = ZoomType.HORIZONTAL
        }

        private fun previewXY() {
            // Better to not modify viewport of any chart directly so create a copy.
            val tempViewport = chart?.maximumViewport.copy()
            // Make temp viewport smaller.
            val dx = tempViewport.width() / 4
            val dy = tempViewport.height() / 4
            tempViewport.inset(dx, dy)
            previewChart?.setCurrentViewportWithAnimation(tempViewport)
        }

        /**
         * Viewport listener for preview chart(lower one). in [.onViewportChanged] method change
         * viewport of upper chart.
         */
        private inner class ViewportListener : ViewportChangeListener {

            override fun onViewportChanged(viewport: Viewport) {
                // don't use animation, it is unnecessary when using preview chart because usually viewport changes
                // happens to often.
                chart?.currentViewport = viewport
            }

        }
    }
}
