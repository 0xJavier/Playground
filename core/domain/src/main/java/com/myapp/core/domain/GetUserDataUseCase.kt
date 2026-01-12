package com.myapp.core.domain

import com.myapp.core.data.repository.UserDataRepository
import com.myapp.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting user data stream.
 */
class GetUserDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<UserData> = userDataRepository.userData
}
