package co.csadev.kellocharts.view.hack

import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Hacky fix for issue with DrawerLayout https://github.com/chrisbanes/PhotoView/issues/72
 */
class HackyDrawerLayout : androidx.drawerlayout.widget.DrawerLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

}
