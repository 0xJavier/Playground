package com.myapp.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myapp.core.designsystem.component.MyAppButton
import com.myapp.core.designsystem.component.MyAppOutlinedButton
import com.myapp.core.designsystem.component.MyAppTextButton
import com.myapp.core.designsystem.theme.MyAppTheme

@Composable
internal fun OnboardingRoute(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onNextStep = viewModel::onNextStep,
        onPreviousStep = viewModel::onPreviousStep,
        onComplete = { viewModel.onCompleteOnboarding(onOnboardingComplete) },
        onSkip = { viewModel.onCompleteOnboarding(onOnboardingComplete) },
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.currentStep,
        pageCount = { uiState.totalSteps },
    )

    // Sync pager with ViewModel state
    LaunchedEffect(uiState.currentStep) {
        pagerState.animateScrollToPage(uiState.currentStep)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!uiState.isLastStep) {
                    MyAppTextButton(onClick = onSkip) {
                        Text("Skip")
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false,
            ) { page ->
                OnboardingStepContent(
                    stepData = uiState.steps[page],
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page indicators
            PageIndicator(
                currentPage = uiState.currentStep,
                totalPages = uiState.totalSteps,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!uiState.isFirstStep) {
                    MyAppOutlinedButton(
                        onClick = onPreviousStep,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Back")
                    }
                }

                MyAppButton(
                    onClick = if (uiState.isLastStep) onComplete else onNextStep,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (uiState.isLastStep) "Get Started" else "Next")
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepContent(
    stepData: OnboardingStepData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Placeholder for illustration
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge,
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stepData.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stepData.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    MyAppTheme {
        OnboardingScreen(
            uiState = OnboardingUiState(),
            onNextStep = {},
            onPreviousStep = {},
            onComplete = {},
            onSkip = {},
        )
    }
}
