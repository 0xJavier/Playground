package com.myapp.core.domain

import com.myapp.core.data.repository.UserDataRepository
import javax.inject.Inject

/**
 * Use case for completing onboarding.
 */
class CompleteOnboardingUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke() {
        userDataRepository.setOnboardingComplete(true)
    }
}
