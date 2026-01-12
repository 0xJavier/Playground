pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyApp"

// Enable type-safe accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// App module
include(":app")

// Core modules
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:ui")

// Feature modules
include(":feature:splash")
include(":feature:onboarding")
include(":feature:home")
include(":feature:profile")
include(":feature:settings")
