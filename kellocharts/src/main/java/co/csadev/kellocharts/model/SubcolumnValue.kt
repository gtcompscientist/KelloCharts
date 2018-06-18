package co.csadev.kellocharts.model

import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart
import java.util.*

/**
 * Single sub-column value for ColumnChart.
 */
class SubcolumnValue(value: Float = 0f, color: Int = ChartUtils.DEFAULT_COLOR, var label: CharArray? = null) {

    var value: Float = value
        set (value) {
            field = value
            this.originValue = value
            this.diff = 0f
        }
    private var originValue: Float = 0.toFloat()
    private var diff: Float = 0.toFloat()
    var color: Int = color
        set (value) {
            field = value
            this.darkenColor = ChartUtils.darkenColor(field)
        }
    var darkenColor = ChartUtils.darkenColor(color)
        private set

    fun update(scale: Float) {
        value = originValue + diff * scale
    }

    fun finish() {
        value = originValue + diff
    }

    /**
     * Set target value that should be reached when data animation finish then call [Chart.startDataAnimation]
     *
     * @param target
     * @return
     */
    fun setTarget(target: Float): SubcolumnValue {
        value = value
        this.diff = target - originValue
        return this
    }

    override fun toString(): String {
        return "ColumnValue [value=$value]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as SubcolumnValue?

        if (color != that!!.color) return false
        if (darkenColor != that.darkenColor) return false
        if (java.lang.Float.compare(that.diff, diff) != 0) return false
        if (java.lang.Float.compare(that.originValue, originValue) != 0) return false
        if (java.lang.Float.compare(that.value, value) != 0) return false
        return Arrays.equals(label, that.label)

    }

    override fun hashCode(): Int {
        var result = if (value != +0.0f) java.lang.Float.floatToIntBits(value) else 0
        result = 31 * result + if (originValue != +0.0f) java.lang.Float.floatToIntBits(originValue) else 0
        result = 31 * result + if (diff != +0.0f) java.lang.Float.floatToIntBits(diff) else 0
        result = 31 * result + color
        result = 31 * result + darkenColor
        result = 31 * result + if (label != null) Arrays.hashCode(label) else 0
        return result
    }

    fun copy() = SubcolumnValue(value, color, label)
}
