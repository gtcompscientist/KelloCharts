package co.csadev.kellocharts.computator

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import co.csadev.kellocharts.listener.DummyViewportChangeListener
import co.csadev.kellocharts.listener.ViewportChangeListener
import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.model.set

/**
 * Computes raw points coordinates(in pixels), holds content area dimensions and chart viewport.
 */
open class ChartComputator {
    internal var maxZoom = DEFAULT_MAXIMUM_ZOOM
        set(value) {
            field = value
            if (field < 1) {
                field = 1f
            }
            computeMinimumWidthAndHeight()
            currentViewport = currentViewport
        }

    var chartWidth: Int = 0
        protected set
    var chartHeight: Int = 0
        protected set
    // contentRectMinusAllMargins <= contentRectMinusAxesMargins <= maxContentRect
    /**
     * Returns content rectangle in pixels.
     *
     * @see .setContentRect
     */
    var contentRectMinusAllMargins = Rect()
        protected set

    /**
     * Returns content rectangle with chart internal margins, for example for LineChart contentRectMinusAxesMargins is
     * bigger
     * than contentRectMinusAllMargins by point radius, thanks to that points are not cut on edges.
     *
     * @see .setContentRect
     */
    var contentRectMinusAxesMargins = Rect()
        protected set
    protected var maxContentRect = Rect()

    /**
     * This rectangle represents the currently visible chart values ranges. The currently visible chart X values are
     * from this rectangle's left to its right. The currently visible chart Y values are from this rectangle's top to
     * its bottom.
     */
    internal var currentViewport: Viewport = Viewport()
        set(value) {
            constrainViewport(value.left, value.top, value.right, value.bottom)
        }

    /**
     * Returns maximum viewport - values ranges extremes.
     */
    var maximumViewport = Viewport()
        internal set
    var minimumViewportWidth: Float = 0.toFloat()
        protected set
    var minimumViewportHeight: Float = 0.toFloat()
        protected set

    /**
     * Warning! Viewport listener is disabled for all charts beside preview charts to avoid additional method calls
     * during animations.
     */
    var viewportChangeListener: ViewportChangeListener? = DummyViewportChangeListener()
        internal set

    /**
     * Returns viewport for visible part of chart, for most charts it is equal to current viewport.
     *
     * @return
     */
    open var visibleViewport: Viewport
        get() = currentViewport
        set(visibleViewport) {
            currentViewport = visibleViewport
        }

    /**
     * Calculates available width and height. Should be called when chart dimensions change. ContentRect is relative to
     * chart view not the device's screen.
     */
    fun setContentRect(
        width: Int,
        height: Int,
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        chartWidth = width
        chartHeight = height
        maxContentRect.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        contentRectMinusAxesMargins.set(maxContentRect)
        contentRectMinusAllMargins.set(maxContentRect)
    }

    fun resetContentRect() {
        contentRectMinusAxesMargins.set(maxContentRect)
        contentRectMinusAllMargins.set(maxContentRect)
    }

    fun insetContentRect(deltaLeft: Int, deltaTop: Int, deltaRight: Int, deltaBottom: Int) {
        contentRectMinusAxesMargins.left = contentRectMinusAxesMargins.left + deltaLeft
        contentRectMinusAxesMargins.top = contentRectMinusAxesMargins.top + deltaTop
        contentRectMinusAxesMargins.right = contentRectMinusAxesMargins.right - deltaRight
        contentRectMinusAxesMargins.bottom = contentRectMinusAxesMargins.bottom - deltaBottom

        insetContentRectByInternalMargins(deltaLeft, deltaTop, deltaRight, deltaBottom)
    }

    fun insetContentRectByInternalMargins(
        deltaLeft: Int,
        deltaTop: Int,
        deltaRight: Int,
        deltaBottom: Int
    ) {
        contentRectMinusAllMargins.left = contentRectMinusAllMargins.left + deltaLeft
        contentRectMinusAllMargins.top = contentRectMinusAllMargins.top + deltaTop
        contentRectMinusAllMargins.right = contentRectMinusAllMargins.right - deltaRight
        contentRectMinusAllMargins.bottom = contentRectMinusAllMargins.bottom - deltaBottom
    }

    /**
     * Checks if new viewport doesn't exceed max available viewport.
     */
    open fun constrainViewport(left: Float, top: Float, right: Float, bottom: Float) {
        var l = left
        var t = top
        var r = right
        var b = bottom

        if (r - l < minimumViewportWidth) {
            // Minimum width - constrain horizontal zoom!
            r = l + minimumViewportWidth
            if (l < maximumViewport.left) {
                l = maximumViewport.left
                r = l + minimumViewportWidth
            } else if (r > maximumViewport.right) {
                r = maximumViewport.right
                l = r - minimumViewportWidth
            }
        }

        if (t - b < minimumViewportHeight) {
            // Minimum height - constrain vertical zoom!
            b = t - minimumViewportHeight
            if (t > maximumViewport.top) {
                t = maximumViewport.top
                b = t - minimumViewportHeight
            } else if (b < maximumViewport.bottom) {
                b = maximumViewport.bottom
                t = b + minimumViewportHeight
            }
        }

        currentViewport.left = Math.max(maximumViewport.left, l)
        currentViewport.top = Math.min(maximumViewport.top, t)
        currentViewport.right = Math.min(maximumViewport.right, r)
        currentViewport.bottom = Math.max(maximumViewport.bottom, b)

        viewportChangeListener?.onViewportChanged(currentViewport)
    }

