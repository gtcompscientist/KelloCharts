package co.csadev.kellocharts.util

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import java.lang.Math.random
import kotlin.math.ceil
import kotlin.math.roundToInt

const val THREE_SIXTY = 360
const val THREE_SIXTY_F = 360f
private const val COLOR_SEGMENTS = 3

object ChartUtils {

    val DEFAULT_COLOR = Color.parseColor("#DFDFDF")
    val DEFAULT_DARKEN_COLOR = Color.parseColor("#DDDDDD")
    val COLOR_BLUE = Color.parseColor("#33B5E5")
    val COLOR_VIOLET = Color.parseColor("#AA66CC")
    val COLOR_GREEN = Color.parseColor("#99CC00")
    val COLOR_ORANGE = Color.parseColor("#FFBB33")
    val COLOR_RED = Color.parseColor("#FF4444")
    val COLORS = intArrayOf(COLOR_BLUE, COLOR_VIOLET, COLOR_GREEN, COLOR_ORANGE, COLOR_RED)
    private const val DARKEN_SATURATION = 1.1f
    private const val DARKEN_INTENSITY = 0.9f
    private var COLOR_INDEX = 0

    fun pickColor(): Int {
        return COLORS[(random() * (COLORS.size - 1)).roundToInt()]
    }

    fun nextColor(): Int {
        if (COLOR_INDEX >= COLORS.size) {
            COLOR_INDEX = 0
        }
        return COLORS[COLOR_INDEX++]
    }

    fun Int.dp2px(density: Float): Int = if (this == 0) 0 else (this * density + 0.5f).toInt()

    fun px2dp(density: Float, px: Int): Int = ceil((px / density).toDouble()).toInt()

    fun sp2px(scaledDensity: Float, sp: Int): Int {
        return if (sp == 0) {
            0
        } else (sp * scaledDensity + 0.5f).toInt()
    }

    fun px2sp(scaledDensity: Float, px: Int): Int {
        return ceil((px / scaledDensity).toDouble()).toInt()
    }

    fun mm2px(context: Context, mm: Int): Int {
        return (
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_MM, mm.toFloat(),
                context.resources
                    .displayMetrics
            ) + 0.5f
            ).toInt()
    }

    fun Int.darken(): Int {
        val hsv = FloatArray(COLOR_SEGMENTS)
        val alpha = Color.alpha(this)
        Color.colorToHSV(this, hsv)
        hsv[1] = (hsv[1] * DARKEN_SATURATION).coerceAtMost(1.0f)
        hsv[2] = hsv[2] * DARKEN_INTENSITY
        val tempColor = Color.HSVToColor(hsv)
        return Color.argb(
            alpha,
            Color.red(tempColor),
            Color.green(tempColor),
            Color.blue(tempColor)
        )
    }
}
