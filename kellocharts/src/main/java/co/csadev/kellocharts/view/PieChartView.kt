package co.csadev.kellocharts.view

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import co.csadev.kellocharts.BuildConfig
import co.csadev.kellocharts.animation.PieChartRotationAnimator
import co.csadev.kellocharts.animation.PieChartRotationAnimatorV14
import co.csadev.kellocharts.gesture.PieChartTouchHandler
import co.csadev.kellocharts.listener.DummyPieChartOnValueSelectListener
import co.csadev.kellocharts.listener.PieChartOnValueSelectListener
import co.csadev.kellocharts.model.ChartData
import co.csadev.kellocharts.model.PieChartData
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.SliceValue
import co.csadev.kellocharts.provider.PieChartDataProvider
import co.csadev.kellocharts.renderer.PieChartRenderer

/**
 * PieChart is a little different than others charts. It doesn't have axes. It doesn't support viewport so changing
 * viewport wont work. Instead it support "Circle Oval". Pinch-to-Zoom and double tap zoom wont work either. Instead of
 * scroll there is chart rotation if isChartRotationEnabled is set to true. PieChart looks the best when it has the same
 * width and height, drawing chart on rectangle with proportions other than 1:1 will left some empty spaces.
 *
 * @author Leszek Wach
 */
class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AbstractChartView(context, attrs, defStyle), PieChartDataProvider {
    var onValueTouchListener: PieChartOnValueSelectListener = DummyPieChartOnValueSelectListener()
    protected var pieChartRenderer: PieChartRenderer = PieChartRenderer(context, this, this)
    protected var rotationAnimator: PieChartRotationAnimator

    override var pieChartData: PieChartData = PieChartData.generateDummyData()
        set(value) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Setting data for ColumnChartView")
            }
            field = value
            super.onChartDataChange()
        }

    override val chartData: ChartData
        get() = pieChartData

    /**
     * Returns rectangle that will constraint pie chart area.
     */
    /**
     * Use this to change pie chart area. Because by default CircleOval is calculated onSizeChanged() you must call this
     * method after size of PieChartView is calculated. In most cases it will probably be easier to use
     * [.setCircleFillRatio] to change chart area or just use view padding.
     */
    var circleOval: RectF
        get() = pieChartRenderer.circleOval
        set(orginCircleOval) {
            pieChartRenderer.circleOval = orginCircleOval
            ViewCompat.postInvalidateOnAnimation(this)
        }

    /**
     * Returns pie chart rotation, 0 rotation means that 0 degrees is at 3 o'clock. Don't confuse with
     * [View.getRotation].
     *
     * @return
     */
    val chartRotation: Int
        get() = pieChartRenderer.chartRotation

    /**
     * Set false if you don't wont the chart to be rotated by touch gesture. Rotating programmatically will still work.
     *
     * @param isRotationEnabled
     */
    var isChartRotationEnabled: Boolean
        get() = (touchHandler as? PieChartTouchHandler)?.isRotationEnabled ?: false
        set(isRotationEnabled) {
            (touchHandler as? PieChartTouchHandler)?.isRotationEnabled = isRotationEnabled
        }

    /**
     * @see .setCircleFillRatio
     */
    /**
     * Set how much of view area should be taken by chart circle. Value should be between 0 and 1. Default is 1 so
     * circle will have radius equals min(View.width, View.height).
     */
    var circleFillRatio: Float
        get() = pieChartRenderer.circleFillRatio
        set(fillRatio) {
            pieChartRenderer.circleFillRatio = fillRatio
            ViewCompat.postInvalidateOnAnimation(this)
        }

    init {
        touchHandler = PieChartTouchHandler(context, this)
        chartRenderer = pieChartRenderer
        this.rotationAnimator = PieChartRotationAnimatorV14(this)
        pieChartData = PieChartData.generateDummyData()
    }

    override fun callTouchListener() {
        val selectedValue = chartRenderer.selectedValue

        if (selectedValue.isSet) {
            val sliceValue = pieChartData.values[selectedValue.firstIndex]
            onValueTouchListener.onValueSelected(selectedValue.firstIndex, sliceValue)
        } else {
            onValueTouchListener.onValueDeselected()
        }
    }

    /**
     * Set pie chart rotation. Don't confuse with [View.getRotation].
     *
     * @param rotation
     * @see .getChartRotation
     */
    fun setChartRotation(rotation: Int, isAnimated: Boolean) {
        if (isAnimated) {
            rotationAnimator.cancelAnimation()
            rotationAnimator.startAnimation(
                pieChartRenderer.chartRotation.toFloat(),
                rotation.toFloat()
            )
        } else {
            pieChartRenderer.chartRotation = rotation
        }
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Returns SliceValue that is under given angle, selectedValue (if not null) will be hold slice index.
     */
    fun getValueForAngle(angle: Int, selectedValue: SelectedValue): SliceValue? {
        return pieChartRenderer.getValueForAngle(angle, selectedValue)
    }

    companion object {
        private val TAG = "PieChartView"
    }
}
