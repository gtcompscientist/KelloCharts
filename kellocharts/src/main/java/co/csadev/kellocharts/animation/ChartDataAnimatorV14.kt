package co.csadev.kellocharts.animation

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint

import co.csadev.kellocharts.view.Chart

@SuppressLint("NewApi")
class ChartDataAnimatorV14(private val chart: Chart) : ChartDataAnimator, AnimatorListener, AnimatorUpdateListener {
    private val animator: ValueAnimator
    private var animationListener: ChartAnimationListener = DummyChartAnimationListener()

    override val isAnimationStarted: Boolean
        get() = animator.isStarted

    init {
        animator = ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.addListener(this)
        animator.addUpdateListener(this)
    }

    override fun startAnimation(duration: Long) {
        if (duration >= 0) {
            animator.duration = duration
        } else {
            animator.duration = ChartDataAnimator.Companion.DEFAULT_ANIMATION_DURATION
        }
        animator.start()
    }

    override fun cancelAnimation() {
        animator.cancel()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        chart.animationDataUpdate(animation.animatedFraction)
    }

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationEnd(animation: Animator) {
        chart.animationDataFinished()
        animationListener.onAnimationFinished()
    }

    override fun onAnimationRepeat(animation: Animator) {}

    override fun onAnimationStart(animation: Animator) {
        animationListener.onAnimationStarted()
    }

    override fun setChartAnimationListener(animationListener: ChartAnimationListener) {
        if (null == animationListener) {
            this.animationListener = DummyChartAnimationListener()
        } else {
            this.animationListener = animationListener
        }
    }

}
