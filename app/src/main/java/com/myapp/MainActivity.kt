package com.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapp.core.designsystem.theme.MyAppTheme
import com.myapp.navigation.MyAppNavHost
import com.myapp.ui.MyApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appStateManager: AppStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appStateManager.appState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        // Keep the splash screen on-screen until the UI state is loaded
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }

        enableEdgeToEdge()

        setContent {
            MyAppTheme {
                MyApp(
                    appState = uiState,
                )
            }
        }
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val isOnboardingComplete: Boolean,
        val isAuthenticated: Boolean,
    ) : MainActivityUiState
}
