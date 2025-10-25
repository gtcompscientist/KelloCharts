package co.csadev.kellocharts.compose.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * DataStore extension for theme preferences.
 */
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "kellocharts_theme_prefs"
)

/**
 * Repository for managing theme preferences persistence.
 *
 * Uses DataStore to save and retrieve the user's theme preference.
 *
 * ## Usage Example
 * ```kotlin
 * val context = LocalContext.current
 * val themeRepo = ThemePreferencesRepository(context)
 *
 * // Read theme mode
 * val themeMode by themeRepo.themeModeFlow.collectAsState(ThemeMode.SYSTEM)
 *
 * // Save theme mode
 * themeRepo.setThemeMode(ThemeMode.DARK)
 * ```
 */
class ThemePreferencesRepository(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val DYNAMIC_COLOR_KEY = stringPreferencesKey("dynamic_color")
    }

    /**
     * Flow of the current theme mode.
     */
    val themeModeFlow: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        val modeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    /**
     * Flow of the dynamic color preference.
     */
    val dynamicColorFlow: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY]?.toBoolean() ?: true
    }

    /**
     * Save the theme mode preference.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    /**
     * Save the dynamic color preference.
     */
    suspend fun setDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled.toString()
        }
    }

    /**
     * Clear all theme preferences (reset to defaults).
     */
    suspend fun clearPreferences() {
        context.themeDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

/**
 * State holder for theme preferences.
 *
 * Provides convenient access to theme mode and functions to update it.
 *
 * ## Usage Example
 * ```kotlin
 * val themeState = rememberThemeState()
 *
 * KelloChartsTheme(
 *     themeMode = themeState.themeMode,
 *     dynamicColor = themeState.dynamicColor
 * ) {
 *     // Chart content
 *
 *     // Change theme
 *     Button(onClick = { themeState.setThemeMode(ThemeMode.DARK) }) {
 *         Text("Dark Mode")
 *     }
 * }
 * ```
 */
class ThemeState(
    val themeMode: ThemeMode,
    val dynamicColor: Boolean,
    private val repository: ThemePreferencesRepository,
    private val onModeChange: (ThemeMode) -> Unit,
    private val onDynamicColorChange: (Boolean) -> Unit
) {
    /**
     * Update the theme mode and persist it.
     */
    fun setThemeMode(mode: ThemeMode) {
        onModeChange(mode)
    }

    /**
     * Update the dynamic color preference and persist it.
     */
    fun setDynamicColor(enabled: Boolean) {
        onDynamicColorChange(enabled)
    }

    /**
     * Cycle to the next theme mode (Light -> Dark -> System -> Light).
     */
    fun cycleThemeMode() {
        val nextMode = when (themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }
}

/**
 * Remember theme state across recompositions with persistence.
 *
 * @return ThemeState that manages theme mode and dynamic color
 */
@Composable
fun rememberThemeState(): ThemeState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val repository = remember { ThemePreferencesRepository(context) }
    val themeMode by repository.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val dynamicColor by repository.dynamicColorFlow.collectAsState(initial = true)

    return remember(themeMode, dynamicColor) {
        ThemeState(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            repository = repository,
            onModeChange = { mode ->
                scope.launch {
                    repository.setThemeMode(mode)
                }
            },
            onDynamicColorChange = { enabled ->
                scope.launch {
                    repository.setDynamicColor(enabled)
                }
            }
        )
    }
}
