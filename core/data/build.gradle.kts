plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.hilt)
}

android {
    namespace = "com.myapp.core.data"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.core.database)
    
    implementation(libs.kotlinx.coroutines.core)
}
