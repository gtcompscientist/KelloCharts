package co.csadev.kellocharts.model

import java.util.Arrays

import co.csadev.kellocharts.util.ChartUtils
import co.csadev.kellocharts.view.Chart

/**
 * Single value drawn as bubble on BubbleChart.
 */
class BubbleValue(x: Float = 0f, y: Float = 0f, z: Float = 0f, color: Int = ChartUtils.DEFAULT_COLOR, var label: CharArray? = null, var shape: ValueShape? = ValueShape.CIRCLE) {
    /**
     * Current X value.
     */
    var x: Float = x
        private set
    /**
     * Current Y value.
     */
    var y: Float = y
        private set
    /**
     * Current Z value , third bubble value interpreted as bubble area.
     */
    var z: Float = z
        private set

    var color = color
        set(value) {
            field = value
            darkenColor = ChartUtils.darkenColor(field)
        }
    var darkenColor = ChartUtils.DEFAULT_DARKEN_COLOR
        private set

    /**
     * Origin X value, used during value animation.
     */
    private var originX: Float = x
    /**
     * Origin Y value, used during value animation.
     */
    private var originY: Float = y
    /**
     * Origin Z value, used during value animation.
     */
    private var originZ: Float = z

    /**
     * Difference between originX value and target X value.
     */
    private var diffX: Float = x - originX

    /**
     * Difference between originX value and target X value.
     */
    private var diffY: Float = y - originY

    /**
     * Difference between originX value and target X value.
     */
    private var diffZ: Float = z - originZ

    fun copy() = BubbleValue(x, y, z, color, label)

    fun update(scale: Float) {
        x = originX + diffX * scale
        y = originY + diffY * scale
        z = originZ + diffZ * scale
    }

    fun finish() {
        set(originX + diffX, originY + diffY, originZ + diffZ)
    }

    operator fun set(x: Float, y: Float, z: Float): BubbleValue {
        this.x = x
        this.y = y
        this.z = z
        this.originX = x
        this.originY = y
        this.originZ = z
        this.diffX = 0f
        this.diffY = 0f
        this.diffZ = 0f
        return this
    }

    /**
     * Set target values that should be reached when data animation finish then call [Chart.startDataAnimation]
     */
    fun setTarget(targetX: Float, targetY: Float, targetZ: Float): BubbleValue {
        set(x, y, z)
        this.diffX = targetX - originX
        this.diffY = targetY - originY
        this.diffZ = targetZ - originZ
        return this
    }

    override fun toString(): String {
        return "BubbleValue [x=$x, y=$y, z=$z]"
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as BubbleValue?

        if (color != that!!.color) return false
        if (darkenColor != that.darkenColor) return false
        if (java.lang.Float.compare(that.diffX, diffX) != 0) return false
        if (java.lang.Float.compare(that.diffY, diffY) != 0) return false
        if (java.lang.Float.compare(that.diffZ, diffZ) != 0) return false
        if (java.lang.Float.compare(that.originX, originX) != 0) return false
        if (java.lang.Float.compare(that.originY, originY) != 0) return false
        if (java.lang.Float.compare(that.originZ, originZ) != 0) return false
        if (java.lang.Float.compare(that.x, x) != 0) return false
        if (java.lang.Float.compare(that.y, y) != 0) return false
        if (java.lang.Float.compare(that.z, z) != 0) return false
        if (!Arrays.equals(label, that.label)) return false
        return shape === that.shape

    }

    override fun hashCode(): Int {
        var result = if (x != +0.0f) java.lang.Float.floatToIntBits(x) else 0
        result = 31 * result + if (y != +0.0f) java.lang.Float.floatToIntBits(y) else 0
        result = 31 * result + if (z != +0.0f) java.lang.Float.floatToIntBits(z) else 0
        result = 31 * result + if (originX != +0.0f) java.lang.Float.floatToIntBits(originX) else 0
        result = 31 * result + if (originY != +0.0f) java.lang.Float.floatToIntBits(originY) else 0
        result = 31 * result + if (originZ != +0.0f) java.lang.Float.floatToIntBits(originZ) else 0
        result = 31 * result + if (diffX != +0.0f) java.lang.Float.floatToIntBits(diffX) else 0
        result = 31 * result + if (diffY != +0.0f) java.lang.Float.floatToIntBits(diffY) else 0
        result = 31 * result + if (diffZ != +0.0f) java.lang.Float.floatToIntBits(diffZ) else 0
        result = 31 * result + color
        result = 31 * result + darkenColor
        result = 31 * result + if (shape != null) shape!!.hashCode() else 0
        result = 31 * result + if (label != null) Arrays.hashCode(label) else 0
        return result
    }
}
