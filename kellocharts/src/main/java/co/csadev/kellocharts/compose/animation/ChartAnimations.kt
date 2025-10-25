package co.csadev.kellocharts.compose.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import co.csadev.kellocharts.model.BubbleChartData
import co.csadev.kellocharts.model.ColumnChartData
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PieChartData
import co.csadev.kellocharts.model.Viewport
import kotlinx.coroutines.launch

/**
 * Default animation specifications for charts.
 */
object ChartAnimationDefaults {
    /**
     * Default spring animation for smooth, natural motion.
     */
    val spring: SpringSpec<Float> = spring(
        dampingRatio = 0.8f,
        stiffness = 300f
    )

    /**
     * Default tween animation for consistent timing.
     */
    val tween: TweenSpec<Float> = tween(
        durationMillis = 500
    )

    /**
     * Fast animation for quick transitions.
     */
    val fast: TweenSpec<Float> = tween(
        durationMillis = 200
    )

    /**
     * Slow animation for emphasis.
     */
    val slow: TweenSpec<Float> = tween(
        durationMillis = 800
    )
}

/**
 * Animate a Float value with the given animation spec.
 *
 * ## Usage Example
 * ```kotlin
 * val animatedValue = animateFloatAsState(
 *     targetValue = targetValue,
 *     animationSpec = ChartAnimationDefaults.spring
 * )
 * ```
 *
 * @param targetValue The target value to animate to
 * @param animationSpec The animation specification
 * @param label Debug label for the animation
 * @return Animated state value
 */
@Composable
fun animateFloatAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = ChartAnimationDefaults.spring,
    label: String = "FloatAnimation"
): State<Float> {
    return androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = label
    )
}

/**
 * Animate viewport changes.
 *
 * Creates smooth transitions when the viewport changes (zoom/pan).
 *
 * ## Usage Example
 * ```kotlin
 * val animatedViewport = animateViewport(
 *     targetViewport = targetViewport,
 *     animationSpec = ChartAnimationDefaults.spring
 * )
 * ```
 *
 * @param targetViewport The target viewport to animate to
 * @param animationSpec The animation specification
 * @return Animated viewport state
 */
@Composable
fun animateViewport(
    targetViewport: Viewport,
    animationSpec: AnimationSpec<Float> = ChartAnimationDefaults.spring
): State<Viewport> {
    val animatedLeft = animateFloatAsState(
        targetValue = targetViewport.left,
        animationSpec = animationSpec,
        label = "ViewportLeft"
    )
    val animatedTop = animateFloatAsState(
        targetValue = targetViewport.top,
        animationSpec = animationSpec,
        label = "ViewportTop"
    )
    val animatedRight = animateFloatAsState(
        targetValue = targetViewport.right,
        animationSpec = animationSpec,
        label = "ViewportRight"
    )
    val animatedBottom = animateFloatAsState(
        targetValue = targetViewport.bottom,
        animationSpec = animationSpec,
        label = "ViewportBottom"
    )

    return derivedStateOf {
        Viewport(
            animatedLeft.value,
            animatedTop.value,
            animatedRight.value,
            animatedBottom.value
        )
    }
}

/**
 * Animate chart data appearance.
 *
 * Creates a fade-in or scale animation when chart data first appears.
 *
 * ## Usage Example
 * ```kotlin
 * val progress = rememberChartDataAnimation(
 *     animationSpec = ChartAnimationDefaults.tween
 * )
 *
 * Canvas(modifier = Modifier.fillMaxSize()) {
 *     // Scale elements based on progress
 *     drawCircle(
 *         radius = baseRadius * progress.value,
 *         // ...
 *     )
 * }
 * ```
 *
 * @param animationSpec The animation specification
 * @param startDelay Delay before starting animation (milliseconds)
 * @return Animation progress from 0f to 1f
 */
@Composable
fun rememberChartDataAnimation(
    animationSpec: AnimationSpec<Float> = ChartAnimationDefaults.tween,
    startDelay: Int = 0
): State<Float> {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (startDelay > 0) {
            kotlinx.coroutines.delay(startDelay.toLong())
        }
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = animationSpec
        )
    }

    return animatable.asState()
}

