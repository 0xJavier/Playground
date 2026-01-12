plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.library.compose)
}

android {
    namespace = "com.myapp.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
}
