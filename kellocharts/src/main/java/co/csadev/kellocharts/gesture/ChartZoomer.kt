package co.csadev.kellocharts.gesture

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent

import co.csadev.kellocharts.computator.ChartComputator
import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.model.set

/**
 * Encapsulates zooming functionality.
 */
class ChartZoomer(context: Context, var zoomType: ZoomType?) {
    private val zoomer = ZoomerCompat(context)
    private val zoomFocalPoint = PointF()// Used for double tap zoom
    private val viewportFocus = PointF()
    private val scrollerStartViewport = Viewport() // Used only for zooms and flings

    fun startZoom(e: MotionEvent, computator: ChartComputator): Boolean {
        zoomer.forceFinished(true)
        scrollerStartViewport.set(computator.currentViewport)
        if (!computator.rawPixelsToDataPoint(e.x, e.y, zoomFocalPoint)) {
            // Focus point is not within content area.
            return false
        }
        zoomer.startZoom(ZOOM_AMOUNT)
        return true
    }

    fun computeZoom(computator: ChartComputator): Boolean {
        if (zoomer.computeZoom()) {
            // Performs the zoom since a zoom is in progress.
            val newWidth = (1.0f - zoomer.currZoom) * scrollerStartViewport.width()
            val newHeight = (1.0f - zoomer.currZoom) * scrollerStartViewport.height()
            val pointWithinViewportX = (zoomFocalPoint.x - scrollerStartViewport.left) / scrollerStartViewport.width()
            val pointWithinViewportY = (zoomFocalPoint.y - scrollerStartViewport.bottom) / scrollerStartViewport.height()

            val left = zoomFocalPoint.x - newWidth * pointWithinViewportX
            val top = zoomFocalPoint.y + newHeight * (1 - pointWithinViewportY)
            val right = zoomFocalPoint.x + newWidth * (1 - pointWithinViewportX)
            val bottom = zoomFocalPoint.y - newHeight * pointWithinViewportY
            setCurrentViewport(computator, left, top, right, bottom)
            return true
        }
        return false
    }

    fun scale(computator: ChartComputator, focusX: Float, focusY: Float, scale: Float): Boolean {
        /**
         * Smaller viewport means bigger zoom so for zoomIn scale should have value <1, for zoomOout >1
         */
        val newWidth = scale * computator.currentViewport.width()
        val newHeight = scale * computator.currentViewport.height()
        if (!computator.rawPixelsToDataPoint(focusX, focusY, viewportFocus)) {
            // Focus point is not within content area.
            return false
        }

        val left = viewportFocus.x - (focusX - computator.contentRectMinusAllMargins.left) * (newWidth / computator.contentRectMinusAllMargins.width())
        val top = viewportFocus.y + (focusY - computator.contentRectMinusAllMargins.top) * (newHeight / computator.contentRectMinusAllMargins.height())
        val right = left + newWidth
        val bottom = top - newHeight
        setCurrentViewport(computator, left, top, right, bottom)
        return true
    }

    private fun setCurrentViewport(computator: ChartComputator, left: Float, top: Float, right: Float, bottom: Float) {
        val currentViewport = computator.currentViewport
        when (zoomType ?: return) {
            ZoomType.HORIZONTAL -> computator.setCurrentViewport(left, currentViewport.top, right, currentViewport.bottom)
            ZoomType.VERTICAL -> computator.setCurrentViewport(currentViewport.left, top, currentViewport.right, bottom)
            ZoomType.HORIZONTAL_AND_VERTICAL -> computator.setCurrentViewport(left, top, right, bottom)
        }
    }

    companion object {
        val ZOOM_AMOUNT = 0.25f
    }
}
