# Android Kotlin Project - GitHub Copilot Instructions

## Project Overview
This is a modern Android application built with Kotlin and Jetpack Compose, following Google's official architecture guidance. The app uses a clean, layered architecture with separation of concerns, unidirectional data flow, and reactive programming patterns.

## Tech Stack
- **Language**: Kotlin (100%)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles (UI → Domain → Data layers)
- **Dependency Injection**: Hilt
- **Async/Reactive**: Kotlin Coroutines and Flow
- **Local Database**: Room
- **Networking**: Retrofit with Kotlin Serialization
- **Navigation**: Jetpack Navigation Compose
- **Testing**: JUnit, MockK, Turbine (for Flow testing), Compose Testing
- **Build System**: Gradle with Kotlin DSL and Version Catalogs

## Architecture Principles

### Layer Structure
1. **UI Layer** (`feature/*/ui/`): Compose screens, ViewModels, UI state
2. **Domain Layer** (`core/domain/`): Use cases, business logic (optional but recommended)
3. **Data Layer** (`core/data/`): Repositories, data sources, models

### Key Patterns
- **Unidirectional Data Flow (UDF)**: State flows down, events flow up
- **Single Source of Truth (SSOT)**: Each data type has one authoritative source
- **Repository Pattern**: Abstract data sources from the rest of the app
- **Dependency Inversion**: Depend on abstractions, not concretions

## Coding Guidelines

### Kotlin Style
- Use `val` over `var` whenever possible (immutability)
- Prefer data classes for models
- Use sealed classes/interfaces for UI state and results
- Leverage Kotlin's null safety - avoid `!!` operator
- Use extension functions for cleaner code
- Follow Kotlin naming conventions (camelCase for functions/properties, PascalCase for classes)

### Compose Guidelines
- Composables should be stateless when possible - hoist state up
- Use `remember` and `rememberSaveable` appropriately
- Follow the naming convention: `@Composable fun FeatureNameScreen()`
- Extract reusable UI components to the design system module
- Use `Modifier` as the first optional parameter
- Prefer `LaunchedEffect` and `SideEffect` for side effects
- Use `collectAsStateWithLifecycle()` for collecting Flows in Compose

### ViewModel Guidelines
- Extend `androidx.lifecycle.ViewModel`
- Expose UI state as `StateFlow<UiState>`
- Use `viewModelScope` for coroutines
- Handle user events through a single `onEvent()` function or specific action functions
- Never expose mutable state directly

### Repository Guidelines
- Define repository interfaces in the domain/data layer
- Implement repositories in the data layer
- Repositories should be the single source of truth for their data type
- Use `Flow` for reactive data streams
- Handle offline-first scenarios with local caching

### Coroutines and Flow
- Use appropriate dispatchers: `Dispatchers.IO` for I/O, `Dispatchers.Default` for CPU-intensive work
- Use `flowOn()` to change dispatcher for upstream operations
- Prefer `StateFlow` and `SharedFlow` over `LiveData`
- Use `stateIn()` and `shareIn()` operators appropriately
- Handle errors with `catch` operator or `Result` wrapper

### Dependency Injection (Hilt)
- Use `@HiltViewModel` for ViewModels
- Use `@Inject constructor` for dependencies
- Define modules with `@Module` and `@InstallIn`
- Use appropriate scopes: `@Singleton`, `@ViewModelScoped`, `@ActivityScoped`

### Testing
- Write unit tests for ViewModels, UseCases, and Repositories
- Use fake implementations instead of mocks when possible
- Test Compose UI with `composeTestRule`
- Use Turbine for testing Flows
- Aim for meaningful test coverage, not 100% coverage

## File Organization

```
app/
├── src/main/kotlin/com/example/app/
│   ├── MainActivity.kt
│   ├── MainApplication.kt
│   └── navigation/
│       └── AppNavHost.kt
│
feature/
├── featurename/
│   ├── api/                    # Navigation keys (if using multi-module navigation)
│   └── impl/
│       ├── ui/
│       │   ├── FeatureScreen.kt
│       │   └── FeatureViewModel.kt
│       ├── domain/             # Feature-specific use cases (optional)
│       └── di/
│           └── FeatureModule.kt
│
core/
├── data/
│   ├── repository/
│   │   ├── SomeRepository.kt   # Interface
│   │   └── SomeRepositoryImpl.kt
│   ├── local/
│   │   ├── database/
│   │   └── datastore/
│   └── remote/
│       ├── api/
│       └── dto/
│
├── domain/
│   ├── model/                  # Domain models
│   └── usecase/                # Use cases
│
├── designsystem/
│   ├── component/              # Reusable Compose components
│   ├── theme/                  # Theme, colors, typography
│   └── icon/                   # App icons
│
├── common/
│   └── util/                   # Shared utilities
│
└── testing/                    # Test utilities and fakes
```

## Common Patterns

### UI State Pattern
```kotlin
sealed interface FeatureUiState {
    data object Loading : FeatureUiState
    data class Success(val data: SomeData) : FeatureUiState
    data class Error(val message: String) : FeatureUiState
}
```

### Result Wrapper
```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}
```

### ViewModel Template
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val someUseCase: SomeUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
    
    fun onAction(action: FeatureAction) {
        when (action) {
            is FeatureAction.LoadData -> loadData()
            // Handle other actions
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            someUseCase()
                .collect { result ->
                    _uiState.value = when (result) {
                        is Result.Loading -> FeatureUiState.Loading
                        is Result.Success -> FeatureUiState.Success(result.data)
                        is Result.Error -> FeatureUiState.Error(result.exception.message ?: "Unknown error")
                    }
                }
        }
    }
}
```

## Build and Test Commands
- Build: `./gradlew assembleDebug`
- Unit Tests: `./gradlew testDebugUnitTest`
- Instrumented Tests: `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lint`
- Format: `./gradlew spotlessApply` (if using Spotless)

## Important Notes
- Always handle configuration changes (screen rotation, window resize)
- Support adaptive layouts for different screen sizes
- Use proper lifecycle handling for resources
- Follow Material 3 design guidelines
- Ensure accessibility support (content descriptions, proper contrast)
- Handle edge cases: empty states, error states, loading states
