# MyApp - Android Scaffold Project

A modern Android application scaffold following Google's recommended architecture patterns, inspired by the [Now in Android](https://github.com/android/nowinandroid) sample project.

## Architecture Overview

This project follows the official [Android App Architecture](https://developer.android.com/topic/architecture) guidelines with:

- **Unidirectional Data Flow (UDF)** - State flows down, events flow up
- **Single Source of Truth (SSOT)** - Data is owned and modified only in one place
- **Separation of Concerns** - Clear boundaries between UI, domain, and data layers

### Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer                               │
│  (Compose UI + ViewModels + UI State)                       │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                             │
│  (Use Cases / Interactors)                                  │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                              │
│  (Repositories + Data Sources)                              │
└─────────────────────────────────────────────────────────────┘
```

## Module Structure

```
MyApp/
├── app/                          # Main application module
│   └── Main entry point, navigation, DI setup
│
├── build-logic/                  # Convention plugins
│   └── convention/               # Reusable build configurations
│
├── core/                         # Core/shared modules
│   ├── common/                   # Common utilities, Result wrapper, Dispatchers
│   ├── data/                     # Repository implementations
│   ├── database/                 # Room database setup
│   ├── datastore/                # DataStore preferences
│   ├── designsystem/             # Theme, colors, typography, components
│   ├── domain/                   # Use cases
│   ├── model/                    # Data models
│   ├── network/                  # Retrofit setup, API interfaces
│   └── ui/                       # Shared UI components
│
└── feature/                      # Feature modules
    ├── splash/                   # Splash screen
    ├── onboarding/               # Onboarding flow
    ├── home/                     # Home screen
    ├── profile/                  # Profile screen
    └── settings/                 # Settings screen
```

## App Flow

```
┌──────────────────────────────────────────────────────────────┐
│                         App Start                             │
│                            │                                  │
│                    ┌───────▼───────┐                         │
│                    │ Splash Screen │                         │
│                    │ (Load Data)   │                         │
│                    └───────┬───────┘                         │
│                            │                                  │
│              ┌─────────────┴─────────────┐                   │
│              │                           │                    │
│    ┌─────────▼─────────┐     ┌──────────▼──────────┐        │
│    │ Onboarding Flow   │     │  Authenticated Flow  │        │
│    │ (if not complete) │     │   (Tab Bar View)     │        │
│    └─────────┬─────────┘     └──────────────────────┘        │
│              │                                                │
│              └──────────────────────────────────────────────▶│
│                        (on complete)                          │
└──────────────────────────────────────────────────────────────┘
```

## Tech Stack

### Core
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

### UI
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Material Design 3 components
- **Compose Navigation** - Type-safe navigation

### Architecture Components
- **ViewModel** - UI state management
- **DataStore** - Preferences storage
- **Room** - Local database (ready for use)
- **Hilt** - Dependency injection

### Networking
- **Retrofit** - HTTP client
- **OkHttp** - HTTP & logging interceptor
- **Kotlinx Serialization** - JSON parsing

### Build
- **Gradle Kotlin DSL** - Build scripts
- **Version Catalog** - Centralized dependency management
- **Convention Plugins** - Reusable build configurations

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35

### Setup

1. Clone or download the project
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or device

### Configuration

#### API Base URL
Update the base URL in `core/network/di/NetworkModule.kt`:
```kotlin
.baseUrl("https://your-api.com/")
```

#### App ID
Update in `app/build.gradle.kts`:
```kotlin
applicationId = "com.yourcompany.yourapp"
```

## Convention Plugins

The project uses convention plugins for consistent build configuration:

| Plugin | Description |
|--------|-------------|
| `myapp.android.application` | Base Android application setup |
| `myapp.android.application.compose` | Application + Compose support |
| `myapp.android.library` | Base Android library setup |
| `myapp.android.library.compose` | Library + Compose support |
| `myapp.android.feature` | Feature module (includes UI, Domain dependencies) |
| `myapp.android.hilt` | Hilt dependency injection |
| `myapp.android.room` | Room database setup |

## Adding New Features

### 1. Create Feature Module

```bash
mkdir -p feature/newfeature/src/main/java/com/myapp/feature/newfeature/navigation
```

### 2. Create build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.myapp.android.feature)
}

android {
    namespace = "com.myapp.feature.newfeature"
}
```

### 3. Add to settings.gradle.kts

```kotlin
include(":feature:newfeature")
```

### 4. Create Navigation

```kotlin
@Serializable
data object NewFeatureRoute

fun NavGraphBuilder.newFeatureScreen() {
    composable<NewFeatureRoute> {
        NewFeatureRoute()
    }
}
```

## Key Files

| File | Purpose |
|------|---------|
| `app/MainActivity.kt` | Entry point with splash screen |
| `app/AppStateManager.kt` | Determines onboarding vs. main flow |
| `app/navigation/MyAppNavHost.kt` | Root navigation setup |
| `core/data/repository/UserDataRepository.kt` | User data interface |
| `core/datastore/UserPreferencesDataSource.kt` | Preferences persistence |
| `core/designsystem/theme/Theme.kt` | App theming |

## Best Practices Implemented

- ✅ Single Activity architecture
- ✅ Unidirectional Data Flow
- ✅ Repository pattern for data
- ✅ Use cases for business logic
- ✅ Type-safe navigation
- ✅ Compose state management
- ✅ Dependency injection with Hilt
- ✅ Convention plugins for build config
- ✅ Version catalog for dependencies
- ✅ Material 3 theming
- ✅ Modular architecture

## Resources

- [Guide to Android App Architecture](https://developer.android.com/topic/architecture)
- [Now in Android Sample](https://github.com/android/nowinandroid)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Modularization](https://developer.android.com/topic/modularization)

## License

This project scaffold is provided as-is for learning and development purposes.
