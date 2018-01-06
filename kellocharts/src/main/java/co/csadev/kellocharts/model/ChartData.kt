package co.csadev.kellocharts.model

import android.graphics.Typeface

/**
 * Base interface for all chart data models.
 */
interface ChartData {

    /**
     * @see .setAxisXBottom
     */
    /**
     * Set horizontal axis at the bottom of the chart. Pass null to remove that axis.
     *
     * @param axisX
     */
    var axisXBottom: Axis?

    /**
     * @see .setAxisYLeft
     */
    /**
     * Set vertical axis on the left of the chart. Pass null to remove that axis.
     *
     * @param axisY
     */
    var axisYLeft: Axis?

    /**
     * @see .setAxisXTop
     */
    /**
     * Set horizontal axis at the top of the chart. Pass null to remove that axis.
     *
     * @param axisX
     */
    var axisXTop: Axis?

    /**
     * @see .setAxisYRight
     */
    /**
     * Set vertical axis on the right of the chart. Pass null to remove that axis.
     *
     * @param axisY
     */
    var axisYRight: Axis?

    /**
     * Returns color used to draw value label text.
     */
    var valueLabelTextColor: Int

    /**
     * Returns text size for value label in SP units.
     */
    /**
     * Set text size for value label in SP units.
     */
    var valueLabelTextSize: Int

    /**
     * Returns Typeface for value labels.
     *
     * @return Typeface or null if Typeface is not set.
     */
    /**
     * Set Typeface for all values labels.
     *
     * @param typeface
     */
    var valueLabelTypeface: Typeface?

    /**
     * @see .setValueLabelBackgroundEnabled
     */
    /**
     * Set whether labels should have rectangle background. Default is true.
     */
    var isValueLabelBackgroundEnabled: Boolean

    /**
     * @see .setValueLabelBackgroundAuto
     */
    /**
     * Set false if you want to set custom color for all value labels. Default is true.
     */
    var isValueLabelBackgroundAuto: Boolean

    /**
     * @see .setValueLabelBackgroundColor
     */
    /**
     * Set value labels background. This value is used only if isValueLabelBackgroundAuto returns false. Default is
     * green.
     */
    var valueLabelBackgroundColor: Int

    /**
     * Updates data by scale during animation.
     *
     * @param scale value from 0 to 1.0
     */
    fun update(scale: Float)

    /**
     * Inform data that animation finished(data should be update with scale 1.0f).
     */
    fun finish()
}
