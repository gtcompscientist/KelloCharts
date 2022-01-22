package co.csadev.kellocharts.animation

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import co.csadev.kellocharts.util.THREE_SIXTY
import co.csadev.kellocharts.view.PieChartView

class PieChartRotationAnimatorV14 @JvmOverloads constructor(
    private val chart: PieChartView,
    duration: Long = 200L
) : PieChartRotationAnimator, AnimatorListener, AnimatorUpdateListener {
    private val animator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
    private var startRotation = 0f
    private var targetRotation = 0f
    private var animationListener: ChartAnimationListener = DummyChartAnimationListener()

    override val isAnimationStarted: Boolean
        get() = animator.isStarted

    init {
        animator.duration = duration
        animator.addListener(this)
        animator.addUpdateListener(this)
    }

    override fun startAnimation(startAngle: Float, angleToRotate: Float) {
        this.startRotation = (startAngle % THREE_SIXTY + THREE_SIXTY) % THREE_SIXTY
        this.targetRotation = (angleToRotate % THREE_SIXTY + THREE_SIXTY) % THREE_SIXTY
        animator.start()
    }

    override fun cancelAnimation() {
        animator.cancel()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val scale = animation.animatedFraction
        var rotation = startRotation + (targetRotation - startRotation) * scale
        rotation = (rotation % THREE_SIXTY + THREE_SIXTY) % THREE_SIXTY
        chart.setChartRotation(rotation.toInt(), false)
    }

    override fun onAnimationCancel(animation: Animator) = Unit

    override fun onAnimationEnd(animation: Animator) {
        chart.setChartRotation(targetRotation.toInt(), false)
        animationListener.onAnimationFinished()
    }

    override fun onAnimationRepeat(animation: Animator) = Unit

    override fun onAnimationStart(animation: Animator) {
        animationListener.onAnimationStarted()
    }

    override fun setChartAnimationListener(animationListener: ChartAnimationListener) {
        this.animationListener = animationListener
    }
}
