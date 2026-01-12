package com.myapp.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.myapp.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

/**
 * Add settings screen to the navigation graph.
 */
fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
) {
    composable<SettingsRoute> {
        SettingsRoute(
            onBackClick = onBackClick,
        )
    }
}

/**
 * Navigate to the settings screen.
 */
fun NavController.navigateToSettings() {
    navigate(SettingsRoute)
}
