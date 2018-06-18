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
import android.widget.Toast

import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.listener.BubbleChartOnValueSelectListener
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.BubbleChartData
import co.csadev.kellocharts.model.BubbleValue
import co.csadev.kellocharts.model.ValueShape
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.BubbleChartView
import co.csadev.kellocharts.view.Chart

class BubbleChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubble_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment()).commit()
        }
    }

    /**
     * A fragment containing a bubble chart.
     */
    class PlaceholderFragment : Fragment() {

        private var chart: BubbleChartView? = null
        private var data: BubbleChartData? = null
        private var hasAxes = true
        private var hasAxesNames = true
        private var shape = ValueShape.CIRCLE
        private var hasLabels = false
        private var hasLabelForSelected = false

        private val sign: Int
            get() {
                val sign = intArrayOf(-1, 1)
                return sign[Math.round(Math.random().toFloat())]
            }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            setHasOptionsMenu(true)
            val rootView = inflater.inflate(R.layout.fragment_bubble_chart, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as BubbleChartView
            chart?.onValueTouchListener = ValueTouchListener()

            generateData()

            return rootView
        }

        override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
            inflater!!.inflate(R.menu.bubble_chart, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            val id = item!!.itemId
            if (id == R.id.action_reset) {
                reset()
                generateData()
                return true
            }
            if (id == R.id.action_shape_circles) {
                setCircles()
                return true
            }
            if (id == R.id.action_shape_square) {
                setSquares()
                return true
            }
            if (id == R.id.action_toggle_labels) {
                toggleLabels()
                return true
            }
            if (id == R.id.action_toggle_axes) {
                toggleAxes()
                return true
            }
            if (id == R.id.action_toggle_axes_names) {
                toggleAxesNames()
                return true
            }
            if (id == R.id.action_animate) {
                prepareDataAnimation()
                chart?.startDataAnimation()
                return true
            }
            if (id == R.id.action_toggle_selection_mode) {
                toggleLabelForSelected()
                Toast.makeText(activity,
                        "Selection mode set to " + chart?.isValueSelectionEnabled + " select any point.",
                        Toast.LENGTH_SHORT).show()
                return true
            }
            if (id == R.id.action_toggle_touch_zoom) {
                chart?.isZoomEnabled = chart?.isZoomEnabled != true
                Toast.makeText(activity, "IsZoomEnabled " + chart?.isZoomEnabled, Toast.LENGTH_SHORT).show()
                return true
            }
            if (id == R.id.action_zoom_both) {
                chart?.zoomType = ZoomType.HORIZONTAL_AND_VERTICAL
                return true
            }
            if (id == R.id.action_zoom_horizontal) {
                chart?.zoomType = ZoomType.HORIZONTAL
                return true
            }
            if (id == R.id.action_zoom_vertical) {
                chart?.zoomType = ZoomType.VERTICAL
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun reset() {
            hasAxes = true
            hasAxesNames = true
            shape = ValueShape.CIRCLE
            hasLabels = false
            hasLabelForSelected = false

            chart?.isValueSelectionEnabled = hasLabelForSelected
        }

        private fun generateData() {

            val values = ArrayList<BubbleValue>()
            for (i in 0 until BUBBLES_NUM) {
                val value = BubbleValue(i.toFloat(), Math.random().toFloat() * 100, Math.random().toFloat() * 1000)
                value.color = ChartUtils.pickColor()
                value.shape = shape
                values.add(value)
            }

            val newData = BubbleChartData(values = values)
            newData.hasLabels = hasLabels
            newData.setHasLabelsOnlyForSelected(hasLabelForSelected)

            if (hasAxes) {
                val axisX = Axis(hasLines = true)
                val axisY = Axis(hasLines = true)
                if (hasAxesNames) {
                    axisX.name = "Axis X"
                    axisY.name = "Axis Y"
                }
                newData.axisXBottom = axisX
                newData.axisYLeft = axisY
            } else {
                newData.axisXBottom = null
                newData.axisYLeft = null
            }

            chart?.bubbleChartData = newData
            data = newData
        }

        private fun setCircles() {
            shape = ValueShape.CIRCLE
            generateData()
        }

        private fun setSquares() {
            shape = ValueShape.SQUARE
            generateData()
        }

        private fun toggleLabels() {
            hasLabels = !hasLabels

            if (hasLabels) {
                hasLabelForSelected = false
                chart?.isValueSelectionEnabled = hasLabelForSelected
            }

            generateData()
        }

        private fun toggleLabelForSelected() {
            hasLabelForSelected = !hasLabelForSelected

            chart?.isValueSelectionEnabled = hasLabelForSelected

            if (hasLabelForSelected) {
                hasLabels = false
            }

            generateData()
        }

        private fun toggleAxes() {
            hasAxes = !hasAxes

            generateData()
        }

        private fun toggleAxesNames() {
            hasAxesNames = !hasAxesNames

            generateData()
        }

        /**
         * To animate values you have to change targets values and then call [Chart.startDataAnimation]
         * method(don't confuse with View.animate()).
         */
        private fun prepareDataAnimation() {
            data?.values?.forEach {
                it.setTarget(it.x + Math.random().toFloat() * 4f * sign.toFloat(), Math.random().toFloat() * 100,
                        Math.random().toFloat() * 1000)
            }
        }

        private inner class ValueTouchListener : BubbleChartOnValueSelectListener {

            override fun onValueSelected(bubbleIndex: Int, value: BubbleValue) {
                Toast.makeText(activity, "Selected: " + value, Toast.LENGTH_SHORT).show()
            }

            override fun onValueDeselected() {
                // TODO Auto-generated method stub

            }
        }

        companion object {

            private val BUBBLES_NUM = 8
        }
    }
}
