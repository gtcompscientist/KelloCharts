package co.csadev.kellocharts.compose.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport

/**
 * Base interface for Compose-based chart renderers.
 *
 * ## Key Differences: DrawScope vs Android Canvas API
 *
 * ### Coordinate System
 * - **DrawScope**: Uses Float-based coordinates (more precise for modern displays)
 * - **Canvas**: Uses legacy int-based coordinates with some float support
 *
 * ### Drawing Primitives
 * - **DrawScope.drawLine()**: Similar to Canvas.drawLine() but with compose.ui.graphics types
 * - **DrawScope.drawPath()**: Similar to Canvas.drawPath() but uses androidx.compose.ui.graphics.Path
 * - **DrawScope.drawCircle()**: Similar to Canvas.drawCircle()
 * - **DrawScope.drawRect()**: Similar to Canvas.drawRect()
 * - **DrawScope.drawArc()**: Similar to Canvas.drawArc()
 *
 * ### Paint vs DrawStyle
 * - **Canvas**: Uses android.graphics.Paint for styling
 * - **DrawScope**: Uses inline parameters (color, strokeWidth, style, etc.)
 * - No need to create and manage Paint objects in Compose
 *
 * ### Text Rendering
 * - **Canvas**: Uses Paint.setTextSize(), Canvas.drawText()
 * - **DrawScope**: Uses drawContext.canvas.nativeCanvas for text (temporarily)
 *   or TextMeasurer for better Compose integration
 *
 * ### Paths
 * - **Canvas**: Uses android.graphics.Path
 * - **DrawScope**: Uses androidx.compose.ui.graphics.Path
 * - API is similar but packages differ
 *
 * ### Transformations
 * - **Canvas**: save(), translate(), rotate(), scale(), restore()
 * - **DrawScope**: withTransform { translate(), rotate(), scale() }
 * - Compose automatically manages save/restore
 *
 * ### Clipping
 * - **Canvas**: clipRect(), clipPath()
 * - **DrawScope**: clipRect(), clipPath() with similar APIs
 *
 * @see DrawScope
 */
interface ComposeChartRenderer {

    /**
     * Called when the size of the chart changes.
     * Use this to recalculate layout-dependent values.
     */
    fun onSizeChanged(size: Size, contentRect: Rect)

    /**
     * Called when the chart data changes.
     * Use this to update cached values and trigger recomposition.
     */
    fun onDataChanged()

    /**
     * Called when the viewport changes (zoom/scroll).
     * Use this to recalculate visible data points.
     */
    fun onViewportChanged(viewport: Viewport)

    /**
     * Main drawing function called during composition.
     *
     * @param drawScope The DrawScope to draw into
     * @param size The size of the drawing area
     * @param viewport The current viewport (visible area)
     */
    fun draw(drawScope: DrawScope, size: Size, viewport: Viewport)

    /**
     * Calculate which value (if any) is at the given position.
     * Used for touch/click handling.
     *
     * @param position The position to check
     * @return SelectedValue if a value is found, null otherwise
     */
    fun getValueAtPosition(position: Offset): SelectedValue?
}
