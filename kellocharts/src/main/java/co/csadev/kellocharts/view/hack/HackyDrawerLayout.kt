package co.csadev.kellocharts.view.hack

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.drawerlayout.widget.DrawerLayout

/**
 * Hacky fix for issue with DrawerLayout https://github.com/chrisbanes/PhotoView/issues/72
 */
class HackyDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DrawerLayout(context, attrs, defStyleAttr) {
    override fun onInterceptTouchEvent(ev: MotionEvent) = try {
        super.onInterceptTouchEvent(ev)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
