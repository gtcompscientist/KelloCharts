package co.csadev.kellocharts.util

import android.content.Context
import android.graphics.Color
import android.util.TypedValue

object ChartUtils {

    val DEFAULT_COLOR = Color.parseColor("#DFDFDF")
    val DEFAULT_DARKEN_COLOR = Color.parseColor("#DDDDDD")
    val COLOR_BLUE = Color.parseColor("#33B5E5")
    val COLOR_VIOLET = Color.parseColor("#AA66CC")
    val COLOR_GREEN = Color.parseColor("#99CC00")
    val COLOR_ORANGE = Color.parseColor("#FFBB33")
    val COLOR_RED = Color.parseColor("#FF4444")
    val COLORS = intArrayOf(COLOR_BLUE, COLOR_VIOLET, COLOR_GREEN, COLOR_ORANGE, COLOR_RED)
    private val DARKEN_SATURATION = 1.1f
    private val DARKEN_INTENSITY = 0.9f
    private var COLOR_INDEX = 0

    fun pickColor(): Int {
        return COLORS[Math.round(Math.random() * (COLORS.size - 1)).toInt()]
    }

    fun nextColor(): Int {
        if (COLOR_INDEX >= COLORS.size) {
            COLOR_INDEX = 0
        }
        return COLORS[COLOR_INDEX++]
    }

    fun dp2px(density: Float, dp: Int): Int {
        return if (dp == 0) {
            0
        } else (dp * density + 0.5f).toInt()

    }

    fun px2dp(density: Float, px: Int): Int {
        return Math.ceil((px / density).toDouble()).toInt()
    }

    fun sp2px(scaledDensity: Float, sp: Int): Int {
        return if (sp == 0) {
            0
        } else (sp * scaledDensity + 0.5f).toInt()
    }

    fun px2sp(scaledDensity: Float, px: Int): Int {
        return Math.ceil((px / scaledDensity).toDouble()).toInt()
    }

    fun mm2px(context: Context, mm: Int): Int {
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mm.toFloat(), context.resources
                .displayMetrics) + 0.5f).toInt()
    }

    fun darkenColor(color: Int): Int {
        val hsv = FloatArray(3)
        val alpha = Color.alpha(color)
        Color.colorToHSV(color, hsv)
        hsv[1] = Math.min(hsv[1] * DARKEN_SATURATION, 1.0f)
        hsv[2] = hsv[2] * DARKEN_INTENSITY
        val tempColor = Color.HSVToColor(hsv)
        return Color.argb(alpha, Color.red(tempColor), Color.green(tempColor), Color.blue(tempColor))
    }

}
