---
applyTo: "**/*Navigation.kt,**/*NavHost.kt,**/*Route.kt,**/navigation/**/*.kt"
---

# Navigation Compose Instructions

## Navigation Architecture Overview

This project uses Jetpack Navigation Compose with type-safe navigation patterns. Navigation is structured to support multi-module apps where features don't depend on each other directly.

## Navigation Graph Setup

### Main NavHost
```kotlin
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = HomeRoute,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        homeScreen(
            onNavigateToDetail = { itemId ->
                navController.navigateToItemDetail(itemId)
            },
        )
        itemDetailScreen(
            onNavigateBack = navController::popBackStack,
            onNavigateToEdit = { itemId ->
                navController.navigateToEditItem(itemId)
            },
        )
        editItemScreen(
            onNavigateBack = navController::popBackStack,
            onSaveComplete = {
                navController.popBackStack()
            },
        )
        // Add more screens...
    }
}
```

## Type-Safe Navigation (Kotlin Serialization)

### Route Definitions
```kotlin
// Simple route without arguments
@Serializable
data object HomeRoute

// Route with required argument
@Serializable
data class ItemDetailRoute(val itemId: String)

// Route with optional arguments
@Serializable
data class SearchRoute(
    val query: String? = null,
    val filter: String? = null,
)

// Route with complex arguments
@Serializable
data class FilterRoute(
    val categories: List<String> = emptyList(),
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
)
```

### Navigation Extensions
```kotlin
// Navigation extension functions for type-safe navigation
fun NavController.navigateToItemDetail(itemId: String) {
    navigate(ItemDetailRoute(itemId))
}

fun NavController.navigateToSearch(query: String? = null) {
    navigate(SearchRoute(query = query))
}

fun NavController.navigateToHome() {
    navigate(HomeRoute) {
        popUpTo(HomeRoute) {
            inclusive = true
        }
    }
}
```

### NavGraph Builder Extensions
```kotlin
// Define screen in navigation graph
fun NavGraphBuilder.itemDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
) {
    composable<ItemDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ItemDetailRoute>()
        ItemDetailRoute(
            itemId = route.itemId,
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit,
        )
    }
}

// With custom transitions
fun NavGraphBuilder.itemDetailScreen(
    onNavigateBack: () -> Unit,
) {
    composable<ItemDetailRoute>(
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
    ) {
        ItemDetailRoute(onNavigateBack = onNavigateBack)
    }
}
```

## Multi-Module Navigation Pattern

### Feature API Module (`:feature:itemdetail:api`)
```kotlin
// Only expose route and navigation function
@Serializable
data class ItemDetailRoute(val itemId: String)

fun NavController.navigateToItemDetail(itemId: String) {
    navigate(ItemDetailRoute(itemId))
}

fun NavGraphBuilder.itemDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
)
```

### Feature Impl Module (`:feature:itemdetail:impl`)
```kotlin
// Implement the navigation graph extension
fun NavGraphBuilder.itemDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
) {
    composable<ItemDetailRoute> {
        ItemDetailRoute(
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit,
        )
    }
}

@Composable
internal fun ItemDetailRoute(
    viewModel: ItemDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle one-time navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ItemDetailEvent.NavigateToEdit -> onNavigateToEdit(event.itemId)
                is ItemDetailEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    
    ItemDetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}
```

## Bottom Navigation / Navigation Rail

### Top-Level Destinations
```kotlin
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
    val route: Any,
) {
    HOME(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        iconTextId = R.string.home,
        titleTextId = R.string.home,
        route = HomeRoute,
    ),
    SEARCH(
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        iconTextId = R.string.search,
        titleTextId = R.string.search,
        route = SearchRoute(),
    ),
    PROFILE(
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        iconTextId = R.string.profile,
        titleTextId = R.string.profile,
        route = ProfileRoute,
    ),
}
```

