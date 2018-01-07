package co.csadev.kellocharts.view

import android.content.Context
import android.graphics.Canvas
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import co.csadev.kellocharts.animation.ChartAnimationListener
import co.csadev.kellocharts.animation.ChartDataAnimator
import co.csadev.kellocharts.animation.ChartDataAnimatorV14
import co.csadev.kellocharts.animation.ChartViewportAnimator
import co.csadev.kellocharts.animation.ChartViewportAnimatorV14
import co.csadev.kellocharts.computator.ChartComputator
import co.csadev.kellocharts.gesture.ChartTouchHandler
import co.csadev.kellocharts.gesture.ContainerScrollType
import co.csadev.kellocharts.gesture.ZoomType
import co.csadev.kellocharts.listener.ViewportChangeListener
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.model.copy
import co.csadev.kellocharts.model.set
import co.csadev.kellocharts.renderer.*
import co.csadev.kellocharts.util.ChartUtils

/**
 * Abstract class for charts views.
 *
 * @author Leszek Wach
 */
abstract class AbstractChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr), Chart {
    override val chartComputator: ChartComputator = ChartComputator()
    override val axesRenderer: AxesRenderer = AxesRenderer(context, this)
    override var touchHandler: ChartTouchHandler = ChartTouchHandler(context, this)
    override var chartRenderer: ChartRenderer = InternalChartRendererBase(context, this)
        set(value) {
            field=  value
            resetRendererAndTouchHandler()
            ViewCompat.postInvalidateOnAnimation(this)
        }


    var dataAnimator: ChartDataAnimator = ChartDataAnimatorV14(this)
    var viewportAnimator: ChartViewportAnimator = ChartViewportAnimatorV14(this)
    override var isInteractive = true
    override var isContainerScrollEnabled = false
    var containerScrollType: ContainerScrollType = ContainerScrollType.HORIZONTAL

    override var isZoomEnabled: Boolean
        get() = touchHandler.isZoomEnabled
        set(value) { touchHandler.isZoomEnabled = value }
    override var isScrollEnabled: Boolean
        get() = touchHandler.isScrollEnabled
        set(value) { touchHandler.isScrollEnabled = value }
    override var zoomType: ZoomType?
        get() = touchHandler.zoomType
        set(value) { touchHandler.zoomType = value }

    override var maxZoom: Float
        get() = chartComputator.maxZoom
        set(value) {
            chartComputator.maxZoom = value
            ViewCompat.postInvalidateOnAnimation(this)
        }

    override var zoomLevel: Float
        get() = Math.max(maximumViewport.width() / currentViewport.width(), maximumViewport.height() / currentViewport.height())
        set(value) {
            currentViewport = computeZoomViewport(x, y, value)
        }

    override fun setZoomLevel(x: Float, y: Float, zoomLevel: Float) {
        this.x = x
        this.y = y
        this.zoomLevel = zoomLevel
    }

    override var isValueTouchEnabled: Boolean
        get() = touchHandler.isValueTouchEnabled
        set(value) { touchHandler.isValueTouchEnabled = value }

    override var maximumViewport: Viewport
        get() = chartRenderer.maximumViewport
        set(value) {
            chartRenderer.maximumViewport = value
            ViewCompat.postInvalidateOnAnimation(this)
        }

    override var currentViewport: Viewport
        get() = chartRenderer.currentViewport
        set(value) {
            chartRenderer.currentViewport = value
            ViewCompat.postInvalidateOnAnimation(this)
        }

    override var selectedValue: SelectedValue
        get() = chartRenderer.selectedValue
        set(value) {
            chartRenderer.selectedValue = value
            callTouchListener()
            ViewCompat.postInvalidateOnAnimation(this)
        }

    override var isViewportCalculationEnabled: Boolean
        get() = chartRenderer.isViewportCalculationEnabled
        set(value) { chartRenderer.isViewportCalculationEnabled = value }

