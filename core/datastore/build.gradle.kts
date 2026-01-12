plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.hilt)
}

android {
    namespace = "com.myapp.core.datastore"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
