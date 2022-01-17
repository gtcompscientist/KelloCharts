package co.csadev.kellocharts.gesture

import android.content.Context
import android.graphics.Point
import android.widget.OverScroller

import co.csadev.kellocharts.computator.ChartComputator
import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.model.set

/**
 * Encapsulates scrolling functionality.
 */
class ChartScroller(context: Context) {

    private val scrollerStartViewport = Viewport() // Used only for zooms and flings
    private val surfaceSizeBuffer = Point()// Used for scroll and flings
    private val scroller: OverScroller = OverScroller(context)

    fun startScroll(computator: ChartComputator): Boolean {
        scroller.abortAnimation()
        scrollerStartViewport.set(computator.currentViewport)
        return true
    }

    fun scroll(computator: ChartComputator, distanceX: Float, distanceY: Float, scrollResult: ScrollResult): Boolean {

        // Scrolling uses math based on the viewport (as opposed to math using pixels). Pixel offset is the offset in
        // screen pixels, while viewport offset is the offset within the current viewport. For additional
        // information on
        // surface sizes and pixel offsets, see the docs for {@link computeScrollSurfaceSize()}. For additional
        // information about the viewport, see the comments for {@link mCurrentViewport}.

        val maxViewport = computator.maximumViewport
        val visibleViewport = computator.visibleViewport
        val currentViewport = computator.currentViewport
        val contentRect = computator.contentRectMinusAllMargins

        val canScrollLeft = currentViewport.left > maxViewport.left
        val canScrollRight = currentViewport.right < maxViewport.right
        val canScrollTop = currentViewport.top < maxViewport.top
        val canScrollBottom = currentViewport.bottom > maxViewport.bottom

        var canScrollX = false
        var canScrollY = false

        if (canScrollLeft && distanceX <= 0) {
            canScrollX = true
        } else if (canScrollRight && distanceX >= 0) {
            canScrollX = true
        }

        if (canScrollTop && distanceY <= 0) {
            canScrollY = true
        } else if (canScrollBottom && distanceY >= 0) {
            canScrollY = true
        }

        if (canScrollX || canScrollY) {

            computator.computeScrollSurfaceSize(surfaceSizeBuffer)

            val viewportOffsetX = distanceX * visibleViewport.width() / contentRect.width()
            val viewportOffsetY = -distanceY * visibleViewport.height() / contentRect.height()

            computator
                    .setViewportTopLeft(currentViewport.left + viewportOffsetX, currentViewport.top + viewportOffsetY)
        }

        scrollResult.canScrollX = canScrollX
        scrollResult.canScrollY = canScrollY

        return canScrollX || canScrollY
    }

    fun computeScrollOffset(computator: ChartComputator): Boolean {
        if (scroller.computeScrollOffset()) {
            // The scroller isn't finished, meaning a fling or programmatic pan operation is
            // currently active.

            val maxViewport = computator.maximumViewport

            computator.computeScrollSurfaceSize(surfaceSizeBuffer)

            val currXRange = maxViewport.left + maxViewport.width() * scroller.currX / surfaceSizeBuffer.x
            val currYRange = maxViewport.top - maxViewport.height() * scroller.currY / surfaceSizeBuffer.y

            computator.setViewportTopLeft(currXRange, currYRange)

            return true
        }

        return false
    }

    fun fling(velocityX: Int, velocityY: Int, computator: ChartComputator): Boolean {
        // Flings use math in pixels (as opposed to math based on the viewport).
        computator.computeScrollSurfaceSize(surfaceSizeBuffer)
        scrollerStartViewport.set(computator.currentViewport)

        val startX = (surfaceSizeBuffer.x * (scrollerStartViewport.left - computator.maximumViewport.left) / computator.maximumViewport.width()).toInt()
        val startY = (surfaceSizeBuffer.y * (computator.maximumViewport.top - scrollerStartViewport.top) / computator.maximumViewport.height()).toInt()

        // TODO probably should be mScroller.forceFinish but ScrollerCompat doesn't have that method.
        scroller.abortAnimation()

        val width = computator.contentRectMinusAllMargins.width()
        val height = computator.contentRectMinusAllMargins.height()
        scroller.fling(startX, startY, velocityX, velocityY, 0, surfaceSizeBuffer.x - width + 1, 0,
                surfaceSizeBuffer.y - height + 1)
        return true
    }

    class ScrollResult {
        var canScrollX: Boolean = false
        var canScrollY: Boolean = false
    }

}
