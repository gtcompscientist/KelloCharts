package co.csadev.kellocharts.sample

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import co.csadev.kellocharts.listener.ComboLineColumnChartOnValueSelectListener
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.Column
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.model.ComboLineColumnChartData
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.model.SubcolumnValue
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.ComboLineColumnChartView

class ComboLineColumnChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combo_line_column_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment()).commit()
        }
    }

    /**
     * A fragment containing a combo line/column chart view.
     */
    class PlaceholderFragment : Fragment() {

        private var chart: ComboLineColumnChartView? = null
        private var data: ComboLineColumnChartData? = null

        private var numberOfLines = 1
        private val maxNumberOfLines = 4
        private val numberOfPoints = 12

        internal var randomNumbersTab = Array(maxNumberOfLines) { FloatArray(numberOfPoints) }

        private var hasAxes = true
        private var hasAxesNames = true
        private var hasPoints = true
        private var hasLines = true
        private var isCubic = false
        private var hasLabels = false

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            setHasOptionsMenu(true)
            val rootView = inflater.inflate(R.layout.fragment_combo_line_column_chart, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as ComboLineColumnChartView
            chart!!.onValueTouchListener = ValueTouchListener()

            generateValues()
            generateData()

            return rootView
        }

        // MENU
        override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
            inflater!!.inflate(R.menu.combo_line_column_chart, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            val id = item!!.itemId
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
                chart!!.startDataAnimation()
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun generateValues() {
            for (i in 0 until maxNumberOfLines) {
                for (j in 0 until numberOfPoints) {
                    randomNumbersTab[i][j] = Math.random().toFloat() * 50f + 5
                }
            }
        }

        private fun reset() {
            numberOfLines = 1

            hasAxes = true
            hasAxesNames = true
            hasLines = true
            hasPoints = true
            hasLabels = false
            isCubic = false

        }

        private fun generateData() {
            // Chart looks the best when line data and column data have similar maximum viewports.
            val newData = ComboLineColumnChartData(generateColumnData(), generateLineData())

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

            chart?.comboLineColumnChartData = newData
            data = newData
        }

        private fun generateLineData(): LineChartData {

            val lines = ArrayList<Line>()
            for (i in 0 until numberOfLines) {

                val values = ArrayList<PointValue>()
                for (j in 0 until numberOfPoints) {
                    values.add(PointValue(j.toFloat(), randomNumbersTab[i][j]))
                }

                val line = Line(values)
                line.color = ChartUtils.COLORS[i]
                line.isCubic = isCubic
                line.hasLabels = hasLabels
                line.hasLines = hasLines
                line.hasPoints = hasPoints
                lines.add(line)
            }

            return LineChartData(lines)

        }

        private fun generateColumnData(): ColumnChartData {
            val numSubcolumns = 1
            val numColumns = 12
            // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            for (i in 0 until numColumns) {

                values = ArrayList()
                for (j in 0 until numSubcolumns) {
                    values.add(SubcolumnValue(Math.random().toFloat() * 50 + 5, ChartUtils.COLOR_GREEN))
                }

                columns.add(Column(values))
            }

            return ColumnChartData(columns)
        }

        private fun addLineToData() {
            if (data!!.lineChartData.lines.size >= maxNumberOfLines) {
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
        }

        private fun toggleLabels() {
            hasLabels = !hasLabels

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

        private fun prepareDataAnimation() {

            // Line animations
            for (line in data!!.lineChartData.lines) {
                for (value in line.values) {
                    // Here I modify target only for Y values but it is OK to modify X targets as well.
                    value.setTarget(value.x, Math.random().toFloat() * 50 + 5)
                }
            }

            // Columns animations
            for (column in data!!.columnChartData.columns) {
                for (value in column.values) {
                    value.setTarget(Math.random().toFloat() * 50 + 5)
                }
            }
        }

        private inner class ValueTouchListener : ComboLineColumnChartOnValueSelectListener {

            override fun onValueDeselected() {
                // TODO Auto-generated method stub

            }

            override fun onColumnValueSelected(columnIndex: Int, subcolumnIndex: Int, value: SubcolumnValue) {
                Toast.makeText(activity, "Selected column: " + value, Toast.LENGTH_SHORT).show()
            }

            override fun onPointValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) {
                Toast.makeText(activity, "Selected line point: " + value, Toast.LENGTH_SHORT).show()
            }

        }
    }
}
