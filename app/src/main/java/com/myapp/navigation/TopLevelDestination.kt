package com.myapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.myapp.feature.home.navigation.HomeRoute
import com.myapp.feature.profile.navigation.ProfileRoute
import com.myapp.feature.settings.navigation.SettingsRoute

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can be displayed in the bottom navigation bar.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
    val route: Any,
) {
    HOME(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        iconTextId = com.myapp.core.ui.R.string.home,
        titleTextId = com.myapp.core.ui.R.string.home,
        route = HomeRoute,
    ),
    PROFILE(
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        iconTextId = com.myapp.core.ui.R.string.profile,
        titleTextId = com.myapp.core.ui.R.string.profile,
        route = ProfileRoute,
    ),
    SETTINGS(
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconTextId = com.myapp.core.ui.R.string.settings,
        titleTextId = com.myapp.core.ui.R.string.settings,
        route = SettingsRoute,
    ),
}
