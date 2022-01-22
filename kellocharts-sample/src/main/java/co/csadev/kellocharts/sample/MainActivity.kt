package co.csadev.kellocharts.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import co.csadev.kellocharts.view.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment())
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_about) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    enum class ChartType {
        LINE_CHART, COLUMN_CHART, PIE_CHART, BUBBLE_CHART, PREVIEW_LINE_CHART, PREVIEW_COLUMN_CHART, OTHER
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment(), OnItemClickListener {

        private lateinit var listView: ListView
        private lateinit var adapter: ChartSamplesAdapter

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            adapter = ChartSamplesAdapter(requireContext(), 0, generateSamplesDescriptions())
            listView = rootView.findViewById<ListView>(android.R.id.list).also {
                it.adapter = adapter
                it.onItemClickListener = this
            }
            return rootView
        }

        override fun onItemClick(adapter: AdapterView<*>, view: View, position: Int, id: Long) {
            val intent: Intent

            when (position) {
                0 -> {
                    // Line Chart;
                    intent = Intent(activity, LineChartActivity::class.java)
                    startActivity(intent)
                }
                1 -> {
                    // Column Chart;
                    intent = Intent(activity, ColumnChartActivity::class.java)
                    startActivity(intent)
                }
                2 -> {
                    // Pie Chart;
                    intent = Intent(activity, PieChartActivity::class.java)
                    startActivity(intent)
                }
                3 -> {
                    // Bubble Chart;
                    intent = Intent(activity, BubbleChartActivity::class.java)
                    startActivity(intent)
                }
                4 -> {
                    // Preview Line Chart;
                    intent = Intent(activity, PreviewLineChartActivity::class.java)
                    startActivity(intent)
                }
                5 -> {
                    // Preview Column Chart;
                    intent = Intent(activity, PreviewColumnChartActivity::class.java)
                    startActivity(intent)
                }
                6 -> {
                    // Combo Chart;
                    intent = Intent(activity, ComboLineColumnChartActivity::class.java)
                    startActivity(intent)
                }
                7 -> {
                    // Line Column Dependency;
                    intent = Intent(activity, LineColumnDependencyActivity::class.java)
                    startActivity(intent)
                }
                8 -> {
                    // Tempo line chart;
                    intent = Intent(activity, TempoChartActivity::class.java)
                    startActivity(intent)
                }
                9 -> {
                    // Speed line chart;
                    intent = Intent(activity, SpeedChartActivity::class.java)
                    startActivity(intent)
                }
                10 -> {
                    // Good Bad filled line chart;
                    intent = Intent(activity, GoodBadChartActivity::class.java)
                    startActivity(intent)
                }
                11 -> {
                    // Good Bad filled line chart;
                    intent = Intent(activity, ViewPagerChartsActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                }
            }
        }

        private fun generateSamplesDescriptions(): List<ChartSampleDescription> {
            val list = mutableListOf<ChartSampleDescription>()

            list.add(ChartSampleDescription("Line Chart", "", ChartType.LINE_CHART))
            list.add(ChartSampleDescription("Column Chart", "", ChartType.COLUMN_CHART))
            list.add(ChartSampleDescription("Pie Chart", "", ChartType.PIE_CHART))
            list.add(ChartSampleDescription("Bubble Chart", "", ChartType.BUBBLE_CHART))
            list.add(
                ChartSampleDescription(
                    "Preview Line Chart",
                    "Control line chart viewport with another line chart.",
                    ChartType.PREVIEW_LINE_CHART
                )
            )
            list.add(
                ChartSampleDescription(
                    "Preview Column Chart",
                    "Control column chart viewport with another column chart.",
                    ChartType.PREVIEW_COLUMN_CHART
                )
            )
            list.add(
                ChartSampleDescription(
                    "Combo Line/Column Chart", "Combo chart with lines and columns.",
                    ChartType.OTHER
                )
            )
            list.add(
                ChartSampleDescription(
                    "Line/Column Chart Dependency",
                    "LineChart responds(with animation) to column chart value selection.",
                    ChartType.OTHER
                )
            )
            list.add(
                ChartSampleDescription(
                    "Tempo Chart",
                    "Presents tempo and height values on a single chart. Example of multiple axes and reverted Y axis"
                            + " with time format [mm:ss].",
                    ChartType.OTHER
                )
            )
            list.add(
                ChartSampleDescription(
                    "Speed Chart",
                    "Presents speed and height values on a single chart. Example of multiple axes inside chart area.",
                    ChartType.OTHER
                )
            )
            list.add(
                ChartSampleDescription(
                    "Good/Bad Chart",
                    "Example of filled area line chart with custom labels", ChartType.OTHER
                )
            )
            list.add(
                ChartSampleDescription(
                    "ViewPager with Charts",
                    "Interactive charts within ViewPager. Each chart can be zoom/scroll except pie chart.",
                    ChartType.OTHER
                )
            )

            return list
        }
    }

    class ChartSamplesAdapter(
        context: Context,
        resource: Int,
        objects: List<ChartSampleDescription>
    ) : ArrayAdapter<ChartSampleDescription>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val chartView = convertView ?: View.inflate(context, R.layout.list_item_sample, null).apply {
                val holder = ViewHolder().also {
                    it.text1 = findViewById(R.id.text1)
                    it.text2 = findViewById(R.id.text2)
                    it.chartLayout = findViewById(R.id.chart_layout)
                }
                tag = holder
            }
            val holder = chartView.tag as ViewHolder
            val item = getItem(position)
            val chartLayout = holder.chartLayout ?: return chartView

            chartLayout.visibility = View.VISIBLE
            chartLayout.removeAllViews()
            if (item?.chartType == null) {
                chartLayout.visibility = View.GONE
            } else {
                val chart = when (item.chartType) {
                    ChartType.LINE_CHART, ChartType.OTHER -> LineChartView(context)
                    ChartType.COLUMN_CHART -> ColumnChartView(context)
                    ChartType.PIE_CHART -> PieChartView(context)
                    ChartType.BUBBLE_CHART -> BubbleChartView(context)
                    ChartType.PREVIEW_LINE_CHART -> PreviewLineChartView(context)
                    ChartType.PREVIEW_COLUMN_CHART -> PreviewColumnChartView(context)
                }
                chartLayout.addView(chart)
                chart.isInteractive = false // Disable touch handling for chart on the ListView.
            }
            holder.text1?.text = item?.text1
            holder.text2?.text = item?.text2

            return chartView
        }

        private inner class ViewHolder {

            var text1: TextView? = null
            var text2: TextView? = null
            var chartLayout: FrameLayout? = null
        }
    }

    class ChartSampleDescription(
        internal var text1: String,
        internal var text2: String,
        internal var chartType: ChartType
    )
}
