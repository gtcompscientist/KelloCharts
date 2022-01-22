package co.csadev.kellocharts.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.listener.ColumnChartOnValueSelectListener
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.Column
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.model.SubcolumnValue
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart
import co.csadev.kellocharts.view.ColumnChartView

class ColumnChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_column_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment())
                .commit()
        }
    }

    /**
     * A fragment containing a column chart.
     */
    class PlaceholderFragment : Fragment() {

        private var chart: ColumnChartView? = null
        private var data: ColumnChartData? = null
        private var hasAxes = true
        private var hasAxesNames = true
        private var hasLabels = false
        private var hasLabelForSelected = false
        private var horizontalData = false
        private var dataType = DEFAULT_DATA

        private val sign: Int
            get() {
                val sign = intArrayOf(-1, 1)
                return sign[Math.round(Math.random().toFloat())]
            }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            setHasOptionsMenu(true)
            val rootView = inflater.inflate(R.layout.fragment_column_chart, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as ColumnChartView
            chart?.onValueTouchListener = ValueTouchListener()

            generateData()

            return rootView
        }

        // MENU
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.column_chart, menu)
        }

        @Suppress("ComplexMethod")
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_reset -> {
                    reset()
                    generateData()
                }
                R.id.action_subcolumns -> {
                    dataType = SUBCOLUMNS_DATA
                    generateData()
                }
                R.id.action_stacked -> {
                    dataType = STACKED_DATA
                    generateData()
                }
                R.id.action_negative_subcolumns -> {
                    dataType = NEGATIVE_SUBCOLUMNS_DATA
                    generateData()
                }
                R.id.action_negative_stacked -> {
                    dataType = NEGATIVE_STACKED_DATA
                    generateData()
                }
                R.id.action_toggle_labels -> toggleLabels()
                R.id.action_toggle_axes -> toggleAxes()
                R.id.action_toggle_axes_names -> toggleAxesNames()
                R.id.action_toggle_horizontal_data -> toggleHorizontal()
                R.id.action_animate -> {
                    prepareDataAnimation()
                    chart?.startDataAnimation()
                }
                R.id.action_toggle_selection_mode -> {
                    toggleLabelForSelected()

                    Toast.makeText(
                        activity,
                        "Selection mode set to " + chart?.isValueSelectionEnabled + " select any point.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.id.action_toggle_touch_zoom -> {
                    chart?.isZoomEnabled = chart?.isZoomEnabled != true
                    Toast.makeText(
                        activity,
                        "IsZoomEnabled " + chart?.isZoomEnabled,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.id.action_zoom_both -> chart?.zoomType = ZoomType.HORIZONTAL_AND_VERTICAL
                R.id.action_zoom_horizontal -> chart?.zoomType = ZoomType.HORIZONTAL
                R.id.action_zoom_vertical -> chart?.zoomType = ZoomType.VERTICAL
                else -> return super.onOptionsItemSelected(item)
            }
            return true
        }

        private fun reset() {
            hasAxes = true
            hasAxesNames = true
            hasLabels = false
            hasLabelForSelected = false
            dataType = DEFAULT_DATA
            chart?.isValueSelectionEnabled = hasLabelForSelected
        }

        private fun generateDefaultData() {
            val numSubcolumns = 1
            val numColumns = 8
            // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            repeat(numColumns) {

                values = ArrayList()
                repeat(numSubcolumns) {
                    values.add(
                        SubcolumnValue(
                            Math.random().toFloat() * SAMPLES_F + 5,
                            ChartUtils.pickColor()
                        )
                    )
                }

                val column = Column(values)
                column.hasLabels = hasLabels
                column.hasLabelsOnlyForSelected = hasLabelForSelected
                columns.add(column)
            }

            val newData = ColumnChartData(columns, isHorizontal = horizontalData)
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

            chart?.columnChartData = newData
        }

        /**
         * Generates columns with subcolumns, columns have larger separation than subcolumns.
         */
        private fun generateSubcolumnsData() {
            val numSubcolumns = CHART_AXES
            val numColumns = CHART_AXES
            // Column can have many subcolumns, here I use 4 subcolumn in each of 8 columns.
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            repeat(numColumns) {

                values = ArrayList()
                repeat(numSubcolumns) {
                    values.add(
                        SubcolumnValue(
                            Math.random().toFloat() * SAMPLES_F + 5,
                            ChartUtils.pickColor()
                        )
                    )
                }

                val column = Column(values)
                column.hasLabels = hasLabels
                column.hasLabelsOnlyForSelected = hasLabelForSelected
                columns.add(column)
            }

            val newData = ColumnChartData(columns)
            newData.isHorizontal = horizontalData

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

            chart?.columnChartData = newData
            data = newData
        }

        /**
         * Generates columns with stacked subcolumns.
         */
        private fun generateStackedData() {
            val numSubcolumns = CHART_AXES
            val numColumns = 8
            // Column can have many stacked subcolumns, here I use 4 stacke subcolumn in each of 4 columns.
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            repeat(numColumns) {

                values = ArrayList()
                repeat(numSubcolumns) {
                    values.add(
                        SubcolumnValue(
                            Math.random().toFloat() * 20f + 5,
                            ChartUtils.pickColor()
                        )
                    )
                }

                val column = Column(values)
                column.hasLabels = hasLabels
                column.hasLabelsOnlyForSelected = hasLabelForSelected
                columns.add(column)
            }

            val newData = ColumnChartData(columns)
            newData.isHorizontal = horizontalData

            // Set stacked flag.
            newData.isStacked = true

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

            chart?.columnChartData = newData
            data = newData
        }

        private fun generateNegativeSubcolumnsData() {

            val numSubcolumns = CHART_AXES
            val numColumns = CHART_AXES
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            repeat(numColumns) {

                values = ArrayList()
                repeat(numSubcolumns) {
                    val sign = sign
                    values.add(
                        SubcolumnValue(
                            Math.random().toFloat() * SAMPLES_F * sign.toFloat() + 5 * sign,
                            ChartUtils.pickColor()
                        )
                    )
                }

                val column = Column(values)
                column.hasLabels = hasLabels
                column.hasLabelsOnlyForSelected = hasLabelForSelected
                columns.add(column)
            }

            val newData = ColumnChartData(columns)
            newData.isHorizontal = horizontalData

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

            chart?.columnChartData = newData
            data = newData
        }

        private fun generateNegativeStackedData() {

            val numSubcolumns = CHART_AXES
            val numColumns = 8
            // Column can have many stacked subcolumns, here I use 4 stacke subcolumn in each of 4 columns.
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            repeat(numColumns) {

                values = ArrayList()
                repeat(numSubcolumns) {
                    val sign = sign
                    values.add(
                        SubcolumnValue(
                            Math.random().toFloat() * 20f * sign.toFloat() + 5 * sign,
                            ChartUtils.pickColor()
                        )
                    )
                }

                val column = Column(values)
                column.hasLabels = hasLabels
                column.hasLabelsOnlyForSelected = hasLabelForSelected
                columns.add(column)
            }

            val newData = ColumnChartData(columns)
            newData.isHorizontal = horizontalData

            // Set stacked flag.
            newData.isStacked = true

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

            chart?.columnChartData = newData
            data = newData
        }

        private fun generateData() {
            when (dataType) {
                DEFAULT_DATA -> generateDefaultData()
                SUBCOLUMNS_DATA -> generateSubcolumnsData()
                STACKED_DATA -> generateStackedData()
                NEGATIVE_SUBCOLUMNS_DATA -> generateNegativeSubcolumnsData()
                NEGATIVE_STACKED_DATA -> generateNegativeStackedData()
                else -> generateDefaultData()
            }
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

        private fun toggleHorizontal() {
            horizontalData = !horizontalData

            generateData()
        }

        /**
         * To animate values you have to change targets values and then call [Chart.startDataAnimation]
         * method(don't confuse with View.animate()).
         */
        private fun prepareDataAnimation() {
            data?.columns?.forEach { column ->
                column.values.forEach { value ->
                    value.setTarget(Math.random().toFloat() * 100)
                }
            }
        }

        private inner class ValueTouchListener : ColumnChartOnValueSelectListener {

            override fun onValueSelected(
                columnIndex: Int,
                subcolumnIndex: Int,
                value: SubcolumnValue
            ) {
                Toast.makeText(activity, "Selected: " + value, Toast.LENGTH_SHORT).show()
            }

            override fun onValueDeselected() = Unit
        }

        companion object {

            private const val DEFAULT_DATA = 0
            private const val SUBCOLUMNS_DATA = 1
            private const val STACKED_DATA = 2
            private const val NEGATIVE_SUBCOLUMNS_DATA = 3
            private const val NEGATIVE_STACKED_DATA = 4
        }
    }
}
