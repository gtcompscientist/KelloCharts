package co.csadev.kellocharts.sample

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.listener.ViewportChangeListener
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.LineChartView
import co.csadev.kellocharts.view.PreviewLineChartView

class PreviewLineChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_line_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment())
                .commit()
        }
    }

    /**
     * A fragment containing a line chart and preview line chart.
     */
    class PlaceholderFragment : Fragment() {

        private var chart: LineChartView? = null
        private var previewChart: PreviewLineChartView? = null
        private var data: LineChartData = generateDefaultData()

        /**
         * Deep copy of data.
         */
        private var previewData: LineChartData = darkenPreviewData()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            setHasOptionsMenu(true)
            val rootView = inflater.inflate(R.layout.fragment_preview_line_chart, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as LineChartView
            previewChart = rootView.findViewById<View>(R.id.chart_preview) as PreviewLineChartView

            // Generate data for previewed chart and copy of that data for preview chart.
            generateDefaultData()

            chart?.lineChartData = data
            // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
            // zoom/scroll is unnecessary.
            chart?.isZoomEnabled = false
            chart?.isScrollEnabled = false

            previewChart?.lineChartData = previewData
            previewChart?.setViewportChangeListener(ViewportListener())

            previewX(false)

            return rootView
        }

        // MENU
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.preview_line_chart, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == R.id.action_reset) {
                generateDefaultData()
                chart?.lineChartData = data
                previewChart?.lineChartData = previewData
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

        private fun generateDefaultData(): LineChartData {
            val numValues = 50

            val values = ArrayList<PointValue>()
            for (i in 0 until numValues) {
                values.add(PointValue(i.toFloat(), Math.random().toFloat() * 100f))
            }

            val line = Line(values)
            line.color = ChartUtils.COLOR_GREEN
            line.hasPoints = false // too many values so don't draw points.

            val lines = ArrayList<Line>()
            lines.add(line)

            val data = LineChartData(lines)
            data.axisXBottom = Axis(hasLines = true)
            data.axisYLeft = Axis(hasLines = true)
            return data
        }

        // prepare preview data, is better to use separate deep copy for preview chart.
        // Set color to grey to make preview area more visible.
        private fun darkenPreviewData(): LineChartData {
            val previewData = data.copy()
            previewData.lines[0].color = ChartUtils.DEFAULT_DARKEN_COLOR
            return previewData
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

            override fun onViewportChanged(newViewport: Viewport) {
                // don't use animation, it is unnecessary when using preview chart.
                chart?.currentViewport = newViewport
            }
        }
    }
}
