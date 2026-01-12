package com.myapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.myapp.MainActivityUiState
import com.myapp.navigation.MyAppNavHost

@Composable
fun MyApp(
    appState: MainActivityUiState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (appState) {
            MainActivityUiState.Loading -> {
                // Splash screen is showing, nothing to render
            }
            is MainActivityUiState.Success -> {
                MyAppNavHost(
                    isOnboardingComplete = appState.isOnboardingComplete,
                    isAuthenticated = appState.isAuthenticated,
                )
            }
        }
    }
}
