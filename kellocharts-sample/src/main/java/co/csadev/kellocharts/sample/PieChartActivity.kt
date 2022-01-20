package co.csadev.kellocharts.sample

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.listener.PieChartOnValueSelectListener
import co.csadev.kellocharts.model.PieChartData
import co.csadev.kellocharts.model.SliceValue
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart
import co.csadev.kellocharts.view.PieChartView
import java.lang.Math.random

class PieChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment())
                .commit()
        }
    }

    /**
     * A fragment containing a pie chart.
     */
    class PlaceholderFragment : Fragment() {

        private var chart: PieChartView? = null
        private var data: PieChartData? = null

        private var hasLabels = false
        private var hasLabelsOutside = false
        private var hasCenterCircle = false
        private var hasCenterText1 = false
        private var hasCenterText2 = false
        private var isExploded = false
        private var hasLabelForSelected = false

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            setHasOptionsMenu(true)
            val rootView = inflater.inflate(R.layout.fragment_pie_chart, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as PieChartView
            chart?.onValueTouchListener = ValueTouchListener()

            generateData()

            return rootView
        }

        // MENU
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.pie_chart, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == R.id.action_reset) {
                reset()
                generateData()
                return true
            }
            if (id == R.id.action_explode) {
                explodeChart()
                return true
            }
            if (id == R.id.action_center_circle) {
                hasCenterCircle = !hasCenterCircle
                if (!hasCenterCircle) {
                    hasCenterText1 = false
                    hasCenterText2 = false
                }

                generateData()
                return true
            }
            if (id == R.id.action_center_text1) {
                hasCenterText1 = !hasCenterText1

                if (hasCenterText1) {
                    hasCenterCircle = true
                }

                hasCenterText2 = false

                generateData()
                return true
            }
            if (id == R.id.action_center_text2) {
                hasCenterText2 = !hasCenterText2

                if (hasCenterText2) {
                    hasCenterText1 = true // text 2 need text 1 to by also drawn.
                    hasCenterCircle = true
                }

                generateData()
                return true
            }
            if (id == R.id.action_toggle_labels) {
                toggleLabels()
                return true
            }
            if (id == R.id.action_toggle_labels_outside) {
                toggleLabelsOutside()
                return true
            }
            if (id == R.id.action_animate) {
                prepareDataAnimation()
                chart!!.startDataAnimation()
                return true
            }
            if (id == R.id.action_toggle_selection_mode) {
                toggleLabelForSelected()
                Toast.makeText(
                    activity,
                    "Selection mode set to " + chart!!.isValueSelectionEnabled + " select any point.",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun reset() {
            chart!!.circleFillRatio = 1.0f
            hasLabels = false
            hasLabelsOutside = false
            hasCenterCircle = false
            hasCenterText1 = false
            hasCenterText2 = false
            isExploded = false
            hasLabelForSelected = false
        }

        private fun generateData() {
            val numValues = 6

            val values = ArrayList<SliceValue>()
            repeat(numValues) {
                val sliceValue =
                    SliceValue(random().toFloat() * 30 + 15, color = ChartUtils.pickColor())
                values.add(sliceValue)
            }

            val newData = PieChartData(values)
            newData.hasLabels = hasLabels
            newData.hasLabelsOnlyForSelected = hasLabelForSelected
            newData.hasLabelsOutside = hasLabelsOutside
            newData.hasCenterCircle = hasCenterCircle

            if (isExploded) {
                newData.sliceSpacing = 24
            }

            if (hasCenterText1) {
                newData.centerText1 = "Hello!"

                // Get roboto-italic font.
                newData.centerText1Typeface =
                    Typeface.createFromAsset(activity?.assets, "Roboto-Italic.ttf")

                // Get font size from dimens.xml and convert it to sp(library uses sp values).
                newData.centerText1FontSize = ChartUtils.px2sp(
                    resources.displayMetrics.scaledDensity,
                    resources.getDimension(R.dimen.pie_chart_text1_size).toInt()
                )
            }

            if (hasCenterText2) {
                newData.centerText2 = "Charts (Roboto Italic)"

                newData.centerText2Typeface =
                    Typeface.createFromAsset(activity?.assets, "Roboto-Italic.ttf")
                newData.centerText2FontSize = ChartUtils.px2sp(
                    resources.displayMetrics.scaledDensity,
                    resources.getDimension(R.dimen.pie_chart_text2_size).toInt()
                )
            }

            chart?.pieChartData = newData
            data = newData
        }

        private fun explodeChart() {
            isExploded = !isExploded
            generateData()
        }

        private fun toggleLabelsOutside() {
            // has labels have to be true:P
            hasLabelsOutside = !hasLabelsOutside
            if (hasLabelsOutside) {
                hasLabels = true
                hasLabelForSelected = false
                chart!!.isValueSelectionEnabled = hasLabelForSelected
            }

            if (hasLabelsOutside) {
                chart!!.circleFillRatio = 0.7f
            } else {
                chart!!.circleFillRatio = 1.0f
            }

            generateData()
        }

        private fun toggleLabels() {
            hasLabels = !hasLabels

            if (hasLabels) {
                hasLabelForSelected = false
                chart!!.isValueSelectionEnabled = hasLabelForSelected

                if (hasLabelsOutside) {
                    chart!!.circleFillRatio = 0.7f
                } else {
                    chart!!.circleFillRatio = 1.0f
                }
            }

            generateData()
        }

        private fun toggleLabelForSelected() {
            hasLabelForSelected = !hasLabelForSelected

            chart!!.isValueSelectionEnabled = hasLabelForSelected

            if (hasLabelForSelected) {
                hasLabels = false
                hasLabelsOutside = false

                if (hasLabelsOutside) {
                    chart!!.circleFillRatio = 0.7f
                } else {
                    chart!!.circleFillRatio = 1.0f
                }
            }

            generateData()
        }

        /**
         * To animate values you have to change targets values and then call [Chart.startDataAnimation]
         * method(don't confuse with View.animate()).
         */
        private fun prepareDataAnimation() {
            for (value in data!!.values) {
                value.setTarget(random().toFloat() * 30 + 15)
            }
        }

        private inner class ValueTouchListener : PieChartOnValueSelectListener {

            override fun onValueSelected(arcIndex: Int, value: SliceValue) {
                Toast.makeText(activity, "Selected: $value", Toast.LENGTH_SHORT).show()
            }

            override fun onValueDeselected() = Unit
        }
    }
}
