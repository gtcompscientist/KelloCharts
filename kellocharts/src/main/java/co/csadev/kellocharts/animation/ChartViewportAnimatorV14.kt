package co.csadev.kellocharts.animation

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint

import co.csadev.kellocharts.model.Viewport
import co.csadev.kellocharts.model.set
import co.csadev.kellocharts.view.Chart

@SuppressLint("NewApi")
class ChartViewportAnimatorV14(private val chart: Chart) : ChartViewportAnimator, AnimatorListener, AnimatorUpdateListener {
    private val animator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
    private val startViewport = Viewport()
    private val targetViewport = Viewport()
    private val newViewport = Viewport()
    private var animationListener: ChartAnimationListener? = DummyChartAnimationListener()

    override val isAnimationStarted: Boolean
        get() = animator.isStarted

    init {
        animator.addListener(this)
        animator.addUpdateListener(this)
        animator.duration = FAST_ANIMATION_DURATION
    }

    override fun startAnimation(startViewport: Viewport, targetViewport: Viewport) {
        this.startViewport.set(startViewport)
        this.targetViewport.set(targetViewport)
        animator.duration = FAST_ANIMATION_DURATION
        animator.start()
    }

    override fun startAnimation(startViewport: Viewport, targetViewport: Viewport, duration: Long) {
        this.startViewport.set(startViewport)
        this.targetViewport.set(targetViewport)
        animator.duration = duration
        animator.start()
    }

    override fun cancelAnimation() {
        animator.cancel()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val scale = animation.animatedFraction
        val diffLeft = (targetViewport.left - startViewport.left) * scale
        val diffTop = (targetViewport.top - startViewport.top) * scale
        val diffRight = (targetViewport.right - startViewport.right) * scale
        val diffBottom = (targetViewport.bottom - startViewport.bottom) * scale
        newViewport.set(startViewport.left + diffLeft, startViewport.top + diffTop, startViewport.right + diffRight, startViewport.bottom + diffBottom)
        chart.currentViewport = newViewport
    }

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationEnd(animation: Animator) {
        chart.currentViewport = targetViewport
        animationListener?.onAnimationFinished()
    }

    override fun onAnimationRepeat(animation: Animator) {}

    override fun onAnimationStart(animation: Animator) {
        animationListener?.onAnimationStarted()
    }

    override fun setChartAnimationListener(animationListener: ChartAnimationListener?) {
        this.animationListener = animationListener
    }

}
