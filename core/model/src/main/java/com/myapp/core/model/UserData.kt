package com.myapp.core.model

/**
 * Represents user data and preferences stored locally.
 */
data class UserData(
    val isOnboardingComplete: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val userName: String? = null,
    val email: String? = null,
    val themeBrand: ThemeBrand = ThemeBrand.DEFAULT,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
)

enum class ThemeBrand {
    DEFAULT,
    ANDROID,
}

enum class DarkThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
}
