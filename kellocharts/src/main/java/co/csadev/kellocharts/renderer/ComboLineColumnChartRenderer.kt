package co.csadev.kellocharts.renderer

import android.content.Context

import co.csadev.kellocharts.provider.ColumnChartDataProvider
import co.csadev.kellocharts.provider.LineChartDataProvider
import co.csadev.kellocharts.view.Chart

class ComboLineColumnChartRenderer(context: Context, chart: Chart, private val columnChartRenderer: ColumnChartRenderer,
                                   private val lineChartRenderer: LineChartRenderer) : ComboChartRenderer(context, chart) {

    constructor(context: Context, chart: Chart, columnChartDataProvider: ColumnChartDataProvider, lineChartDataProvider: LineChartDataProvider)
            : this(context, chart, ColumnChartRenderer(context, chart, columnChartDataProvider), LineChartRenderer(context, chart, lineChartDataProvider))

    constructor(context: Context, chart: Chart, columnChartRenderer: ColumnChartRenderer, lineChartDataProvider: LineChartDataProvider)
            : this(context, chart, columnChartRenderer, LineChartRenderer(context, chart, lineChartDataProvider))

    constructor(context: Context, chart: Chart, columnChartDataProvider: ColumnChartDataProvider, lineChartRenderer: LineChartRenderer)
            : this(context, chart, ColumnChartRenderer(context, chart, columnChartDataProvider), lineChartRenderer)

    init {
        renderers.add(this.columnChartRenderer)
        renderers.add(this.lineChartRenderer)
    }
}