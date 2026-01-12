package com.myapp.core.data.repository

import com.myapp.core.datastore.UserPreferencesDataSource
import com.myapp.core.model.DarkThemeConfig
import com.myapp.core.model.ThemeBrand
import com.myapp.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Offline-first implementation of [UserDataRepository].
 * Reads and writes user data from/to the DataStore.
 */
class OfflineFirstUserDataRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : UserDataRepository {

    override val userData: Flow<UserData> = userPreferencesDataSource.userData

    override suspend fun setOnboardingComplete(isComplete: Boolean) {
        userPreferencesDataSource.setOnboardingComplete(isComplete)
    }

    override suspend fun setAuthenticated(isAuthenticated: Boolean) {
        userPreferencesDataSource.setAuthenticated(isAuthenticated)
    }

    override suspend fun setUserInfo(userId: String, userName: String, email: String) {
        userPreferencesDataSource.setUserInfo(userId, userName, email)
    }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        userPreferencesDataSource.setThemeBrand(themeBrand)
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferencesDataSource.setDarkThemeConfig(darkThemeConfig)
    }

    override suspend fun clearUserData() {
        userPreferencesDataSource.clearUserData()
    }
}
