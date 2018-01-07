package co.csadev.kellocharts.computator

import co.csadev.kellocharts.model.Viewport

/**
 * Version of ChartComputator for preview charts. It always uses maxViewport as visible viewport and currentViewport as
 * preview area.
 */
class PreviewChartComputator : ChartComputator() {

    override fun computeRawX(valueX: Float): Float {
        val pixelOffset = (valueX - maximumViewport.left) * (contentRectMinusAllMargins.width() / maximumViewport
                .width())
        return contentRectMinusAllMargins.left + pixelOffset
    }

    override fun computeRawY(valueY: Float): Float {
        val pixelOffset = (valueY - maximumViewport.bottom) * (contentRectMinusAllMargins.height() / maximumViewport
                .height())
        return contentRectMinusAllMargins.bottom - pixelOffset
    }

    override var visibleViewport: Viewport
        get() = maximumViewport
        set(value) { maximumViewport = value }

    override fun constrainViewport(left: Float, top: Float, right: Float, bottom: Float) {
        super.constrainViewport(left, top, right, bottom)
        viewportChangeListener?.onViewportChanged(currentViewport)
    }

}