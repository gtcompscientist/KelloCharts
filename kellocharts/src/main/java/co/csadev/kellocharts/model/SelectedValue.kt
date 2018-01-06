package co.csadev.kellocharts.model

/**
 * Holds selected values indexes, i.e. for LineChartModel it will be firstIndex=lineIndex; secondIndex=valueIndex.
 */
class SelectedValue(var firstIndex: Int = 0, var secondIndex: Int = 0, var type: SelectedValueType? = SelectedValueType.NONE) {

    /**
     * Return true if selected value have meaningful value.
     */
    val isSet: Boolean
        get() = firstIndex >= 0 && secondIndex >= 0

    operator fun set(firstIndex: Int, secondIndex: Int, type: SelectedValueType?) {
        this.firstIndex = firstIndex
        this.secondIndex = secondIndex
        if (null != type) {
            this.type = type
        } else {
            this.type = SelectedValueType.NONE
        }
    }

    fun set(selectedValue: SelectedValue) {
        this.firstIndex = selectedValue.firstIndex
        this.secondIndex = selectedValue.secondIndex
        this.type = selectedValue.type
    }

    fun clear() {
        set(Integer.MIN_VALUE, Integer.MIN_VALUE, SelectedValueType.NONE)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + firstIndex
        result = prime * result + secondIndex
        result = prime * result + if (type == null) 0 else type!!.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (javaClass != other.javaClass)
            return false
        val other = other as SelectedValue?
        if (firstIndex != other!!.firstIndex)
            return false
        if (secondIndex != other.secondIndex)
            return false
        return type == other.type
    }

    override fun toString(): String {
        return "SelectedValue [firstIndex=$firstIndex, secondIndex=$secondIndex, type=$type]"
    }

    /**
     * Used in combo chart to determine if selected value is used for line or column selection.
     */
    enum class SelectedValueType {
        NONE, LINE, COLUMN
    }

}
