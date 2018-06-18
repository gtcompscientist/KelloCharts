package co.csadev.kellocharts.model

import co.csadev.kellocharts.view.Chart
import java.util.*

/**
 * Single point coordinates, used for LineChartData.
 */
class PointValue(x: Float = 0f, y: Float = 0f, var label: CharArray? = null) {
    constructor(x: Int, y: Int, label: CharArray? = null) : this(x.toFloat(), y.toFloat(), label)
    constructor(x: Int, y: Int, label: String) : this(x.toFloat(), y.toFloat(), label.toCharArray())
    constructor(x: Float, y: Float, label: String) : this(x, y, label.toCharArray())

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

        if (that!!.diffX.compareTo(diffX) != 0) return false
        if (that.diffY.compareTo(diffY) != 0) return false
        if (that.originX.compareTo(originX) != 0) return false
        if (that.originY.compareTo(originY) != 0) return false
        if (that.x.compareTo(x) != 0) return false
        if (that.y.compareTo(y) != 0) return false
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
