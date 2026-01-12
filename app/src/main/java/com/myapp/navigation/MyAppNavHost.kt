package com.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.myapp.feature.home.navigation.HomeRoute
import com.myapp.feature.home.navigation.homeScreen
import com.myapp.feature.onboarding.navigation.OnboardingRoute
import com.myapp.feature.onboarding.navigation.onboardingGraph
import com.myapp.feature.profile.navigation.profileScreen
import com.myapp.feature.settings.navigation.settingsScreen

@Composable
fun MyAppNavHost(
    isOnboardingComplete: Boolean,
    isAuthenticated: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val startDestination: Any = when {
        !isOnboardingComplete -> OnboardingRoute
        else -> HomeRoute
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // Onboarding flow
        onboardingGraph(
            onOnboardingComplete = {
                navController.navigate(HomeRoute) {
                    popUpTo(OnboardingRoute) { inclusive = true }
                }
            }
        )

        // Main authenticated flow
        homeScreen(
            onNavigateToSettings = {
                navController.navigate(com.myapp.feature.settings.navigation.SettingsRoute)
            }
        )

        profileScreen()

        settingsScreen(
            onBackClick = navController::popBackStack
        )
    }
}
