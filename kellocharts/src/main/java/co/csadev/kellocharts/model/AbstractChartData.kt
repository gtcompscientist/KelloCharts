package co.csadev.kellocharts.model

import android.graphics.Color
import android.graphics.Typeface

import co.csadev.kellocharts.util.ChartUtils

/**
 * Base class for most chart data models.
 */
abstract class AbstractChartData : ChartData {
    override var axisXBottom: Axis? = null
    override var axisYLeft: Axis? = null
    override var axisXTop: Axis? = null
    override var axisYRight: Axis? = null
    override var valueLabelTextColor = Color.WHITE
    override var valueLabelTextSize = DEFAULT_TEXT_SIZE_SP
    override var valueLabelTypeface: Typeface? = null

    /**
     * If true each value label will have background rectangle
     */
    override var isValueLabelBackgroundEnabled = true

    /**
     * If true and [.isValueLabelBackgroundEnabled] is true each label will have background rectangle and that
     * rectangle will be filled with color specified for given value.
     */
    override var isValueLabelBackgroundAuto = true

    /**
     * If [.isValueLabelBackgroundEnabled] is true and [.isValueLabelBackgrountAuto] is false each label
     * will have background rectangle and that rectangle will be filled with this color. Helpful if you want all labels
     * to have the same background color.
     */
    override var valueLabelBackgroundColor = ChartUtils.darkenColor(ChartUtils.DEFAULT_DARKEN_COLOR)

    fun deepCopy(newItem: AbstractChartData) {
        newItem.axisXBottom = axisXBottom
        newItem.axisXTop = axisXTop
        newItem.axisYLeft = axisYLeft
        newItem.axisYRight = axisYRight
        newItem.valueLabelTextColor = valueLabelTextColor
        newItem.valueLabelTextSize = valueLabelTextSize
        newItem.valueLabelTypeface = valueLabelTypeface
    }

    fun withData(original: AbstractChartData) : AbstractChartData {
        axisXBottom = original.axisXBottom
        axisXTop = original.axisXTop
        axisYLeft = original.axisYLeft
        axisYRight = original.axisYRight
        valueLabelTextColor = original.valueLabelTextColor
        valueLabelTextSize = original.valueLabelTextSize
        valueLabelTypeface = original.valueLabelTypeface
        return this
    }

    companion object {
        val DEFAULT_TEXT_SIZE_SP = 12
    }

}