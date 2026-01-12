package com.myapp.core.data.repository

import com.myapp.core.model.DarkThemeConfig
import com.myapp.core.model.ThemeBrand
import com.myapp.core.model.UserData
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user data and preferences.
 */
interface UserDataRepository {
    /**
     * Stream of [UserData]
     */
    val userData: Flow<UserData>

    /**
     * Sets whether the onboarding has been completed.
     */
    suspend fun setOnboardingComplete(isComplete: Boolean)

    /**
     * Sets whether the user is authenticated.
     */
    suspend fun setAuthenticated(isAuthenticated: Boolean)

    /**
     * Sets the user information.
     */
    suspend fun setUserInfo(userId: String, userName: String, email: String)

    /**
     * Sets the desired theme brand.
     */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /**
     * Sets the desired dark theme config.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Clears all user data (for logout).
     */
    suspend fun clearUserData()
}
