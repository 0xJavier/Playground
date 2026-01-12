plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.myapp.core.network"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
