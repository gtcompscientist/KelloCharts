package co.csadev.kellocharts.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.csadev.kellocharts.compose.theme.KelloChartsTheme
import co.csadev.kellocharts.compose.theme.rememberThemeState
import co.csadev.kellocharts.sample.screens.*

/**
 * Main activity for the KelloCharts sample app.
 *
 * Demonstrates all Compose chart components with Material 3 theming.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SampleApp()
        }
    }
}

@Composable
fun SampleApp() {
    val themeState = rememberThemeState()

    KelloChartsTheme(
        themeMode = themeState.themeMode,
        dynamicColor = themeState.dynamicColor
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SampleAppNavigation(themeState = themeState)
        }
    }
}

@Composable
fun SampleAppNavigation(
    themeState: co.csadev.kellocharts.compose.theme.ThemeState
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                themeState = themeState
            )
        }

        composable("line_chart") {
            LineChartScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("column_chart") {
            ColumnChartScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("pie_chart") {
            PieChartScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("bubble_chart") {
            BubbleChartScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("good_bad_chart") {
            GoodBadChartScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("about") {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
