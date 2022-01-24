package co.csadev.kellocharts.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
        return if (item.itemId == R.id.action_about) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
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
            adapter = ChartSamplesAdapter(requireContext(), 0, ChartSamples.values())
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
    }

    class ChartSamplesAdapter(
        context: Context,
        resource: Int,
        objects: Array<ChartSamples>
    ) : ArrayAdapter<ChartSamples>(context, resource, objects) {

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
            if (item?.type == null) {
                chartLayout.visibility = View.GONE
            } else {
                val chart = when (item.type) {
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
}
