package co.csadev.kellocharts.model

import java.util.Arrays

import co.csadev.kellocharts.view.Chart

/**
 * Single point coordinates, used for LineChartData.
 */
class PointValue(x: Float = 0f, y: Float = 0f, var label: CharArray? = null) {

    var x: Float = x
        private set
    var y: Float = y
        private set
    private var originX: Float = x
    private var originY: Float = y
    private var diffX: Float = x - originX
    private var diffY: Float = y - originY

    fun copy() = PointValue(x, y, label)

    fun update(scale: Float) {
        x = originX + diffX * scale
        y = originY + diffY * scale
    }

    fun finish() {
        set(originX + diffX, originY + diffY)
    }

    operator fun set(x: Float, y: Float): PointValue {
        this.x = x
        this.y = y
        this.originX = x
        this.originY = y
        this.diffX = 0f
        this.diffY = 0f
        return this
    }

    /**
     * Set target values that should be reached when data animation finish then call [Chart.startDataAnimation]
     */
    fun setTarget(targetX: Float, targetY: Float): PointValue {
        set(x, y)
        this.diffX = targetX - originX
        this.diffY = targetY - originY
        return this
    }

    override fun toString(): String {
        return "PointValue [x=$x, y=$y]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as PointValue?

        if (java.lang.Float.compare(that!!.diffX, diffX) != 0) return false
        if (java.lang.Float.compare(that.diffY, diffY) != 0) return false
        if (java.lang.Float.compare(that.originX, originX) != 0) return false
        if (java.lang.Float.compare(that.originY, originY) != 0) return false
        if (java.lang.Float.compare(that.x, x) != 0) return false
        if (java.lang.Float.compare(that.y, y) != 0) return false
        return Arrays.equals(label, that.label)

    }

    override fun hashCode(): Int {
        var result = if (x != +0.0f) java.lang.Float.floatToIntBits(x) else 0
        result = 31 * result + if (y != +0.0f) java.lang.Float.floatToIntBits(y) else 0
        result = 31 * result + if (originX != +0.0f) java.lang.Float.floatToIntBits(originX) else 0
        result = 31 * result + if (originY != +0.0f) java.lang.Float.floatToIntBits(originY) else 0
        result = 31 * result + if (diffX != +0.0f) java.lang.Float.floatToIntBits(diffX) else 0
        result = 31 * result + if (diffY != +0.0f) java.lang.Float.floatToIntBits(diffY) else 0
        result = 31 * result + if (label != null) Arrays.hashCode(label) else 0
        return result
    }
}
