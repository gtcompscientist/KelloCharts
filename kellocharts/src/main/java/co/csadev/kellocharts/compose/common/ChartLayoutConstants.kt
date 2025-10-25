package co.csadev.kellocharts.compose.common

/**
 * Layout constants used across chart components.
 *
 * This object centralizes all chart layout measurements to:
 * - **Improve maintainability**: Single source of truth for spacing and margins
 * - **Ensure consistency**: Same layout behavior across all chart types
 * - **Simplify adjustments**: Change once, apply to LineChart, ColumnChart, and BubbleChart
 *
 * These constants define the chart content area margins based on whether axes are present.
 * Larger margins are needed when axes are present to accommodate axis labels and tick marks.
 *
 * ## Usage
 *
 * ```kotlin
 * val marginLeft = if (data.axisYLeft != null) {
 *     ChartLayoutConstants.MARGIN_WITH_AXIS
 * } else {
 *     ChartLayoutConstants.MARGIN_WITHOUT_AXIS
 * }
 * ```
 *
 * @see LineChart
 * @see ColumnChart
 * @see BubbleChart
 */
object ChartLayoutConstants {
    /**
     * Margin when Y axis (left or right) is present (dp).
     *
     * This larger margin accommodates:
     * - Axis line width
     * - Tick marks
     * - Axis value labels (numbers or custom text)
     * - Padding between labels and edge
     *
     * 60dp provides sufficient space for most axis label configurations.
     */
    const val MARGIN_WITH_AXIS = 60f

    /**
     * Margin when axis is absent (dp).
     *
     * This minimal margin provides basic padding from the chart edges.
     * 10dp gives breathing room without wasting space.
     */
    const val MARGIN_WITHOUT_AXIS = 10f

    /**
     * Top margin when X axis is present (dp).
     *
     * Smaller than side margins because top axis labels are typically
     * more compact. 40dp accommodates axis labels and tick marks.
     */
    const val MARGIN_TOP_WITH_AXIS = 40f

    /**
     * Bottom margin when X axis is present (dp).
     *
     * Smaller than side margins because bottom axis labels are typically
     * more compact. 40dp accommodates axis labels and tick marks.
     */
    const val MARGIN_BOTTOM_WITH_AXIS = 40f

    /**
     * Viewport padding as percentage of data range.
     *
     * When auto-calculating viewport from data, this adds 10% padding
     * on each side to prevent data points from touching the chart edges.
     * 0.1 = 10% padding.
     */
    const val VIEWPORT_PADDING_RATIO = 0.1f
}
