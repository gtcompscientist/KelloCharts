package co.csadev.kellocharts.renderer

import android.graphics.Canvas

import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport

/**
 * Interface for all chart renderer.
 */
interface ChartRenderer {

    /**
     * Returns true if there is value selected.
     */
    val isTouched: Boolean

    var maximumViewport: Viewport

    var currentViewport: Viewport

    var isViewportCalculationEnabled: Boolean

    var selectedValue: SelectedValue

    fun onChartSizeChanged()

    fun onChartDataChanged()

    fun onChartViewportChanged()

    fun resetRenderer()

    /**
     * Draw chart data.
     */
    fun draw(canvas: Canvas)

    /**
     * Draw chart data that should not be clipped to contentRect area.
     */
    fun drawUnclipped(canvas: Canvas)

    /**
     * Checks if given pixel coordinates corresponds to any chart value. If yes return true and set selectedValue, if
     * not selectedValue should be *cleared* and method should return false.
     */
    fun checkTouch(touchX: Float, touchY: Float): Boolean

    /**
     * Clear value selection.
     */
    fun clearTouch()
}
