package co.csadev.kellocharts.renderer

import android.content.Context
import android.graphics.Canvas
import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.model.set
import co.csadev.kellocharts.view.Chart
import java.util.*

open class ComboChartRenderer(context: Context, chart: Chart) : AbstractChartRenderer(context, chart) {

    internal var renderers: MutableList<ChartRenderer> = ArrayList()
    protected var unionViewport = Viewport()

    override fun onChartSizeChanged() {
        for (renderer in renderers) {
            renderer.onChartSizeChanged()
        }
    }

    override fun onChartDataChanged() {
        super.onChartDataChanged()
        for (renderer in renderers) {
            renderer.onChartDataChanged()
        }
        onChartViewportChanged()
    }

    override fun onChartViewportChanged() {
        if (isViewportCalculationEnabled) {
            var rendererIndex = 0
            for (renderer in renderers) {
                renderer.onChartViewportChanged()
                if (rendererIndex == 0) {
                    unionViewport.set(renderer.maximumViewport)
                } else {
                    unionViewport.union(renderer.maximumViewport)
                }
                ++rendererIndex
            }
            computator.maximumViewport = unionViewport
            computator.currentViewport = unionViewport
        }


    }

    override fun draw(canvas: Canvas) {
        for (renderer in renderers) {
            renderer.draw(canvas)
        }
    }

    override fun drawUnclipped(canvas: Canvas) {
        for (renderer in renderers) {
            renderer.drawUnclipped(canvas)
        }
    }

    override fun checkTouch(touchX: Float, touchY: Float): Boolean {
        selectedValue.clear()
        var rendererIndex = renderers.size - 1
        while (rendererIndex >= 0) {
            val renderer = renderers[rendererIndex]
            if (renderer.checkTouch(touchX, touchY)) {
                selectedValue.set(renderer.selectedValue)
                break
            }
            rendererIndex--
        }

        //clear the rest of renderers if value was selected, if value was not selected this loop
        // will not be executed.
        rendererIndex--
        while (rendererIndex >= 0) {
            val renderer = renderers[rendererIndex]
            renderer.clearTouch()
            rendererIndex--
        }

        return isTouched
    }

    override fun clearTouch() {
        for (renderer in renderers) {
            renderer.clearTouch()
        }
        selectedValue.clear()
    }
}