### App State with Navigation
```kotlin
@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): AppState {
    return remember(navController, coroutineScope) {
        AppState(navController, coroutineScope)
    }
}

@Stable
class AppState(
    val navController: NavHostController,
    private val coroutineScope: CoroutineScope,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination
    
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = TopLevelDestination.entries.firstOrNull { destination ->
            currentDestination?.hasRoute(destination.route::class) == true
        }
    
    val shouldShowBottomBar: Boolean
        @Composable get() = currentTopLevelDestination != null
    
    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            // Pop up to the start destination to avoid building up a large stack
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
```

### Adaptive Navigation (Phone/Tablet)
```kotlin
@Composable
fun AppScaffold(
    appState: AppState,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    
    when {
        windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT -> {
            // Phone: Bottom Navigation
            Scaffold(
                bottomBar = {
                    if (appState.shouldShowBottomBar) {
                        AppBottomBar(
                            destinations = TopLevelDestination.entries,
                            currentDestination = appState.currentDestination,
                            onNavigate = appState::navigateToTopLevelDestination,
                        )
                    }
                },
                modifier = modifier,
            ) { paddingValues ->
                AppNavHost(
                    navController = appState.navController,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
        else -> {
            // Tablet/Desktop: Navigation Rail
            Row(modifier = modifier) {
                if (appState.shouldShowBottomBar) {
                    AppNavigationRail(
                        destinations = TopLevelDestination.entries,
                        currentDestination = appState.currentDestination,
                        onNavigate = appState::navigateToTopLevelDestination,
                    )
                }
                AppNavHost(
                    navController = appState.navController,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
```

## Deep Links

### Defining Deep Links
```kotlin
@Serializable
data class ItemDetailRoute(val itemId: String)

fun NavGraphBuilder.itemDetailScreen(
    onNavigateBack: () -> Unit,
) {
    composable<ItemDetailRoute>(
        deepLinks = listOf(
            navDeepLink<ItemDetailRoute>(
                basePath = "https://example.com/items"
            ),
            navDeepLink<ItemDetailRoute>(
                basePath = "app://example/items"
            ),
        ),
    ) {
        ItemDetailRoute(onNavigateBack = onNavigateBack)
    }
}
```

### AndroidManifest.xml
```xml
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="example.com" />
        <data android:scheme="app" android:host="example" />
    </intent-filter>
</activity>
```

## Navigation Testing

```kotlin
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var navController: TestNavHostController
    
    @Before
    fun setup() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            AppNavHost(navController = navController)
        }
    }
    
    @Test
    fun `clicking item navigates to detail screen`() {
        // When
        composeTestRule
            .onNodeWithText("Item 1")
            .performClick()
        
        // Then
        val currentRoute = navController.currentBackStackEntry?.toRoute<ItemDetailRoute>()
        assertEquals("1", currentRoute?.itemId)
    }
    
    @Test
    fun `back press returns to previous screen`() {
        // Given - navigate to detail
        composeTestRule.runOnUiThread {
            navController.navigateToItemDetail("1")
        }
        
        // When - press back
        composeTestRule.runOnUiThread {
            navController.popBackStack()
        }
        
        // Then - back on home
        assertTrue(navController.currentDestination?.hasRoute<HomeRoute>() == true)
    }
}
```

## Best Practices

1. **Use Type-Safe Navigation**: Use `@Serializable` routes instead of string-based routes
2. **Keep Routes Simple**: Route data classes should only contain navigation arguments
3. **Navigation in ViewModel Events**: Emit navigation events, handle in Route composable
4. **Single NavController**: One NavController per navigation graph
5. **Feature Module Independence**: Features navigate via routes, not direct dependencies
6. **Handle Deep Links**: Define deep links for important screens
7. **Test Navigation**: Use TestNavHostController for navigation tests
8. **Adaptive Navigation**: Support different screen sizes with NavigationRail/BottomBar
9. **Save/Restore State**: Use `saveState` and `restoreState` for bottom navigation
10. **Pop Up Properly**: Clear back stack when navigating to top-level destinations
