package co.csadev.kellocharts.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewParent
import co.csadev.kellocharts.computator.ChartComputator
import co.csadev.kellocharts.gesture.ChartScroller.ScrollResult
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.renderer.ChartRenderer
import co.csadev.kellocharts.view.Chart

/**
 * Default touch handler for most charts. Handles value touch, scroll, fling and zoom.
 */
open class ChartTouchHandler(context: Context, protected var chart: Chart) {
    protected var gestureDetector: GestureDetector
    protected var scaleGestureDetector: ScaleGestureDetector
    protected var chartScroller: ChartScroller
    protected var chartZoomer: ChartZoomer
    protected var computator: ChartComputator
    protected var renderer: ChartRenderer

    var isZoomEnabled = true
    var isScrollEnabled = true
    var isValueTouchEnabled = true
    var isValueSelectionEnabled = false

    /**
     * Used only for selection mode to avoid calling listener multiple times for the same selection. Small thing but it
     * is more intuitive this way.
     */
    protected var selectionModeOldValue = SelectedValue()

    protected var selectedValue = SelectedValue()
    protected var oldSelectedValue = SelectedValue()

    /**
     * ViewParent to disallow touch events interception if chart is within scroll container.
     */
    protected var viewParent: ViewParent? = null

    /**
     * Type of scroll of container, horizontal or vertical.
     */
    protected var containerScrollType = ContainerScrollType.HORIZONTAL

    var zoomType: ZoomType?
        get() = chartZoomer.zoomType
        set(zoomType) {
            chartZoomer.zoomType = zoomType
        }

    init {
        this.computator = chart.chartComputator
        this.renderer = chart.chartRenderer
        gestureDetector = GestureDetector(context, ChartGestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, ChartScaleGestureListener())
        chartScroller = ChartScroller(context)
        chartZoomer = ChartZoomer(context, ZoomType.HORIZONTAL_AND_VERTICAL)
    }

    fun resetTouchHandler() {
        this.computator = chart.chartComputator
        this.renderer = chart.chartRenderer
    }

    /**
     * Computes scroll and zoom using [ChartScroller] and [ChartZoomer]. This method returns true if
     * scroll/zoom was computed and chart needs to be invalidated.
     */
    open fun computeScroll(): Boolean {
        var needInvalidate = false
        if (isScrollEnabled && chartScroller.computeScrollOffset(computator)) {
            needInvalidate = true
        }
        if (isZoomEnabled && chartZoomer.computeZoom(computator)) {
            needInvalidate = true
        }
        return needInvalidate
    }

    /**
     * Handle chart touch event(gestures, clicks). Return true if gesture was handled and chart needs to be
     * invalidated.
     */
    open fun handleTouchEvent(event: MotionEvent): Boolean {
        var needInvalidate: Boolean

        // TODO: detectors always return true, use class member needInvalidate instead local variable as workaround.
        // This flag should be computed inside gesture listeners methods to avoid invalidation.
        needInvalidate = gestureDetector.onTouchEvent(event)

        needInvalidate = scaleGestureDetector.onTouchEvent(event) || needInvalidate

        if (isZoomEnabled && scaleGestureDetector.isInProgress) {
            // Special case: if view is inside scroll container and user is scaling disable touch interception by
            // parent.
            disallowParentInterceptTouchEvent()
        }

        if (isValueTouchEnabled) {
            needInvalidate = computeTouch(event) || needInvalidate
        }

        return needInvalidate
    }

    /**
     * Handle chart touch event(gestures, clicks). Return true if gesture was handled and chart needs to be
     * invalidated.
     * If viewParent and containerScrollType are not null chart can be scrolled and scaled within horizontal or
     * vertical
     * scroll container like ViewPager.
     */
    fun handleTouchEvent(
        event: MotionEvent,
        viewParent: ViewParent,
        containerScrollType: ContainerScrollType
    ): Boolean {
        this.viewParent = viewParent
        this.containerScrollType = containerScrollType

        return handleTouchEvent(event)
    }

