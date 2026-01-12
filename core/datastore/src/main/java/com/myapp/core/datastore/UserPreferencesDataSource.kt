package com.myapp.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.myapp.core.model.DarkThemeConfig
import com.myapp.core.model.ThemeBrand
import com.myapp.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val userData: Flow<UserData> = dataStore.data.map { preferences ->
        UserData(
            isOnboardingComplete = preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE] ?: false,
            isAuthenticated = preferences[PreferencesKeys.IS_AUTHENTICATED] ?: false,
            userId = preferences[PreferencesKeys.USER_ID],
            userName = preferences[PreferencesKeys.USER_NAME],
            email = preferences[PreferencesKeys.EMAIL],
            themeBrand = ThemeBrand.valueOf(
                preferences[PreferencesKeys.THEME_BRAND] ?: ThemeBrand.DEFAULT.name
            ),
            darkThemeConfig = DarkThemeConfig.valueOf(
                preferences[PreferencesKeys.DARK_THEME_CONFIG] ?: DarkThemeConfig.FOLLOW_SYSTEM.name
            ),
        )
    }

    suspend fun setOnboardingComplete(isComplete: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE] = isComplete
        }
    }

    suspend fun setAuthenticated(isAuthenticated: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AUTHENTICATED] = isAuthenticated
        }
    }

    suspend fun setUserInfo(userId: String, userName: String, email: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_NAME] = userName
            preferences[PreferencesKeys.EMAIL] = email
        }
    }

    suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_BRAND] = themeBrand.name
        }
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME_CONFIG] = darkThemeConfig.name
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private object PreferencesKeys {
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val EMAIL = stringPreferencesKey("email")
        val THEME_BRAND = stringPreferencesKey("theme_brand")
        val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
    }
}