    override var isValueSelectionEnabled: Boolean
        get() = touchHandler.isValueSelectionEnabled
        set(value) { touchHandler.isValueSelectionEnabled = value }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        chartComputator.setContentRect(getWidth(), getHeight(), paddingLeft, paddingTop, paddingRight,
                paddingBottom)
        chartRenderer.onChartSizeChanged()
        axesRenderer.onChartSizeChanged()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isEnabled) {
            axesRenderer.drawInBackground(canvas)
            val clipRestoreCount = canvas.save()
            canvas.clipRect(chartComputator.contentRectMinusAllMargins)
            chartRenderer.draw(canvas)
            canvas.restoreToCount(clipRestoreCount)
            chartRenderer.drawUnclipped(canvas)
            axesRenderer.drawInForeground(canvas)
        } else {
            canvas.drawColor(ChartUtils.DEFAULT_COLOR)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        if (isInteractive) {

            val needInvalidate
                    = if (isContainerScrollEnabled)
                        touchHandler.handleTouchEvent(event, parent, containerScrollType)
                    else
                        touchHandler.handleTouchEvent(event)

            if (needInvalidate) {
                ViewCompat.postInvalidateOnAnimation(this)
            }

            return true
        } else {

            return false
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (isInteractive) {
            if (touchHandler.computeScroll()) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    override fun startDataAnimation() {
        dataAnimator.startAnimation(java.lang.Long.MIN_VALUE)
    }

    override fun startDataAnimation(duration: Long) {
        dataAnimator.startAnimation(duration)
    }

    override fun cancelDataAnimation() {
        dataAnimator.cancelAnimation()
    }

    override fun animationDataUpdate(scale: Float) {
        chartData.update(scale)
        chartRenderer.onChartViewportChanged()
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun animationDataFinished() {
        chartData.finish()
        chartRenderer.onChartViewportChanged()
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun setDataAnimationListener(animationListener: ChartAnimationListener?) {
        dataAnimator.setChartAnimationListener(animationListener)
    }

    override fun setViewportAnimationListener(animationListener: ChartAnimationListener?) {
        viewportAnimator.setChartAnimationListener(animationListener)
    }

    override fun setViewportChangeListener(viewportChangeListener: ViewportChangeListener?) {
        chartComputator.viewportChangeListener = viewportChangeListener
    }

    override fun moveTo(x: Float, y: Float) {
        val scrollViewport = computeScrollViewport(x, y)
        currentViewport = scrollViewport
    }

    override fun moveToWithAnimation(x: Float, y: Float) {
        val scrollViewport = computeScrollViewport(x, y)
        setCurrentViewportWithAnimation(scrollViewport)
    }

    private fun computeScrollViewport(x: Float, y: Float): Viewport {
        val maxViewport = maximumViewport
        val currentViewport = currentViewport
        val scrollViewport = currentViewport.copy()

        if (maxViewport.contains(x, y)) {
            val width = currentViewport.width()
            val height = currentViewport.height()

            val halfWidth = width / 2
            val halfHeight = height / 2

            var left = x - halfWidth
            var top = y + halfHeight

            left = Math.max(maxViewport.left, Math.min(left, maxViewport.right - width))
            top = Math.max(maxViewport.bottom + height, Math.min(top, maxViewport.top))

            scrollViewport.set(left, top, left + width, top - height)
        }

        return scrollViewport
    }

    override fun setZoomLevelWithAnimation(x: Float, y: Float, zoomLevel: Float) {
        val zoomViewport = computeZoomViewport(x, y, zoomLevel)
        setCurrentViewportWithAnimation(zoomViewport)
    }

    private fun computeZoomViewport(x: Float, y: Float, zoomLevel: Float): Viewport {
        var zoomLevel = zoomLevel
        val maxViewport = maximumViewport
        val zoomViewport = maximumViewport.copy()

        if (maxViewport.contains(x, y)) {

            if (zoomLevel < 1) {
                zoomLevel = 1f
            } else if (zoomLevel > maxZoom) {
                zoomLevel = maxZoom
            }

            val newWidth = zoomViewport.width() / zoomLevel
            val newHeight = zoomViewport.height() / zoomLevel

            val halfWidth = newWidth / 2
            val halfHeight = newHeight / 2

            var left = x - halfWidth
            var right = x + halfWidth
            var top = y + halfHeight
            var bottom = y - halfHeight

            if (left < maxViewport.left) {
                left = maxViewport.left
                right = left + newWidth
            } else if (right > maxViewport.right) {
                right = maxViewport.right
                left = right - newWidth
            }

            if (top > maxViewport.top) {
                top = maxViewport.top
                bottom = top - newHeight
            } else if (bottom < maxViewport.bottom) {
                bottom = maxViewport.bottom
                top = bottom + newHeight
            }

            val zoomType = zoomType
            when {
                ZoomType.HORIZONTAL_AND_VERTICAL === zoomType -> zoomViewport.set(left, top, right, bottom)
                ZoomType.HORIZONTAL === zoomType -> {
                    zoomViewport.left = left
                    zoomViewport.right = right
                }
                ZoomType.VERTICAL === zoomType -> {
                    zoomViewport.top = top
                    zoomViewport.bottom = bottom
                }
            }

        }
        return zoomViewport
    }

    override fun setCurrentViewportWithAnimation(targetViewport: Viewport?) {
        if (null != targetViewport) {
            viewportAnimator.cancelAnimation()
            viewportAnimator.startAnimation(currentViewport, targetViewport)
        }
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun setCurrentViewportWithAnimation(targetViewport: Viewport?, duration: Long) {
        if (null != targetViewport) {
            viewportAnimator.cancelAnimation()
            viewportAnimator.startAnimation(currentViewport, targetViewport, duration)
        }
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun setContainerScrollEnabled(isContainerScrollEnabled: Boolean, containerScrollType: ContainerScrollType) {
        this.isContainerScrollEnabled = isContainerScrollEnabled
        this.containerScrollType = containerScrollType
    }

    override fun resetViewports() {
        chartRenderer.maximumViewport = Viewport()
        chartRenderer.currentViewport = Viewport()
    }

    protected fun onChartDataChange() {
        chartComputator.resetContentRect()
        chartRenderer.onChartDataChanged()
        axesRenderer.onChartDataChanged()
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * You should call this method in derived classes, most likely from constructor if you changed chart/axis renderer,
     * touch handler or chart computator
     */
    protected fun resetRendererAndTouchHandler() {
        this.chartRenderer.resetRenderer()
        this.axesRenderer.resetRenderer()
        this.touchHandler.resetTouchHandler()
    }

    /**
     * When embedded in a ViewPager, this will be called in order to know if we can scroll.
     * If this returns true, the ViewPager will ignore the drag so that we can scroll our content.
     * If this return false, the ViewPager will assume we won't be able to scroll and will consume the drag
     *
     * @param direction Amount of pixels being scrolled (x axis)
     * @return true if the chart can be scrolled (ie. zoomed and not against the edge of the chart)
     */
    override fun canScrollHorizontally(direction: Int): Boolean {
        if (zoomLevel <= 1.0) {
            return false
        }
        val currentViewport = currentViewport
        val maximumViewport = maximumViewport
        return if (direction < 0) {
            currentViewport.left > maximumViewport.left
        } else {
            currentViewport.right < maximumViewport.right
        }
    }
}