/**
 * Animate sequential chart elements.
 *
 * Creates staggered animations where each element starts after a delay.
 * Useful for animating chart columns, bars, or data points one by one.
 *
 * ## Usage Example
 * ```kotlin
 * val progress = rememberSequentialAnimation(
 *     count = dataPoints.size,
 *     animationSpec = ChartAnimationDefaults.tween,
 *     staggerDelay = 50
 * )
 *
 * dataPoints.forEachIndexed { index, point ->
 *     val elementProgress = progress.value[index]
 *     // Draw with elementProgress (0f to 1f)
 * }
 * ```
 *
 * @param count Number of elements to animate
 * @param animationSpec The animation specification for each element
 * @param staggerDelay Delay between each element start (milliseconds)
 * @return List of animation progress values (0f to 1f) for each element
 */
@Composable
fun rememberSequentialAnimation(
    count: Int,
    animationSpec: AnimationSpec<Float> = ChartAnimationDefaults.fast,
    staggerDelay: Int = 50
): State<List<Float>> {
    val animatables = remember(count) {
        List(count) { Animatable(0f) }
    }

    LaunchedEffect(count) {
        animatables.forEachIndexed { index, animatable ->
            launch {
                kotlinx.coroutines.delay((index * staggerDelay).toLong())
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = animationSpec
                )
            }
        }
    }

    return remember(animatables) {
        mutableStateOf(animatables.map { it.value })
    }
}

/**
 * Animate value changes in chart data.
 *
 * Interpolates between old and new values when data changes.
 *
 * ## Usage Example
 * ```kotlin
 * val animatedValue = animateValueChange(
 *     targetValue = newValue,
 *     animationSpec = ChartAnimationDefaults.spring
 * )
 * ```
 *
 * @param targetValue The new value to animate to
 * @param animationSpec The animation specification
 * @return Animated value
 */
@Composable
fun animateValueChange(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = ChartAnimationDefaults.spring
): State<Float> {
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "ValueChange"
    )
}

/**
 * Animate offset (position) changes.
 *
 * Useful for animating point positions in scatter or line charts.
 *
 * ## Usage Example
 * ```kotlin
 * val animatedOffset = animateOffsetAsState(
 *     targetOffset = Offset(x, y),
 *     animationSpec = ChartAnimationDefaults.spring
 * )
 * ```
 *
 * @param targetOffset The target offset to animate to
 * @param animationSpec The animation specification
 * @return Animated offset state
 */
@Composable
fun animateOffsetAsState(
    targetOffset: Offset,
    animationSpec: AnimationSpec<Offset> = spring()
): State<Offset> {
    return androidx.compose.animation.core.animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = animationSpec,
        label = "OffsetAnimation"
    )
}

/**
 * Animation configuration for pie chart rotation.
 *
 * @param initialRotation Initial rotation angle in degrees
 * @param animationSpec Animation specification for rotation changes
 */
data class PieRotationConfig(
    val initialRotation: Float = 0f,
    val animationSpec: AnimationSpec<Float> = ChartAnimationDefaults.spring
)

/**
 * Animate pie chart rotation.
 *
 * ## Usage Example
 * ```kotlin
 * var targetRotation by remember { mutableStateOf(0f) }
 * val animatedRotation = animatePieRotation(targetRotation)
 *
 * // Later: targetRotation = 45f (animates smoothly)
 * ```
 *
 * @param targetRotation Target rotation angle in degrees
 * @param animationSpec Animation specification
 * @return Animated rotation state
 */
@Composable
fun animatePieRotation(
    targetRotation: Float,
    animationSpec: AnimationSpec<Float> = ChartAnimationDefaults.spring
): State<Float> {
    return animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = animationSpec,
        label = "PieRotation"
    )
}

/**
 * Pulse animation for highlighting selected values.
 *
 * Creates a repeating scale animation to draw attention.
 *
 * ## Usage Example
 * ```kotlin
 * val pulseScale = rememberPulseAnimation(
 *     minScale = 0.9f,
 *     maxScale = 1.1f,
 *     durationMillis = 600
 * )
 *
 * // Use pulseScale.value to scale selected elements
 * ```
 *
 * @param minScale Minimum scale value
 * @param maxScale Maximum scale value
 * @param durationMillis Duration of one pulse cycle
 * @return Pulse scale state (oscillates between minScale and maxScale)
 */
@Composable
fun rememberPulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 500
): State<Float> {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(
        label = "PulseAnimation"
    )

    return infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(durationMillis / 2),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
}
