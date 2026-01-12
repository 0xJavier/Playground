plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.hilt)
}

android {
    namespace = "com.myapp.core.domain"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    
    implementation(libs.kotlinx.coroutines.core)
}
