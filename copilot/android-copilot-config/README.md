# Android Kotlin Copilot Instructions

This repository contains GitHub Copilot configuration files for modern Android development with Kotlin and Jetpack Compose, following Google's official architecture guidance.

## 📁 File Structure

```
.github/
├── copilot-instructions.md          # Main instructions (always active)
├── instructions/                     # Path-specific instructions
│   ├── compose-ui.instructions.md   # For Compose UI files
│   ├── viewmodel.instructions.md    # For ViewModel files
│   ├── repository.instructions.md   # For Repository files
│   ├── room-database.instructions.md # For Room database files
│   ├── testing.instructions.md      # For test files
│   └── hilt-di.instructions.md      # For Hilt DI modules
├── prompts/                          # Reusable prompts
│   ├── create-feature.prompt.md     # Create a new feature module
│   ├── create-compose-screen.prompt.md # Create a Compose screen
│   ├── create-repository.prompt.md  # Create a repository
│   └── write-tests.prompt.md        # Write tests for code
└── agents/                           # Custom AI agents
    ├── android-architect.agent.md   # Architecture design expert
    └── android-reviewer.agent.md    # Code review expert
```

## 🚀 Setup

1. Copy the `.github` folder to the root of your Android project
2. Enable custom instructions in your IDE:
   - **VS Code**: Settings → Copilot → "Use Instruction Files" ✓
   - **Android Studio/IntelliJ**: Settings → Tools → GitHub Copilot → Enable custom instructions
3. Restart your IDE

## 📝 How It Works

### Main Instructions (`copilot-instructions.md`)
These instructions are **always active** and apply to all Copilot interactions. They include:
- Project overview and tech stack
- Architecture principles (UI → Domain → Data layers)
- Coding guidelines for Kotlin and Compose
- File organization patterns
- Common patterns (UI State, Result wrapper, ViewModel template)

### Path-Specific Instructions (`instructions/*.instructions.md`)
These automatically apply based on the file you're working on:

| File Pattern | Instructions Applied |
|--------------|---------------------|
| `*Screen.kt`, `*Component.kt`, `ui/**/*.kt` | `compose-ui.instructions.md` |
| `*ViewModel.kt` | `viewmodel.instructions.md` |
| `*Repository.kt`, `repository/**/*.kt` | `repository.instructions.md` |
| `*Dao.kt`, `*Database.kt`, `*Entity.kt` | `room-database.instructions.md` |
| `*Test.kt`, `test/**/*.kt` | `testing.instructions.md` |
| `*Module.kt`, `di/**/*.kt` | `hilt-di.instructions.md` |

### Prompts (`prompts/*.prompt.md`)
Use these with Copilot Chat using the `/` command:
- `/create-feature` - Create a complete feature module
- `/create-compose-screen` - Create a new Compose screen
- `/create-repository` - Create a repository with data sources
- `/write-tests` - Generate tests for existing code

### Agents (`agents/*.agent.md`)
Specialized AI personas for specific tasks:
- **Android Architect** - Helps design features and architecture
- **Android Reviewer** - Reviews code for best practices

## 🏗️ Architecture Overview

This configuration follows Google's recommended architecture:

```
┌─────────────────────────────────────────────────────────┐
│                       UI Layer                          │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │   Screen    │ ←→ │  ViewModel  │ ←→ │  UI State   │ │
│  │ (Compose)   │    │             │    │  (sealed)   │ │
│  └─────────────┘    └─────────────┘    └─────────────┘ │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                         │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │  Use Case   │    │  Use Case   │    │   Domain    │ │
│  │     A       │    │     B       │    │   Models    │ │
│  └─────────────┘    └─────────────┘    └─────────────┘ │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                          │
│  ┌─────────────────────────────────────────────────────┐│
│  │                   Repository                        ││
│  └─────────────────────────────────────────────────────┘│
│           ↓                               ↓             │
│  ┌─────────────────┐            ┌─────────────────┐    │
│  │ Local Data      │            │ Remote Data     │    │
│  │ Source (Room)   │            │ Source (API)    │    │
│  └─────────────────┘            └─────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## 💡 Usage Examples

### Creating a New Feature
In Copilot Chat:
```
Create a new feature for UserProfile that displays user information 
including avatar, name, email, and allows editing. Include:
- ProfileScreen with loading/success/error states
- ProfileViewModel with proper state management
- ProfileRepository for data access
- Unit tests for the ViewModel
```

### Getting Architecture Advice
Use the Android Architect agent:
```
@android-architect How should I structure a feature that needs to:
- Show a list of items from API
- Allow offline access
- Support filtering and search
- Handle pagination
```

### Code Review
Use the Android Reviewer agent:
```
@android-reviewer Please review this ViewModel for best practices:
[paste your code]
```

## 📚 Key Patterns

### UI State Pattern
```kotlin
sealed interface FeatureUiState {
    data object Loading : FeatureUiState
    data class Success(val data: Data) : FeatureUiState
    data class Error(val message: String) : FeatureUiState
}
```

### ViewModel Pattern
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<FeatureUiState>(Loading)
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
    
    fun onAction(action: FeatureAction) { /* handle actions */ }
}
```

### Screen Pattern
```kotlin
@Composable
fun FeatureScreen(
    uiState: FeatureUiState,
    onAction: (FeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stateless composable
}
```

## 🎯 Best Practices Enforced

- ✅ Separation of concerns between layers
- ✅ Unidirectional data flow (state down, events up)
- ✅ Stateless Compose components
- ✅ Proper use of StateFlow and collectAsStateWithLifecycle
- ✅ Offline-first data handling
- ✅ Fake implementations for testing (no mocks)
- ✅ Proper error handling with sealed classes
- ✅ Lifecycle-aware collection of flows

## 📖 References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Now in Android Sample App](https://github.com/android/nowinandroid)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)

## 🤝 Customization

Feel free to modify these files to match your team's conventions:

1. **Add project-specific patterns** to `copilot-instructions.md`
2. **Create new prompts** in `prompts/` for your common tasks
3. **Adjust code templates** in instructions files
4. **Add new agents** for specialized roles (e.g., Performance Expert)

## 📄 License

These configuration files are provided as-is for your use. Modify and distribute as needed for your projects.
