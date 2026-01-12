---
applyTo: "**/*Screen.kt,**/*Component.kt,**/ui/**/*.kt,**/compose/**/*.kt"
---

# Jetpack Compose UI Instructions

## Screen Composables

When creating screen-level composables:

1. **Naming**: Use `FeatureNameScreen` naming convention
2. **State Hoisting**: Accept UI state and event callbacks as parameters
3. **Preview**: Always include `@Preview` annotations with different configurations

### Screen Structure Template
```kotlin
@Composable
fun FeatureNameScreen(
    uiState: FeatureUiState,
    onAction: (FeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is FeatureUiState.Loading -> LoadingContent(modifier)
        is FeatureUiState.Success -> SuccessContent(
            data = uiState.data,
            onAction = onAction,
            modifier = modifier,
        )
        is FeatureUiState.Error -> ErrorContent(
            message = uiState.message,
            onRetry = { onAction(FeatureAction.Retry) },
            modifier = modifier,
        )
    }
}

@Composable
private fun SuccessContent(
    data: FeatureData,
    onAction: (FeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Implementation
}
```

### Route Composable (for Navigation)
```kotlin
@Composable
fun FeatureNameRoute(
    viewModel: FeatureViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    FeatureNameScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is FeatureAction.NavigateToDetail -> onNavigateToDetail(action.id)
                else -> viewModel.onAction(action)
            }
        },
    )
}
```

## Component Guidelines

### Reusable Components
- Place in `core/designsystem/component/`
- Make them stateless and configurable
- Use `Modifier` as first optional parameter
- Provide sensible defaults

```kotlin
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text = text)
    }
}
```

### Modifier Usage
- Always pass `modifier` parameter to root composable
- Chain modifiers in logical order: layout → drawing → interaction
- Use `Modifier.then()` when conditionally adding modifiers

```kotlin
@Composable
fun SomeComponent(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
                else Modifier
            )
            .clickable { /* ... */ }
    ) {
        // Content
    }
}
```

## State Management in Compose

### Remember and State
```kotlin
// For simple UI state that doesn't survive configuration changes
var text by remember { mutableStateOf("") }

// For state that should survive configuration changes
var text by rememberSaveable { mutableStateOf("") }

// For complex state, use derivedStateOf
val sortedList by remember(items) {
    derivedStateOf { items.sortedBy { it.name } }
}
```

### Collecting Flows
```kotlin
// Always use collectAsStateWithLifecycle for lifecycle-aware collection
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// For one-time events, use LaunchedEffect
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is Event.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            is Event.Navigate -> onNavigate(event.destination)
        }
    }
}
```

## Side Effects

### LaunchedEffect
```kotlin
// For effects that depend on keys
LaunchedEffect(userId) {
    viewModel.loadUser(userId)
}

// For one-time effects on composition
LaunchedEffect(Unit) {
    // Initialize something
}
```

### DisposableEffect
```kotlin
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        // Handle lifecycle events
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
}
```

## Lists and Performance

### LazyColumn/LazyRow
```kotlin
LazyColumn(
    modifier = modifier,
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    items(
        items = items,
        key = { it.id }, // Always provide keys for stable identity
    ) { item ->
        ItemCard(
            item = item,
            onClick = { onItemClick(item.id) },
        )
    }
}
```

### Stability for Performance
```kotlin
// Use @Stable or @Immutable for custom classes passed to composables
@Immutable
data class UserUiModel(
    val id: String,
    val name: String,
    val avatarUrl: String,
)

// Or use Stable annotation for classes with stable public properties
@Stable
class TimerState {
    var timeRemaining by mutableStateOf(0L)
        private set
}
```

## Previews

### Comprehensive Previews
```kotlin
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large Font", fontScale = 1.5f)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun FeatureScreenPreview() {
    AppTheme {
        FeatureNameScreen(
            uiState = FeatureUiState.Success(
                data = previewData()
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun FeatureScreenLoadingPreview() {
    AppTheme {
        FeatureNameScreen(
            uiState = FeatureUiState.Loading,
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun FeatureScreenErrorPreview() {
    AppTheme {
        FeatureNameScreen(
            uiState = FeatureUiState.Error("Something went wrong"),
            onAction = {},
        )
    }
}
```

## Accessibility

- Always provide `contentDescription` for images and icons
- Use semantic properties: `Modifier.semantics { }`
- Ensure touch targets are at least 48dp
- Test with TalkBack enabled

```kotlin
Icon(
    imageVector = Icons.Default.Favorite,
    contentDescription = "Add to favorites", // Or null if decorative
    modifier = Modifier.clickable(
        onClick = onFavoriteClick,
        onClickLabel = "Add item to favorites",
    )
)
```

## Material 3 Theming

- Use `MaterialTheme.colorScheme` for colors
- Use `MaterialTheme.typography` for text styles
- Use `MaterialTheme.shapes` for shapes
- Support dynamic color on Android 12+

```kotlin
Text(
    text = "Title",
    style = MaterialTheme.typography.headlineMedium,
    color = MaterialTheme.colorScheme.onSurface,
)
```
