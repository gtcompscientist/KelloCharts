package co.csadev.kellocharts.model

/**
 * Single axis value, use it to manually set axis labels position. You can use label attribute to display text instead
 * of number but value formatter implementation have to handle it.
 */
data class AxisValue(var value: Float = 0f, var label: CharArray? = null) {
    constructor(value: Int, label: CharArray? = null) : this(value.toFloat(), label)
    constructor(value: Int, label: String) : this(value.toFloat(), label.toCharArray())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AxisValue

        if (value != other.value) return false
        if (label != null) {
            if (other.label == null) return false
            if (!label.contentEquals(other.label)) return false
        } else if (other.label != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + (label?.contentHashCode() ?: 0)
        return result
    }
}
