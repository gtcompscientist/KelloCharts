package co.csadev.kellocharts.sample

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.formatter.SimpleAxisValueFormatter
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.LineChartView
import java.lang.Math.random
import java.util.*

class SpeedChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tempo_chart)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment())
                .commit()
        }
    }

    class PlaceholderFragment : Fragment() {

        private var chart: LineChartView? = null
        private var data: LineChartData? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_tempo_chart, container, false)

            chart = rootView.findViewById(R.id.chart)

            generateSpeedData()

            return rootView
        }

        private fun generateSpeedData() {
            // I got speed in range (0-55) and height in meters in range(200 - 300). I want this chart to display both
            // information. Differences between speed and height values are large and chart doesn't look good so I need
            // to modify height values to be in range of speed values.

            val speedRange = 55f
            val minHeight = 200f
            val maxHeight = 300f

            val scale = speedRange / maxHeight
            val sub = minHeight * scale / 2

            val numValues = 52

            var line: Line
            var values: MutableList<PointValue>
            val lines = ArrayList<Line>()

            // Height line, add it as first line to be drawn in the background.
            values = ArrayList()
            for (i in 0 until numValues) {
                // Some random height values, add +200 to make line a little more natural
                val rawHeight = (random() * 100 + 200).toFloat()
                val normalizedHeight = rawHeight * scale - sub
                values.add(PointValue(i.toFloat(), normalizedHeight))
            }

            line = Line(values)
            line.color = Color.GRAY
            line.hasPoints = false
            line.isFilled = true
            line.strokeWidth = 1
            lines.add(line)

            // Speed line
            values = ArrayList()
            for (i in 0 until numValues) {
                // Some random speed values, add +20 to make line a little more natural.
                values.add(PointValue(i.toFloat(), random().toFloat() * 30 + 20))
            }

            line = Line(values)
            line.color = ChartUtils.COLOR_GREEN
            line.hasPoints = false
            line.strokeWidth = 3
            lines.add(line)

            // Data and axes
            val newData = LineChartData(lines)

            // Distance axis(bottom X) with formatter that will ad [km] to values, remember to modify max label charts
            // value.
            val distanceAxis = Axis()
            distanceAxis.name = "Distance"
            distanceAxis.textColor = ChartUtils.COLOR_ORANGE
            distanceAxis.maxLabelChars = 4
            distanceAxis.formatter = SimpleAxisValueFormatter(appendedText = "km".toCharArray())
            distanceAxis.hasLines = true
            distanceAxis.isInside = true
            newData.axisXBottom = distanceAxis

            // Speed axis
            newData.axisYLeft = Axis(
                name = "Speed [km/h]",
                hasLines = true,
                maxLabelChars = 3,
                textColor = ChartUtils.COLOR_RED,
                isInside = true
            )

            // Height axis, this axis need custom formatter that will translate values back to real height values.
            newData.axisYRight = Axis(
                name = "Height [m]",
                maxLabelChars = 3,
                textColor = ChartUtils.COLOR_BLUE,
                formatter = HeightValueFormatter(scale, sub, 0),
                isInside = true
            )

            // Set data
            chart?.lineChartData = newData

            // Important: adjust viewport, you could skip this step but in this case it will looks better with custom
            // viewport. Set
            // viewport with Y range 0-55;
            val v = chart?.maximumViewport.set(0f, 0f, 0f, 0f)
            chart?.maximumViewport = v
            chart?.currentViewport = v
        }

        /**
         * Recalculated height values to display on axis.
         */
        private class HeightValueFormatter(
            private val scale: Float,
            private val sub: Float,
            private val decimalDigits: Int
        ) : SimpleAxisValueFormatter() {

            override fun formatValueForAutoGeneratedAxis(
                formattedValue: CharArray,
                value: Float,
                autoDecimalDigits: Int
            ): Int {
                val scaledValue = (value + sub) / scale
                return super.formatValueForAutoGeneratedAxis(
                    formattedValue,
                    scaledValue,
                    this.decimalDigits
                )
            }
        }
    }
}
