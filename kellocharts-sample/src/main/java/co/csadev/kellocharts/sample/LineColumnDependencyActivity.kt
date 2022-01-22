package co.csadev.kellocharts.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.listener.ColumnChartOnValueSelectListener
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.AxisValue
import co.csadev.kellocharts.model.Column
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.model.SubcolumnValue
import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.ColumnChartView
import co.csadev.kellocharts.view.LineChartView

class LineColumnDependencyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_column_dependency)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment())
                .commit()
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        private var chartTop: LineChartView? = null
        private var chartBottom: ColumnChartView? = null

        private var lineData: LineChartData? = null
        private var columnData: ColumnChartData? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView =
                inflater.inflate(R.layout.fragment_line_column_dependency, container, false)

            // *** TOP LINE CHART ***
            chartTop = rootView.findViewById<View>(R.id.chart_top) as LineChartView

            // Generate and set data for line chart
            generateInitialLineData()

            // *** BOTTOM COLUMN CHART ***

            chartBottom = rootView.findViewById<View>(R.id.chart_bottom) as ColumnChartView

            generateColumnData()

            return rootView
        }

        private fun generateColumnData() {

            val numSubcolumns = 1
            val numColumns = months.size

            val axisValues = ArrayList<AxisValue>()
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

                axisValues.add(AxisValue(it.toFloat(), label = months[it].toCharArray()))

                columns.add(Column(values, hasLabelsOnlyForSelected = true))
            }

            val colData = ColumnChartData(columns)

            colData.axisXBottom = Axis(axisValues, hasLines = true)
            colData.axisYLeft = Axis(hasLines = true, maxLabelChars = 2)

            chartBottom?.columnChartData = colData

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom?.onValueTouchListener = ValueTouchListener()

            // Set selection mode to keep selected month column highlighted.
            chartBottom?.isValueSelectionEnabled = true

            chartBottom?.zoomType = ZoomType.HORIZONTAL

            columnData = colData

            // chartBottom.setOnClickListener(new View.OnClickListener() {
            //
            // @Override
            // public void onClick(View v) {
            // SelectedValue sv = chartBottom.getSelectedValue();
            // if (!sv.isSet()) {
            // generateInitialLineData();
            // }
            //
            // }
            // });
        }

        /**
         * Generates initial data for line chart. At the beginning all Y values are equals 0. That will change when user
         * will select value on column chart.
         */
        private fun generateInitialLineData() {
            val numValues = 7

            val axisValues = ArrayList<AxisValue>()
            val values = ArrayList<PointValue>()
            repeat(numValues) {
                values.add(PointValue(it, 0))
                axisValues.add(AxisValue(it, label = days[it]))
            }

            val line = Line(values, color = ChartUtils.COLOR_GREEN, isCubic = true)

            val lines = ArrayList<Line>()
            lines.add(line)

            val data = LineChartData(lines)
            data.axisXBottom = Axis(axisValues, hasLines = true)
            data.axisYLeft = Axis(hasLines = true, maxLabelChars = 3)

            chartTop?.lineChartData = data

            // For build-up animation you have to disable viewport recalculation.
            chartTop?.isViewportCalculationEnabled = false

            // And set initial max viewport and current viewport- remember to set viewports after data.
            val v = Viewport(0f, 110f, 6f, 0f)
            chartTop?.maximumViewport = v
            chartTop?.currentViewport = v

            chartTop?.zoomType = ZoomType.HORIZONTAL
            this.lineData = data
        }

        private fun generateLineData(color: Int, range: Float) {
            // Cancel last animation if not finished.
            chartTop!!.cancelDataAnimation()

            // Modify data targets
            val line = lineData!!.lines[0] // For this example there is always only one line.
            line.color = color
            for (value in line.values) {
                // Change target only for Y value.
                value.setTarget(value.x, Math.random().toFloat() * range)
            }

            // Start new data animation with 300ms duration;
            chartTop!!.startDataAnimation(300)
        }

        private inner class ValueTouchListener : ColumnChartOnValueSelectListener {

            override fun onValueSelected(
                columnIndex: Int,
                subcolumnIndex: Int,
                value: SubcolumnValue
            ) {
                generateLineData(value.color, 100f)
            }

            override fun onValueDeselected() {

                generateLineData(ChartUtils.COLOR_GREEN, 0f)
            }
        }

        companion object {
            val months = arrayOf(
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec"
            )

            val days = arrayOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun")
        }
    }
}
