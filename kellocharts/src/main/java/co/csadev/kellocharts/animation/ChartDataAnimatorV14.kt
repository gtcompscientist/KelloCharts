package co.csadev.kellocharts.animation

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import co.csadev.kellocharts.view.Chart

class ChartDataAnimatorV14(private val chart: Chart) :
    ChartDataAnimator,
    AnimatorListener,
    AnimatorUpdateListener {
    private val animator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
    private var animationListener: ChartAnimationListener? = DummyChartAnimationListener()

    override val isAnimationStarted: Boolean
        get() = animator.isStarted

    init {
        animator.addListener(this)
        animator.addUpdateListener(this)
    }

    override fun startAnimation(duration: Long) {
        if (duration >= 0) {
            animator.duration = duration
        } else {
            animator.duration = ChartDataAnimator.DEFAULT_ANIMATION_DURATION
        }
        animator.start()
    }

    override fun cancelAnimation() = animator.cancel()

    override fun onAnimationUpdate(animation: ValueAnimator) =
        chart.animationDataUpdate(animation.animatedFraction)

    override fun onAnimationCancel(animation: Animator) = Unit

    override fun onAnimationEnd(animation: Animator) {
        chart.animationDataFinished()
        animationListener?.onAnimationFinished()
    }

    override fun onAnimationRepeat(animation: Animator) = Unit

    override fun onAnimationStart(animation: Animator) {
        animationListener?.onAnimationStarted()
    }

    override fun setChartAnimationListener(animationListener: ChartAnimationListener?) {
        this.animationListener = animationListener
    }
}
