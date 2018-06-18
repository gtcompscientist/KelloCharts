package co.csadev.kellocharts.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.LineChartView

class GoodBadChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_good_bad)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment()).commit()
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        private var chart: LineChartView? = null
        private var data: LineChartData? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_good_bad, container, false)

            chart = rootView.findViewById<View>(R.id.chart) as LineChartView

            generateDefaultData()
            chart?.lineChartData = data!!

            // Increase viewport height for better look
            val v = chart!!.maximumViewport
            val dy = v.height() * 0.2f
            v.inset(0f, -dy)
            chart!!.maximumViewport = v
            chart!!.currentViewport = v

            return rootView
        }

        private fun generateDefaultData() {

            // Generate data, every line has 3 points to form filled triangle. Point radius is set to 1 to be almost
            // invisible but it has to be there because without points there is not labels. Area transparency is set to
            // 255(full opacity).

            // Important note. This example uses negative values, to properly fill area below 0 chart base value have to
            // be set to 0. That is default base value but if you want to be sure you can call data.setBaseValue(0)
            // method.

            var line: Line
            var values: MutableList<PointValue>
            val lines = ArrayList<Line>()

            // First good triangle
            values = ArrayList()
            values.add(PointValue(0, 0, "".toCharArray()))
            values.add(PointValue(1, 1, "Very Good:)".toCharArray()))
            values.add(PointValue(2, 0, "".toCharArray()))

            line = Line(values)
            line.color = ChartUtils.COLOR_GREEN
            line.areaTransparency = 255
            line.isFilled = true
            line.pointRadius = 1
            line.hasLabels = true
            lines.add(line)

            // Second good triangle
            values = ArrayList()
            values.add(PointValue(3, 0, "".toCharArray()))
            values.add(PointValue(4f, 0.5f, "Good Enough".toCharArray()))
            values.add(PointValue(5, 0, "".toCharArray()))

            line = Line(values)
            line.color = ChartUtils.COLOR_GREEN
            line.areaTransparency = 255
            line.isFilled = true
            line.pointRadius = 1
            line.hasLabels = true
            lines.add(line)

            // Bad triangle
            values = ArrayList()
            values.add(PointValue(1, 0, "".toCharArray()))
            values.add(PointValue(2, -1, "Very Bad".toCharArray()))
            values.add(PointValue(3, 0, "".toCharArray()))

            line = Line(values)
            line.color = ChartUtils.COLOR_RED
            line.areaTransparency = 255
            line.isFilled = true
            line.pointRadius = 1
            line.hasLabels = true
            lines.add(line)

            data = LineChartData(lines)

            // *** Important, set base value to 0 to fill negative part of chart.
            // data.setBaseValue(0);

        }
    }
}
