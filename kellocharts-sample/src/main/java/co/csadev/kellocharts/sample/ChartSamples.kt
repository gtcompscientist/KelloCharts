package co.csadev.kellocharts.sample

@Suppress("MaximumLineLength", "MaxLineLength")
enum class ChartSamples(
    val text1: String,
    val text2: String = "",
    val type: ChartType = ChartType.OTHER
) {
    Line("Line Chart", type = ChartType.LINE_CHART),
    Column("Column Chart", type = ChartType.COLUMN_CHART),
    Pie("Pie Chart", type = ChartType.PIE_CHART),
    Bubble("Bubble Chart", type = ChartType.BUBBLE_CHART),
    PreviewLine(
        "Preview Line Chart",
        "Control line chart viewport with another line chart.",
        ChartType.PREVIEW_LINE_CHART
    ),
    PreviewColumn(
        "Preview Column Chart",
        "Control column chart viewport with another column chart.",
        ChartType.PREVIEW_COLUMN_CHART
    ),
    ComboLineColumn("Combo Line/Column Chart", "Combo chart with lines and columns."),
    LineColumn(
        "Line/Column Chart Dependency",
        "LineChart responds(with animation) to column chart value selection."
    ),
    Tempo(
        "Tempo Chart",
        "Presents tempo and height values on a single chart. Example of multiple axes and reverted Y axis with time format [mm:ss]."
    ),
    Speed(
        "Speed Chart",
        "Presents speed and height values on a single chart. Example of multiple axes inside chart area."
    ),
    GoodBad("Good/Bad Chart", "Example of filled area line chart with custom labels"),
    ViewPager(
        "ViewPager with Charts",
        "Interactive charts within ViewPager. Each chart can be zoom/scroll except pie chart."
    )
}
