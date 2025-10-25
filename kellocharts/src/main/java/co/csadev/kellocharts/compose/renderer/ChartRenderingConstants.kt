package co.csadev.kellocharts.compose.renderer

/**
 * Rendering constants for chart drawing.
 *
 * This object centralizes all magic numbers used across chart renderers to:
 * - **Improve maintainability**: Single source of truth for rendering parameters
 * - **Ensure consistency**: Same values used across all chart types
 * - **Simplify adjustments**: Change once, apply everywhere
 *
 * ## Usage
 *
 * ```kotlin
 * val touchTolerance = ChartRenderingConstants.TOUCH_TOLERANCE_DP.dp.toPx()
 * val cornerRadius = ChartRenderingConstants.COLUMN_CORNER_RADIUS_DP.dp.toPx()
 * ```
 *
 * @see ComposeLineChartRenderer
 * @see ComposeColumnChartRenderer
 * @see ComposeBubbleChartRenderer
 * @see ComposeAxesRenderer
 */
object ChartRenderingConstants {
    /**
     * Touch tolerance radius for value selection (dp).
     *
     * This determines the hit area for selecting data points via touch/click.
     * 24dp provides a comfortable touch target per Material Design guidelines.
     */
    const val TOUCH_TOLERANCE_DP = 24

    /**
     * Default column corner radius (dp).
     *
     * Applies to bar/column charts for rounded corners at the top of bars.
     * 2dp provides subtle rounding without excessive visual softness.
     */
    const val COLUMN_CORNER_RADIUS_DP = 2

    /**
     * Default bubble base size (dp).
     *
     * This is the maximum diameter for bubbles when Z value equals maxBubbleZ.
     * Actual bubble size scales proportionally based on Z value and bubbleScale.
     * 50dp provides good visibility without overwhelming the chart.
     */
    const val BUBBLE_BASE_SIZE_DP = 50

    /**
     * Horizontal offset for axis labels (dp).
     *
     * Used to provide spacing between axis lines and label text.
     * 8dp provides comfortable breathing room.
     */
    const val AXIS_LABEL_OFFSET_X_DP = 8

    /**
     * Vertical offset for axis labels (dp).
     *
     * Used to provide spacing between axis lines and label text.
     * 16dp provides adequate vertical spacing.
     */
    const val AXIS_LABEL_OFFSET_Y_DP = 16

    /**
     * Grid line opacity.
     *
     * Subtle grid lines that don't overpower the data visualization.
     * 0.2 alpha provides visibility without distraction.
     */
    const val GRID_LINE_ALPHA = 0.2f

    /**
     * Default line stroke width (dp).
     *
     * Used for line charts when no specific stroke width is provided.
     * 2dp provides clear visibility on most screen densities.
     */
    const val DEFAULT_STROKE_WIDTH_DP = 2

    /**
     * Default point marker radius (dp).
     *
     * Used for point markers in line charts when no specific radius is provided.
     * 4dp provides clear visibility without being too large.
     */
    const val DEFAULT_POINT_RADIUS_DP = 4
}
