package com.myapp.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.myapp.feature.profile.ProfileRoute
import kotlinx.serialization.Serializable

@Serializable
data object ProfileRoute

/**
 * Add profile screen to the navigation graph.
 */
fun NavGraphBuilder.profileScreen() {
    composable<ProfileRoute> {
        ProfileRoute()
    }
}

/**
 * Navigate to the profile screen.
 */
fun NavController.navigateToProfile() {
    navigate(ProfileRoute)
}
