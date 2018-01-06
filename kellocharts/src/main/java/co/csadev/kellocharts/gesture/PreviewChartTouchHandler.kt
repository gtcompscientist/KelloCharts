package co.csadev.kellocharts.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

import co.csadev.kellocharts.view.Chart

/**
 * Touch Handler for preview charts. It scroll and zoom only preview area, not all preview chart data.
 */
class PreviewChartTouchHandler(context: Context, chart: Chart) : ChartTouchHandler(context, chart) {

    init {
        gestureDetector = GestureDetector(context, PreviewChartGestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, ChartScaleGestureListener())

        // Disable value touch and selection mode, by default not needed for preview chart.
        isValueTouchEnabled = false
        isValueSelectionEnabled = false
    }

    protected inner class ChartScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (isZoomEnabled) {
                var scale = detector.currentSpan / detector.previousSpan
                if (java.lang.Float.isInfinite(scale)) {
                    scale = 1f
                }
                return chartZoomer.scale(computator, detector.focusX, detector.focusY, scale)
            }

            return false
        }
    }

    protected inner class PreviewChartGestureListener : ChartTouchHandler.ChartGestureListener() {

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return super.onScroll(e1, e2, -distanceX, -distanceY)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return super.onFling(e1, e2, -velocityX, -velocityY)
        }
    }

}
