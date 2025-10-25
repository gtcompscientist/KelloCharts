package co.csadev.kellocharts.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Theme mode options for KelloCharts.
 */
enum class ThemeMode {
    /** Always use light theme */
    LIGHT,

    /** Always use dark theme */
    DARK,

    /** Follow system theme setting */
    SYSTEM
}

/**
 * Light color scheme for KelloCharts with Material 3 colors.
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),

    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),

    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

/**
 * Dark color scheme for KelloCharts with Material 3 colors.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),

    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),

    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

/**
 * Main theme composable for KelloCharts with Material 3 support.
 *
 * This theme wraps Material3 theme and provides additional chart-specific theming.
 * Supports light/dark/system theme modes and dynamic color on Android 12+.
 *
 * ## Usage Example
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     KelloChartsTheme(
 *         themeMode = ThemeMode.SYSTEM,
 *         dynamicColor = true
 *     ) {
 *         Surface(modifier = Modifier.fillMaxSize()) {
 *             LineChart(data = myData)
 *         }
 *     }
 * }
 * ```
 *
 * @param themeMode The theme mode (Light, Dark, or System)
 * @param dynamicColor Whether to use dynamic color from Android 12+ Material You
 * @param content The composable content to theme
 */
@Composable
fun KelloChartsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        // Dynamic color is available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        // Use standard dark/light color schemes
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Create chart-specific colors based on the current color scheme
    val chartColors = if (isDarkTheme) {
        defaultDarkChartColors(colorScheme)
    } else {
        defaultLightChartColors(colorScheme)
    }

    CompositionLocalProvider(
        LocalChartColors provides chartColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KelloChartsTypography,
            content = content
        )
    }
}

/**
 * Preview-friendly version of KelloChartsTheme that works in Android Studio Preview.
 *
 * @param darkTheme Whether to use dark theme
 * @param content The composable content to theme
 */
@Composable
fun KelloChartsThemePreview(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    KelloChartsTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        dynamicColor = false,
        content = content
    )
}
