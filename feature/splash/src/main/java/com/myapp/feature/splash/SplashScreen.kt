package com.myapp.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.myapp.core.designsystem.theme.MyAppTheme
import com.myapp.core.ui.LoadingIndicator

/**
 * Splash screen shown during app initialization.
 * Note: The actual splash is handled by Android's SplashScreen API.
 * This composable is a fallback or can be used for longer loading states.
 */
@Composable
internal fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    MyAppTheme {
        SplashScreen()
    }
}
