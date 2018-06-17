package co.csadev.kellocharts.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView

import java.util.ArrayList

import co.csadev.kellocharts.view.AbstractChartView
import co.csadev.kellocharts.view.BubbleChartView
import co.csadev.kellocharts.view.ColumnChartView
import co.csadev.kellocharts.view.LineChartView
import co.csadev.kellocharts.view.PieChartView
import co.csadev.kellocharts.view.PreviewColumnChartView
import co.csadev.kellocharts.view.PreviewLineChartView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment()).commit()
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
    class PlaceholderFragment : androidx.fragment.app.Fragment(), OnItemClickListener {

        private var listView: ListView? = null
        private var adapter: ChartSamplesAdapter? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            listView = rootView.findViewById<View>(android.R.id.list) as ListView
            adapter = ChartSamplesAdapter(context!!, 0, generateSamplesDescriptions())
            listView!!.adapter = adapter
            listView!!.onItemClickListener = this
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
            val list = ArrayList<MainActivity.ChartSampleDescription>()

            list.add(ChartSampleDescription("Line Chart", "", ChartType.LINE_CHART))
            list.add(ChartSampleDescription("Column Chart", "", ChartType.COLUMN_CHART))
            list.add(ChartSampleDescription("Pie Chart", "", ChartType.PIE_CHART))
            list.add(ChartSampleDescription("Bubble Chart", "", ChartType.BUBBLE_CHART))
            list.add(ChartSampleDescription("Preview Line Chart",
                    "Control line chart viewport with another line chart.", ChartType.PREVIEW_LINE_CHART))
            list.add(ChartSampleDescription("Preview Column Chart",
                    "Control column chart viewport with another column chart.", ChartType.PREVIEW_COLUMN_CHART))
            list.add(ChartSampleDescription("Combo Line/Column Chart", "Combo chart with lines and columns.",
                    ChartType.OTHER))
            list.add(ChartSampleDescription("Line/Column Chart Dependency",
                    "LineChart responds(with animation) to column chart value selection.", ChartType.OTHER))
            list.add(ChartSampleDescription(
                    "Tempo Chart",
                    "Presents tempo and height values on a signle chart. Example of multiple axes and reverted Y axis" + " with time format [mm:ss].",
                    ChartType.OTHER))
            list.add(ChartSampleDescription("Speed Chart",
                    "Presents speed and height values on a single chart. Example of multiple axes inside chart area.",
                    ChartType.OTHER))
            list.add(ChartSampleDescription("Good/Bad Chart",
                    "Example of filled area line chart with custom labels", ChartType.OTHER))
            list.add(ChartSampleDescription("ViewPager with Charts",
                    "Interactive charts within ViewPager. Each chart can be zoom/scroll except pie chart.",
                    ChartType.OTHER))

            return list
        }
    }

    class ChartSamplesAdapter(context: Context, resource: Int, objects: List<ChartSampleDescription>) : ArrayAdapter<ChartSampleDescription>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ViewHolder

            if (convertView == null) {
                convertView = View.inflate(context, R.layout.list_item_sample, null)

                holder = ViewHolder()
                holder.text1 = convertView!!.findViewById<View>(R.id.text1) as TextView
                holder.text2 = convertView.findViewById<View>(R.id.text2) as TextView
                holder.chartLayout = convertView.findViewById<View>(R.id.chart_layout) as FrameLayout

                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }

            val item = getItem(position)

            holder.chartLayout!!.visibility = View.VISIBLE
            holder.chartLayout!!.removeAllViews()
            val chart: AbstractChartView?
            when (item!!.chartType) {
                MainActivity.ChartType.LINE_CHART -> {
                    chart = LineChartView(context)
                    holder.chartLayout!!.addView(chart)
                }
                MainActivity.ChartType.COLUMN_CHART -> {
                    chart = ColumnChartView(context)
                    holder.chartLayout!!.addView(chart)
                }
                MainActivity.ChartType.PIE_CHART -> {
                    chart = PieChartView(context)
                    holder.chartLayout!!.addView(chart)
                }
                MainActivity.ChartType.BUBBLE_CHART -> {
                    chart = BubbleChartView(context)
                    holder.chartLayout!!.addView(chart)
                }
                MainActivity.ChartType.PREVIEW_LINE_CHART -> {
                    chart = PreviewLineChartView(context)
                    holder.chartLayout!!.addView(chart)
                }
                MainActivity.ChartType.PREVIEW_COLUMN_CHART -> {
                    chart = PreviewColumnChartView(context)
                    holder.chartLayout!!.addView(chart)
                }
                else -> {
                    chart = null
                    holder.chartLayout!!.visibility = View.GONE
                }
            }

            if (null != chart) {
                chart.isInteractive = false// Disable touch handling for chart on the ListView.
            }
            holder.text1!!.text = item.text1
            holder.text2!!.text = item.text2

            return convertView
        }

        private inner class ViewHolder {

            internal var text1: TextView? = null
            internal var text2: TextView? = null
            internal var chartLayout: FrameLayout? = null
        }

    }

    class ChartSampleDescription(internal var text1: String, internal var text2: String, internal var chartType: ChartType)

}
