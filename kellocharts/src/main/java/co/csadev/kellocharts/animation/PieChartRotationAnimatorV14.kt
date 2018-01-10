package co.csadev.kellocharts.animation

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint

import co.csadev.kellocharts.view.PieChartView

@SuppressLint("NewApi")
class PieChartRotationAnimatorV14 @JvmOverloads constructor(private val chart: PieChartView, duration: Long = 200L) : PieChartRotationAnimator, AnimatorListener, AnimatorUpdateListener {
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
        this.startRotation = (startAngle % 360 + 360) % 360
        this.targetRotation = (angleToRotate % 360 + 360) % 360
        animator.start()
    }

    override fun cancelAnimation() {
        animator.cancel()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val scale = animation.animatedFraction
        var rotation = startRotation + (targetRotation - startRotation) * scale
        rotation = (rotation % 360 + 360) % 360
        chart.setChartRotation(rotation.toInt(), false)
    }

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationEnd(animation: Animator) {
        chart.setChartRotation(targetRotation.toInt(), false)
        animationListener.onAnimationFinished()
    }

    override fun onAnimationRepeat(animation: Animator) {}

    override fun onAnimationStart(animation: Animator) {
        animationListener.onAnimationStarted()
    }

    override fun setChartAnimationListener(animationListener: ChartAnimationListener?) {
        if (null == animationListener) {
            this.animationListener = DummyChartAnimationListener()
        } else {
            this.animationListener = animationListener
        }
    }

}
