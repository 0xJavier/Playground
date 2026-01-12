package com.myapp.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.myapp.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/**
 * Add home screen to the navigation graph.
 */
fun NavGraphBuilder.homeScreen(
    onNavigateToSettings: () -> Unit,
) {
    composable<HomeRoute> {
        HomeRoute(
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}

/**
 * Navigate to the home screen.
 */
fun NavController.navigateToHome() {
    navigate(HomeRoute) {
        popUpTo(0) { inclusive = true }
    }
}
