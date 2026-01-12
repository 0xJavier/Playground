package com.myapp.core.model

/**
 * Represents a step in the onboarding flow.
 */
data class OnboardingStep(
    val id: Int,
    val title: String,
    val description: String,
    val imageResId: Int? = null,
)
