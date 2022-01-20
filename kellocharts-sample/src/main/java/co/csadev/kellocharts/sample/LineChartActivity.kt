package co.csadev.kellocharts.sample

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.animation.ChartAnimationListener
import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.listener.LineChartOnValueSelectListener
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart
import co.csadev.kellocharts.view.LineChartView

class LineChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment())
                .commit()
        }
    }

    /**
     * A fragment containing a line chart.
     */
    class PlaceholderFragment : Fragment() {

        private var chart: LineChartView? = null
        private var data: LineChartData? = null
        private var numberOfLines = 1
        private val maxNumberOfLines = 4
        private val numberOfPoints = 12

        internal var randomNumbersTab = Array(maxNumberOfLines) { FloatArray(numberOfPoints) }

        private var hasAxes = true
        private var hasAxesNames = true
        private var hasLines = true
        private var hasPoints = true
        private var shape = ValueShape.CIRCLE
        private var isFilled = false
        private var hasLabels = false
        private var isCubic = false
        private var hasLabelForSelected = false
        private var pointsHaveDifferentColor: Boolean = false

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            setHasOptionsMenu(true)
            val rootView = inflater.inflate(R.layout.fragment_line_chart, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as LineChartView
            chart?.onValueTouchListener = ValueTouchListener()

            // Generate some random values.
            generateValues()

            generateData()

            // Disable viewport recalculations, see toggleCubic() method for more info.
            chart?.isViewportCalculationEnabled = false

            resetViewport()

            return rootView
        }

        // MENU
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.line_chart, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == R.id.action_reset) {
                reset()
                generateData()
                return true
            }
            if (id == R.id.action_add_line) {
                addLineToData()
                return true
            }
            if (id == R.id.action_toggle_lines) {
                toggleLines()
                return true
            }
            if (id == R.id.action_toggle_points) {
                togglePoints()
                return true
            }
            if (id == R.id.action_toggle_cubic) {
                toggleCubic()
                return true
            }
            if (id == R.id.action_toggle_area) {
                toggleFilled()
                return true
            }
            if (id == R.id.action_point_color) {
                togglePointColor()
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
            if (id == R.id.action_shape_diamond) {
                setDiamonds()
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

                Toast.makeText(
                    activity,
                    "Selection mode set to " + chart?.isValueSelectionEnabled + " select any point.",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            if (id == R.id.action_toggle_touch_zoom) {
                chart?.isZoomEnabled = chart?.isZoomEnabled != true
                Toast.makeText(
                    activity,
                    "IsZoomEnabled " + chart?.isZoomEnabled,
                    Toast.LENGTH_SHORT
                ).show()
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

        private fun generateValues() {
            for (i in 0 until maxNumberOfLines) {
                for (j in 0 until numberOfPoints) {
                    randomNumbersTab[i][j] = Math.random().toFloat() * 100f
                }
            }
        }

        private fun reset() {
            numberOfLines = 1

            hasAxes = true
            hasAxesNames = true
            hasLines = true
            hasPoints = true
            shape = ValueShape.CIRCLE
            isFilled = false
            hasLabels = false
            isCubic = false
            hasLabelForSelected = false
            pointsHaveDifferentColor = false

            chart?.isValueSelectionEnabled = hasLabelForSelected
            resetViewport()
        }

        private fun resetViewport() {
            // Reset viewport height range to (0,100)
            val v = chart?.maximumViewport.copy()
            v.bottom = 0f
            v.top = 100f
            v.left = 0f
            v.right = (numberOfPoints - 1).toFloat()
            chart?.maximumViewport = v
            chart?.currentViewport = v
        }

        private fun generateData() {

            val lines = ArrayList<Line>()
            for (i in 0 until numberOfLines) {

                val values = ArrayList<PointValue>()
                for (j in 0 until numberOfPoints) {
                    values.add(PointValue(j.toFloat(), randomNumbersTab[i][j]))
                }

                val line = Line(values)
                line.color = ChartUtils.COLORS[i]
                line.shape = shape
                line.isCubic = isCubic
                line.isFilled = isFilled
                line.hasLabels = hasLabels
                line.hasLabelsOnlyForSelected = hasLabelForSelected
                line.hasLines = hasLines
                line.hasPoints = hasPoints
                if (pointsHaveDifferentColor) {
                    line.pointColor = ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.size]
                }
                lines.add(line)
            }

            val newData = LineChartData(lines)

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

            newData.baseValue = java.lang.Float.NEGATIVE_INFINITY
            chart?.lineChartData = newData
            data = newData
        }

        /**
         * Adds lines to data, after that data should be set again with
         * [LineChartView.setLineChartData]. Last 4th line has non-monotonically x values.
         */
        private fun addLineToData() {
            if (data!!.lines.size >= maxNumberOfLines) {
                Toast.makeText(activity, "Samples app uses max 4 lines!", Toast.LENGTH_SHORT).show()
                return
            } else {
                ++numberOfLines
            }

            generateData()
        }

        private fun toggleLines() {
            hasLines = !hasLines

            generateData()
        }

        private fun togglePoints() {
            hasPoints = !hasPoints

            generateData()
        }

        private fun toggleCubic() {
            isCubic = !isCubic

            generateData()

            if (isCubic) {
                // It is good idea to manually set a little higher max viewport for cubic lines because sometimes line
                // go above or below max/min. To do that use Viewport.inest() method and pass negative value as dy
                // parameter or just set top and bottom values manually.
                // In this example I know that Y values are within (0,100) range so I set viewport height range manually
                // to (-5, 105).
                // To make this works during animations you should use Chart.setViewportCalculationEnabled(false) before
                // modifying viewport.
                // Remember to set viewport after you call setLineChartData().
                val v = chart?.maximumViewport.copy()
                v.bottom = -5f
                v.top = 105f
                // You have to set max and current viewports separately.
                chart?.maximumViewport = v
                // I changing current viewport with animation in this case.
                chart?.setCurrentViewportWithAnimation(v)
            } else {
                // If not cubic restore viewport to (0,100) range.
                val v = chart?.maximumViewport.copy()
                v.bottom = 0f
                v.top = 100f

                // You have to set max and current viewports separately.
                // In this case, if I want animation I have to set current viewport first and use animation listener.
                // Max viewport will be set in onAnimationFinished method.
                chart?.setViewportAnimationListener(object : ChartAnimationListener {

                    override fun onAnimationStarted() {
                        // TODO Auto-generated method stub
                    }

                    override fun onAnimationFinished() {
                        // Set max viewpirt and remove listener.
                        chart?.maximumViewport = v
                    }
                })
                // Set current viewport with animation;
                chart?.setCurrentViewportWithAnimation(v)
            }
        }

        private fun toggleFilled() {
            isFilled = !isFilled

            generateData()
        }

        private fun togglePointColor() {
            pointsHaveDifferentColor = !pointsHaveDifferentColor

            generateData()
        }

        private fun setCircles() {
            shape = ValueShape.CIRCLE

            generateData()
        }

        private fun setSquares() {
            shape = ValueShape.SQUARE

            generateData()
        }

        private fun setDiamonds() {
            shape = ValueShape.DIAMOND

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
         * method(don't confuse with View.animate()). If you operate on data that was set before you don't have to call
         * [LineChartView.lineChartData] again.
         */
        private fun prepareDataAnimation() {
            for (line in data!!.lines) {
                for (value in line.values) {
                    // Here I modify target only for Y values but it is OK to modify X targets as well.
                    value.setTarget(value.x, Math.random().toFloat() * 100)
                }
            }
        }

        private inner class ValueTouchListener : LineChartOnValueSelectListener {

            override fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) {
                Toast.makeText(activity, "Selected: " + value, Toast.LENGTH_SHORT).show()
            }

            override fun onValueDeselected() = Unit
        }
    }
}
