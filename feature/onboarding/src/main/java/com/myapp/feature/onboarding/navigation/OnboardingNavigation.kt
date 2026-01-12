package com.myapp.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.myapp.feature.onboarding.OnboardingRoute
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

@Serializable
internal data object OnboardingStepsRoute

/**
 * Onboarding navigation graph.
 */
fun NavGraphBuilder.onboardingGraph(
    onOnboardingComplete: () -> Unit,
) {
    navigation<OnboardingRoute>(
        startDestination = OnboardingStepsRoute,
    ) {
        composable<OnboardingStepsRoute> {
            OnboardingRoute(
                onOnboardingComplete = onOnboardingComplete,
            )
        }
    }
}

/**
 * Navigate to the onboarding screen.
 */
fun NavController.navigateToOnboarding() {
    navigate(OnboardingRoute)
}
