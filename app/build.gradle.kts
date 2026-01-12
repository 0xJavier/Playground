plugins {
    alias(libs.plugins.myapp.android.application)
    alias(libs.plugins.myapp.android.application.compose)
    alias(libs.plugins.myapp.android.hilt)
}

android {
    namespace = "com.myapp"

    defaultConfig {
        applicationId = "com.myapp"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Core modules
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    // Feature modules
    implementation(projects.feature.splash)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.home)
    implementation(projects.feature.profile)
    implementation(projects.feature.settings)

    // Splash Screen
    implementation(libs.androidx.splashscreen)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
