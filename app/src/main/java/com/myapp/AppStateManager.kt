package com.myapp

import com.myapp.core.data.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Manages the overall app state, determining which flow (onboarding or main) to show
 * based on user data loaded from the repository.
 */
class AppStateManager @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    val appState: Flow<MainActivityUiState> = userDataRepository.userData.map { userData ->
        MainActivityUiState.Success(
            isOnboardingComplete = userData.isOnboardingComplete,
            isAuthenticated = userData.isAuthenticated,
        )
    }
}
