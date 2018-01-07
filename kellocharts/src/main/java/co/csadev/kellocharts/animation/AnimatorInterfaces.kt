package co.csadev.kellocharts.animation

import co.csadev.kellocharts.model.Viewport
import java.util.*

val FAST_ANIMATION_DURATION = 200L

interface ChartAnimationListener : EventListener {
    fun onAnimationStarted()
    fun onAnimationFinished()
}

interface ChartDataAnimator {
    val isAnimationStarted: Boolean
    fun startAnimation(duration: Long)
    fun cancelAnimation()
    fun setChartAnimationListener(animationListener: ChartAnimationListener?)
    companion object {
        val DEFAULT_ANIMATION_DURATION: Long = 500
    }
}

interface ChartViewportAnimator {
    val isAnimationStarted: Boolean
    fun startAnimation(startViewport: Viewport, targetViewport: Viewport)
    fun startAnimation(startViewport: Viewport, targetViewport: Viewport, duration: Long)
    fun cancelAnimation()
    fun setChartAnimationListener(animationListener: ChartAnimationListener?)
}

interface PieChartRotationAnimator {
    val isAnimationStarted: Boolean
    fun startAnimation(startAngle: Float, angleToRotate: Float)
    fun cancelAnimation()
    fun setChartAnimationListener(animationListener: ChartAnimationListener?)
}