    /**
     * Sets the current viewport (defined by [.currentViewport]) to the given X and Y positions.
     */
    fun setViewportTopLeft(left: Float, top: Float) {
        /**
         * Constrains within the scroll range. The scroll range is simply the viewport extremes (AXIS_X_MAX,
         * etc.) minus
         * the viewport size. For example, if the extrema were 0 and 10, and the viewport size was 2, the scroll range
         * would be 0 to 8.
         */
        val curWidth = currentViewport.width()
        val curHeight = currentViewport.height()
        val l = left.coerceIn(maximumViewport.left, maximumViewport.right - curWidth)
        val t = top.coerceIn(maximumViewport.bottom + curHeight, maximumViewport.top)
        constrainViewport(l, t, l + curWidth, t - curHeight)
    }

    /**
     * Translates chart value into raw pixel value. Returned value is absolute pixel X coordinate. If this method
     * return
     * 0 that means left most pixel of the screen.
     */
    open fun computeRawX(valueX: Float): Float {
        // TODO: (contentRectMinusAllMargins.width() / currentViewport.width()) can be recalculated only when viewport
        // change.
        val pixelOffset =
            (valueX - currentViewport.left) * (contentRectMinusAllMargins.width() / currentViewport.width())
        return contentRectMinusAllMargins.left + pixelOffset
    }

    /**
     * Translates chart value into raw pixel value. Returned value is absolute pixel Y coordinate. If this method
     * return
     * 0 that means top most pixel of the screen.
     */
    open fun computeRawY(valueY: Float): Float {
        val pixelOffset =
            (valueY - currentViewport.bottom) * (contentRectMinusAllMargins.height() / currentViewport.height())
        return contentRectMinusAllMargins.bottom - pixelOffset
    }

    /**
     * Translates viewport distance int pixel distance for X coordinates.
     */
    fun computeRawDistanceX(distance: Float): Float {
        return distance * (contentRectMinusAllMargins.width() / currentViewport.width())
    }

    /**
     * Translates viewport distance int pixel distance for X coordinates.
     */
    fun computeRawDistanceY(distance: Float): Float {
        return distance * (contentRectMinusAllMargins.height() / currentViewport.height())
    }

    /**
     * Finds the chart point (i.e. within the chart's domain and range) represented by the given pixel coordinates, if
     * that pixel is within the chart region described by [.contentRectMinusAllMargins]. If the point is found,
     * the "dest"
     * argument is set to the point and this function returns true. Otherwise, this function returns false and
     * "dest" is
     * unchanged.
     */
    fun rawPixelsToDataPoint(x: Float, y: Float, dest: PointF): Boolean {
        if (!contentRectMinusAllMargins.contains(x.toInt(), y.toInt())) {
            return false
        }
        dest.set(
            currentViewport.left + (x - contentRectMinusAllMargins.left) * currentViewport.width() / contentRectMinusAllMargins.width(),
            currentViewport.bottom + (y - contentRectMinusAllMargins.bottom) * currentViewport.height() / -contentRectMinusAllMargins.height()
        )
        return true
    }

    /**
     * Computes the current scrollable surface size, in pixels. For example, if the entire chart area is visible, this
     * is simply the current size of [.contentRectMinusAllMargins]. If the chart is zoomed in 200% in both
     * directions, the
     * returned size will be twice as large horizontally and vertically.
     */
    fun computeScrollSurfaceSize(out: Point) {
        out.set(
            (maximumViewport.width() * contentRectMinusAllMargins.width() / currentViewport.width()).toInt(),
            (maximumViewport.height() * contentRectMinusAllMargins.height() / currentViewport.height()).toInt()
        )
    }

    /**
     * Check if given coordinates lies inside contentRectMinusAllMargins.
     */
    fun isWithinContentRect(x: Float, y: Float, precision: Float): Boolean {
        if (x >= contentRectMinusAllMargins.left - precision && x <= contentRectMinusAllMargins.right + precision) {
            if (y <= contentRectMinusAllMargins.bottom + precision && y >= contentRectMinusAllMargins.top - precision) {
                return true
            }
        }
        return false
    }

    /**
     * Set new values for curent viewport, that will change what part of chart is visible. Current viewport must be
     * equal or smaller than maximum viewport.
     */
    fun setCurrentViewport(left: Float, top: Float, right: Float, bottom: Float) {
        constrainViewport(left, top, right, bottom)
    }

    /**
     * Set new values for maximum viewport, that will change what part of chart is visible.
     */
    fun setMaxViewport(left: Float, top: Float = left, right: Float = left, bottom: Float = left) {
        maximumViewport.set(left, top, right, bottom)
        computeMinimumWidthAndHeight()
    }

    private fun computeMinimumWidthAndHeight() {
        minimumViewportWidth = maximumViewport.width() / maxZoom
        minimumViewportHeight = maximumViewport.height() / maxZoom
    }

    companion object {

        /**
         * Maximum chart zoom.
         */
        protected const val DEFAULT_MAXIMUM_ZOOM = 20f
    }
}
