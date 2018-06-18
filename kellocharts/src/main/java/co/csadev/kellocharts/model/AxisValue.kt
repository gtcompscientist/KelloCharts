package co.csadev.kellocharts.model

import java.util.*

/**
 * Single axis value, use it to manually set axis labels position. You can use label attribute to display text instead
 * of number but value formatter implementation have to handle it.
 */
class AxisValue(var value: Float = 0f, var label: CharArray? = null) {
    constructor(value: Int, label: CharArray? = null) : this(value.toFloat(), label)
    constructor(value: Int, label: String) : this(value.toFloat(), label.toCharArray())

    fun copy() = AxisValue(this.value, this.label)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val axisValue = other as AxisValue?

        if (java.lang.Float.compare(axisValue!!.value, value) != 0) return false
        return Arrays.equals(label, axisValue.label)

    }

    override fun hashCode(): Int {
        var result = if (value != +0.0f) java.lang.Float.floatToIntBits(value) else 0
        result = 31 * result + if (label != null) Arrays.hashCode(label) else 0
        return result
    }
}