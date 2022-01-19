package co.csadev.kellocharts.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.widget.ScrollerCompat
import co.csadev.kellocharts.view.PieChartView

/**
 * Touch handler for PieChart. It doesn't handle zoom and scroll like default ChartTouchHandler. Instead it uses
 * Scroller(ScrollerCompat) directly to compute PieChart rotation when user scroll. ChartScroller and ChartZoomer are
 * not really used here.
 */
class PieChartTouchHandler(context: Context, chart: PieChartView) :
    ChartTouchHandler(context, chart) {
    /**
     * PieChartTouchHandler uses its own instance of Scroller.
     */
    protected var scroller: ScrollerCompat

    /**
     * Reference to PieChartView to use some methods specific for that kind of chart.
     */
    protected var pieChart: PieChartView

    var isRotationEnabled = true

    init {
        pieChart = chart
        scroller = ScrollerCompat.create(context)
        gestureDetector = GestureDetector(context, ChartGestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, ChartScaleGestureListener())
        isZoomEnabled = false
    }

    override fun computeScroll(): Boolean {
        if (!isRotationEnabled) {
            return false
        }
        if (scroller.computeScrollOffset()) {
            pieChart.setChartRotation(scroller.currY, false)
            // pieChart.setChartRotation() will invalidate view so no need to return true;
        }
        return false
    }

    override fun handleTouchEvent(event: MotionEvent): Boolean {
        var needInvalidate = super.handleTouchEvent(event)

        if (isRotationEnabled) {
            needInvalidate = gestureDetector.onTouchEvent(event) || needInvalidate
        }
        return needInvalidate
    }

    private inner class ChartScaleGestureListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // No scale for PieChart.
            return false
        }
    }

    private inner class ChartGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (isRotationEnabled) {
                scroller.abortAnimation()
                return true
            }

            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isRotationEnabled) {
                // Set the pie rotation directly.
                val circleOval = pieChart.circleOval
                val centerX = circleOval.centerX()
                val centerY = circleOval.centerY()
                val scrollTheta =
                    vectorToScalarScroll(distanceX, distanceY, e2.x - centerX, e2.y - centerY)
                pieChart.setChartRotation(
                    pieChart.chartRotation - scrollTheta.toInt() / FLING_VELOCITY_DOWNSCALE,
                    false
                )
                return true
            }

            return false
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (isRotationEnabled) {
                // Set up the Scroller for a fling
                val circleOval = pieChart.circleOval
                val centerX = circleOval.centerX()
                val centerY = circleOval.centerY()
                val scrollTheta =
                    vectorToScalarScroll(velocityX, velocityY, e2.x - centerX, e2.y - centerY)
                scroller.abortAnimation()
                scroller.fling(
                    0,
                    pieChart.chartRotation.toInt(),
                    0,
                    scrollTheta.toInt() / FLING_VELOCITY_DOWNSCALE,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE
                )
                return true
            }

            return false
        }

        /**
         * Helper method for translating (x,y) scroll vectors into scalar rotation of the pie.
         *
         * @param dx The x component of the current scroll vector.
         * @param dy The y component of the current scroll vector.
         * @param x  The x position of the current touch, relative to the pie center.
         * @param y  The y position of the current touch, relative to the pie center.
         * @return The scalar representing the change in angular position for this scroll.
         */
        private fun vectorToScalarScroll(dx: Float, dy: Float, x: Float, y: Float): Float {
            // get the length of the vector
            val l = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            // decide if the scalar should be negative or positive by finding
            // the dot product of the vector perpendicular to (x,y).
            val crossX = -y

            val dot = crossX * dx + x * dy
            val sign = Math.signum(dot)

            return l * sign
        }
    }

    companion object {
        /**
         * The initial fling velocity is divided by this amount.
         */
        val FLING_VELOCITY_DOWNSCALE = 4
    }
}
