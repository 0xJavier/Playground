# Create New Feature

Use this prompt when you need to create a new feature module with all the required components.

## Prompt

```
Create a new feature for [FEATURE_NAME] that [DESCRIPTION].

The feature should include:
1. UI Layer:
   - A Screen composable ([FeatureName]Screen.kt) with proper state handling
   - A Route composable for navigation integration
   - A ViewModel ([FeatureName]ViewModel.kt) with UI state and actions
   - UI state sealed interface
   - Action sealed interface for user interactions

2. Domain Layer (if needed):
   - Use case(s) for business logic

3. Navigation:
   - Navigation route definition
   - Navigation extension function

4. DI:
   - Hilt module for the feature

Follow these patterns:
- Use StateFlow for UI state
- Use sealed interfaces for UI state and actions
- Implement proper loading, success, and error states
- Make Screen composable stateless (receive state, emit events)
- Handle one-time events (navigation, snackbars) via Channel

Example structure:
feature/
└── [featurename]/
    ├── api/
    │   └── [FeatureName]Navigation.kt
    └── impl/
        ├── ui/
        │   ├── [FeatureName]Screen.kt
        │   └── [FeatureName]ViewModel.kt
        └── di/
            └── [FeatureName]Module.kt
```

## Variables to Replace

- `[FEATURE_NAME]`: The name of your feature (e.g., "Profile", "Settings", "ItemDetail")
- `[DESCRIPTION]`: What the feature does (e.g., "displays user profile information and allows editing")

## Example Usage

```
Create a new feature for Profile that displays user profile information including avatar, name, email, and bio. Users should be able to edit their profile and see a loading state while data is fetched.

The feature should include:
1. UI Layer:
   - A Screen composable (ProfileScreen.kt) with proper state handling
   - A Route composable for navigation integration
   - A ViewModel (ProfileViewModel.kt) with UI state and actions
   - UI state sealed interface
   - Action sealed interface for user interactions

2. Domain Layer:
   - GetUserProfileUseCase
   - UpdateUserProfileUseCase

3. Navigation:
   - Navigation route definition
   - Navigation extension function

4. DI:
   - Hilt module for the feature

Follow these patterns:
- Use StateFlow for UI state
- Use sealed interfaces for UI state and actions
- Implement proper loading, success, and error states
- Make Screen composable stateless (receive state, emit events)
- Handle one-time events (navigation, snackbars) via Channel
```

## Expected Output Structure

The AI should generate files following this pattern:

### ProfileScreen.kt
```kotlin
@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
) {
    // ...
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ...
}
```

### ProfileViewModel.kt
```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : ViewModel() {
    // ...
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface ProfileAction {
    data object Refresh : ProfileAction
    data object EditProfile : ProfileAction
    data object Logout : ProfileAction
}
```
