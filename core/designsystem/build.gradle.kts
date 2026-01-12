plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.library.compose)
}

android {
    namespace = "com.myapp.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
}
