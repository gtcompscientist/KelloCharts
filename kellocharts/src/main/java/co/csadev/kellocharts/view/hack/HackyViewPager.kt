package co.csadev.kellocharts.view.hack

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * ScaleGestureDetector seems to mess up the touch events, which means that ViewGroups which make use of
 * onInterceptTouchEvent throw a lot of IllegalArgumentException: pointerIndex out of range.There's not much I can do
 * in my code for now, but we can mask the result by just catching the problem and ignoring
 * it.
 *
 * @author Chris Banes
 */
class HackyViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