    /**
     * Disallow parent view from intercepting touch events. Use it for chart that is within some scroll container i.e.
     * ViewPager.
     */
    private fun disallowParentInterceptTouchEvent() {
        viewParent?.requestDisallowInterceptTouchEvent(true)
    }

    /**
     * Allow parent view to intercept touch events if chart cannot be scroll horizontally or vertically according to
     * the
     * current value of [.containerScrollType].
     */
    private fun allowParentInterceptTouchEvent(scrollResult: ScrollResult) {
        val parent = viewParent ?: return
        if (ContainerScrollType.HORIZONTAL == containerScrollType && !scrollResult.canScrollX &&
            !scaleGestureDetector.isInProgress
        ) {
            parent.requestDisallowInterceptTouchEvent(false)
        } else if (ContainerScrollType.VERTICAL == containerScrollType && !scrollResult.canScrollY &&
            !scaleGestureDetector.isInProgress
        ) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
    }

    private fun computeTouch(event: MotionEvent): Boolean {
        var needInvalidate = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val wasTouched = renderer.isTouched
                val isTouched = checkTouch(event.x, event.y)
                if (wasTouched != isTouched) {
                    needInvalidate = true

                    if (isValueSelectionEnabled) {
                        selectionModeOldValue.clear()
                        if (wasTouched && !renderer.isTouched) {
                            chart.callTouchListener()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> if (renderer.isTouched) {
                if (checkTouch(event.x, event.y)) {
                    if (isValueSelectionEnabled) {
                        // For selection mode call listener only if selected value changed,
                        // that means that should be
                        // first(selection) click on given value.
                        if (selectionModeOldValue != selectedValue) {
                            selectionModeOldValue.set(selectedValue)
                            chart.callTouchListener()
                        }
                    } else {
                        chart.callTouchListener()
                        renderer.clearTouch()
                    }
                } else {
                    renderer.clearTouch()
                }
                needInvalidate = true
            }
            MotionEvent.ACTION_MOVE ->
                // If value was touched and now touch point is outside of value area - clear touch and invalidate, user
                // probably moved finger away from given chart value.
                if (renderer.isTouched) {
                    if (!checkTouch(event.x, event.y)) {
                        renderer.clearTouch()
                        needInvalidate = true
                    }
                }
            MotionEvent.ACTION_CANCEL -> if (renderer.isTouched) {
                renderer.clearTouch()
                needInvalidate = true
            }
        }
        return needInvalidate
    }

    private fun checkTouch(touchX: Float, touchY: Float): Boolean {
        oldSelectedValue.set(selectedValue)
        selectedValue.clear()

        if (renderer.checkTouch(touchX, touchY)) {
            selectedValue.set(renderer.selectedValue)
        }

        // Check if selection is still on the same value, if not return false.
        return if (oldSelectedValue.isSet && selectedValue.isSet && oldSelectedValue != selectedValue) {
            false
        } else {
            renderer.isTouched
        }
    }

    protected inner class ChartScaleGestureListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (isZoomEnabled) {
                var scale = 2.0f - detector.scaleFactor
                if (java.lang.Float.isInfinite(scale)) {
                    scale = 1f
                }
                return chartZoomer.scale(computator, detector.focusX, detector.focusY, scale)
            }

            return false
        }
    }

    protected open inner class ChartGestureListener : GestureDetector.SimpleOnGestureListener() {

        protected var scrollResult = ScrollResult()

        override fun onDown(e: MotionEvent): Boolean {
            if (isScrollEnabled) {

                disallowParentInterceptTouchEvent()

                return chartScroller.startScroll(computator)
            }

            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return if (isZoomEnabled) {
                chartZoomer.startZoom(e, computator)
            } else false
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isScrollEnabled) {
                val canScroll = chartScroller
                    .scroll(computator, distanceX, distanceY, scrollResult)

                allowParentInterceptTouchEvent(scrollResult)

                return canScroll
            }

            return false
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return if (isScrollEnabled) {
                chartScroller.fling((-velocityX).toInt(), (-velocityY).toInt(), computator)
            } else false
        }
    }
}
