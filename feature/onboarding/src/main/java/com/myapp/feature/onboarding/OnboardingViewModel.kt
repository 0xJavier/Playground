package com.myapp.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.core.domain.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNextStep() {
        _uiState.update { currentState ->
            if (currentState.currentStep < currentState.totalSteps - 1) {
                currentState.copy(currentStep = currentState.currentStep + 1)
            } else {
                currentState
            }
        }
    }

    fun onPreviousStep() {
        _uiState.update { currentState ->
            if (currentState.currentStep > 0) {
                currentState.copy(currentStep = currentState.currentStep - 1)
            } else {
                currentState
            }
        }
    }

    fun onCompleteOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            completeOnboardingUseCase()
            onComplete()
        }
    }
}

data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 3,
    val steps: List<OnboardingStepData> = listOf(
        OnboardingStepData(
            title = "Welcome to MyApp",
            description = "Discover amazing features that will help you achieve your goals.",
        ),
        OnboardingStepData(
            title = "Stay Organized",
            description = "Keep track of everything important with our intuitive interface.",
        ),
        OnboardingStepData(
            title = "Get Started",
            description = "You're all set! Let's begin your journey.",
        ),
    ),
) {
    val isFirstStep: Boolean get() = currentStep == 0
    val isLastStep: Boolean get() = currentStep == totalSteps - 1
    val currentStepData: OnboardingStepData get() = steps[currentStep]
}

data class OnboardingStepData(
    val title: String,
    val description: String,
)
