package co.csadev.kellocharts.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import co.csadev.kellocharts.compose.animation.ChartAnimationDefaults
import co.csadev.kellocharts.compose.animation.animatePieRotation
import co.csadev.kellocharts.compose.animation.rememberChartDataAnimation
import co.csadev.kellocharts.compose.gesture.chartValueSelection
import co.csadev.kellocharts.compose.renderer.ComposePieChartRenderer
import co.csadev.kellocharts.model.PieChartData
import co.csadev.kellocharts.model.SelectedValue
import co.csadev.kellocharts.model.Viewport
import kotlin.math.atan2

/**
 * A Compose-based pie chart component.
 *
 * Displays a circular chart with slices representing proportional data.
 * Supports donut charts (with center circle), rotation, and slice selection.
 *
 * ## Features
 * - **Pie chart**: Classic circular chart with slices
 * - **Donut chart**: Hollow center circle for modern look
 * - **Slice separation**: Visual spacing between slices
 * - **Rotation**: Rotate entire chart or individual slices
 * - **Gesture rotation**: Drag to rotate the chart
 * - **Selection**: Tap to select slices
 * - **Animations**: Smooth rotation and appearance
 *
 * ## Basic Usage
 * ```kotlin
 * val data = remember {
 *     PieChartData(
 *         values = listOf(
 *             SliceValue(40f, Color.Red.toArgb()),
 *             SliceValue(30f, Color.Blue.toArgb()),
 *             SliceValue(20f, Color.Green.toArgb()),
 *             SliceValue(10f, Color.Yellow.toArgb())
 *         )
 *     )
 * }
 *
 * PieChart(
 *     data = data,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * ## Donut Chart
 * ```kotlin
 * val data = remember {
 *     PieChartData(
 *         values = listOf(/* slices */),
 *         hasCenterCircle = true,
 *         centerCircleScale = 0.6f
 *     )
 * }
 *
 * PieChart(
 *     data = data,
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * ## With Rotation
 * ```kotlin
 * var rotation by remember { mutableStateOf(0f) }
 *
 * PieChart(
 *     data = data,
 *     rotation = rotation,
 *     rotationEnabled = true,
 *     onRotationChange = { newRotation ->
 *         rotation = newRotation
 *     },
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * @param data The pie chart data to display
 * @param modifier Modifier to be applied to the chart
 * @param rotation Current rotation angle in degrees
 * @param rotationEnabled Whether drag-to-rotate gesture is enabled
 * @param selectionEnabled Whether tap-to-select gesture is enabled
 * @param animate Whether to animate initial chart appearance
 * @param onRotationChange Callback when rotation changes via gesture
 * @param onValueSelected Callback when a slice is selected via tap
 * @param onValueDeselected Callback when selection is cleared
 */
@Composable
fun PieChart(
    data: PieChartData,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    rotationEnabled: Boolean = false,
    selectionEnabled: Boolean = true,
    animate: Boolean = true,
    onRotationChange: ((Float) -> Unit)? = null,
    onValueSelected: ((SelectedValue) -> Unit)? = null,
    onValueDeselected: (() -> Unit)? = null
) {
    // Animated rotation
    val animatedRotation by animatePieRotation(
        targetRotation = rotation,
        animationSpec = ChartAnimationDefaults.spring
    )

    // Animation progress for initial appearance
    val animationProgress = if (animate) {
        rememberChartDataAnimation(
            animationSpec = ChartAnimationDefaults.tween
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    // Create renderer
    val renderer = remember(data) {
        ComposePieChartRenderer(data, animatedRotation)
    }

    // Update renderer rotation
    renderer.updateRotation(animatedRotation)

    // Track center for rotation gesture
    var centerOffset by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (selectionEnabled) {
                    Modifier.chartValueSelection(
                        onValueSelected = { value ->
                            onValueSelected?.invoke(value)
                        },
                        onValueDeselected = onValueDeselected,
                        getValueAtPosition = { offset ->
                            renderer.getValueAtPosition(offset)
                        }
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (rotationEnabled && onRotationChange != null) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            // Calculate rotation angle from drag
                            val center = centerOffset
                            val before = atan2(
                                change.position.y - dragAmount.y - center.y,
                                change.position.x - dragAmount.x - center.x
                            )
                            val after = atan2(
                                change.position.y - center.y,
                                change.position.x - center.x
                            )

                            val deltaAngle = Math.toDegrees((after - before).toDouble()).toFloat()
                            onRotationChange(rotation + deltaAngle)
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        val contentRect = calculateContentRect(size)
        centerOffset = contentRect.center

        // Update renderer size
        renderer.onSizeChanged(size, contentRect)

        // Draw chart with animation progress
        if (animationProgress.value > 0f) {
            drawIntoCanvas { canvas ->
                renderer.draw(this, size, Viewport()) // Pie chart doesn't use viewport
            }
        }
    }
}

/**
 * Calculate the content rectangle (chart drawing area).
 * Pie chart uses the full available space.
 */
private fun calculateContentRect(size: Size): Rect {
    val margin = 20f
    return Rect(
        left = margin,
        top = margin,
        right = size.width - margin,
        bottom = size.height - margin
    )
}
