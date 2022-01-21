package co.csadev.kellocharts.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import co.csadev.kellocharts.gesture.ContainerScrollType
import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.model.*
import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.*

class ViewPagerChartsActivity : AppCompatActivity(), ActionBar.TabListener {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every loaded fragment in memory. If this becomes too
     * memory intensive, it may be best to switch to a [android.support.v4.app.FragmentStatePagerAdapter].
     */
    internal var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    internal var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_charts)

        // Set up the action bar.
        supportActionBar?.navigationMode = ActionBar.NAVIGATION_MODE_TABS

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.pager) as? ViewPager
        mViewPager?.adapter = mSectionsPagerAdapter

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager?.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                supportActionBar?.setSelectedNavigationItem(position)
            }
        })

        // For each of the sections in the app, add a tab to the action bar.
        for (i in 0 until (mSectionsPagerAdapter?.count ?: 0)) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            supportActionBar?.addTab(
                supportActionBar?.newTab()?.setText(mSectionsPagerAdapter?.getPageTitle(i))
                    ?.setTabListener(this as ActionBar.TabListener)
            )
        }
    }

    override fun onTabSelected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager?.currentItem = tab.position
    }

    override fun onTabUnselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) = Unit

    override fun onTabReselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) = Unit

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_view_pager_charts, container, false)
            val layout = rootView as RelativeLayout
            val sectionNum = arguments!!.getInt(ARG_SECTION_NUMBER)
            when (sectionNum) {
                1 -> {
                    val lineChartView = LineChartView(activity!!)
                    lineChartView.lineChartData = generateLineChartData()
                    lineChartView.zoomType = ZoomType.HORIZONTAL

                    /** Note: Chart is within ViewPager so enable container scroll mode.  */
                    lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)

                    layout.addView(lineChartView)
                }
                2 -> {
                    val columnChartView = ColumnChartView(activity!!)
                    columnChartView.columnChartData = generateColumnChartData()
                    columnChartView.zoomType = ZoomType.HORIZONTAL

                    /** Note: Chart is within ViewPager so enable container scroll mode.  */
                    columnChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)

                    layout.addView(columnChartView)
                }
                3 -> {
                    val bubbleChartView = BubbleChartView(activity!!)
                    bubbleChartView.bubbleChartData = generateBubbleChartData()
                    bubbleChartView.zoomType = ZoomType.HORIZONTAL_AND_VERTICAL

                    /** Note: Chart is within ViewPager so enable container scroll mode.  */
                    bubbleChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)

                    layout.addView(bubbleChartView)
                }
                4 -> {
                    val previewLineChartView = PreviewLineChartView(activity!!)
                    previewLineChartView.lineChartData = generatePreviewLineChartData()

                    /** Note: Chart is within ViewPager so enable container scroll mode.  */
                    previewLineChartView.setContainerScrollEnabled(
                        true,
                        ContainerScrollType.HORIZONTAL
                    )

                    val tempViewport = previewLineChartView.maximumViewport.copy()
                    val dx = tempViewport.width() / 6
                    tempViewport.inset(dx, 0f)
                    previewLineChartView.currentViewport = tempViewport
                    previewLineChartView.zoomType = ZoomType.HORIZONTAL

                    layout.addView(previewLineChartView)
                }
                5 -> {
                    val pieChartView = PieChartView(activity!!)
                    pieChartView.pieChartData = generatePieChartData()

                    /** Note: Chart is within ViewPager so enable container scroll mode.  */
                    pieChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)

                    layout.addView(pieChartView)
                }
            }

            return rootView
        }

        private fun generateLineChartData(): LineChartData {
            val numValues = 20

            val values = ArrayList<PointValue>()
            repeat(numValues) {
                values.add(PointValue(it.toFloat(), Math.random().toFloat() * 100f))
            }

            val line = Line(values)
            line.color = ChartUtils.COLOR_GREEN

            val lines = ArrayList<Line>()
            lines.add(line)

            val data = LineChartData(lines)
            data.axisXBottom = Axis(name = "Axis X")
            data.axisYLeft = Axis(name = "Axis Y", hasLines = true)
            return data
        }

        private fun generateColumnChartData(): ColumnChartData {
            val numSubcolumns = 1
            val numColumns = 12
            // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
            val columns = ArrayList<Column>()
            var values: MutableList<SubcolumnValue>
            repeat(numColumns) {

                values = ArrayList()
                repeat(numSubcolumns) {
                    values.add(
                        SubcolumnValue(
                            Math.random().toFloat() * 50f + 5,
                            ChartUtils.pickColor()
                        )
                    )
                }

                columns.add(Column(values))
            }

            val data = ColumnChartData(columns)

            data.axisXBottom = Axis(name = "Axis X")
            data.axisYLeft = Axis(name = "Axis Y", hasLines = true)
            return data
        }

        private fun generateBubbleChartData(): BubbleChartData {
            val numBubbles = 10

            val values = ArrayList<BubbleValue>()
            for (i in 0 until numBubbles) {
                val value = BubbleValue(
                    i.toFloat(),
                    Math.random().toFloat() * 100,
                    Math.random().toFloat() * 1000
                )
                value.color = ChartUtils.pickColor()
                values.add(value)
            }

            val data = BubbleChartData(values)

            data.axisXBottom = Axis(name = "Axis X")
            data.axisYLeft = Axis(name = "Axis Y", hasLines = true)
            return data
        }

        private fun generatePreviewLineChartData(): LineChartData {
            val numValues = 50

            val values = ArrayList<PointValue>()
            repeat(numValues) {
                values.add(PointValue(it.toFloat(), Math.random().toFloat() * 100f))
            }

            val line = Line(values)
            line.color = ChartUtils.DEFAULT_DARKEN_COLOR
            line.hasPoints = false // too many values so don't draw points.

            val lines = ArrayList<Line>()
            lines.add(line)

            val data = LineChartData(lines)
            data.axisXBottom = Axis()
            data.axisYLeft = Axis(hasLines = true)

            return data
        }

        private fun generatePieChartData(): PieChartData {
            val numValues = 6

            val values = ArrayList<SliceValue>()
            repeat(numValues) {
                values.add(
                    SliceValue(
                        Math.random().toFloat() * 30 + 15,
                        color = ChartUtils.pickColor()
                    )
                )
            }

            return PieChartData(values)
        }

        companion object {
            /**
             * The fragment argument representing the section number for this fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        override fun getItem(position: Int) = PlaceholderFragment.newInstance(position + 1)

        override fun getCount() = 5

        override fun getPageTitle(position: Int): CharSequence? = when (position) {
            0 -> "LineChart"
            1 -> "ColumnChart"
            2 -> "BubbleChart"
            3 -> "PreviewLineChart"
            4 -> "PieChart"
            else -> null
        }
    }
}
