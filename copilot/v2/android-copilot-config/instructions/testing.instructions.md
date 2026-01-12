---
applyTo: "**/*Test.kt,**/*Tests.kt,**/test/**/*.kt,**/androidTest/**/*.kt"
---

# Testing Instructions

## Testing Philosophy

- Prefer **fake implementations** over mocks for cleaner, more maintainable tests
- Test **behavior**, not implementation details
- Write tests that **verify the contract**, not internal state
- Use **real dependencies** when practical (e.g., Room with in-memory database)

## Unit Test Structure

### Test Class Template
```kotlin
class FeatureViewModelTest {

    // Test rules
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    // Dependencies (use fakes, not mocks)
    private lateinit var fakeRepository: FakeItemRepository
    private lateinit var fakeAnalytics: FakeAnalyticsTracker
    
    // Subject under test
    private lateinit var viewModel: FeatureViewModel
    
    @Before
    fun setup() {
        fakeRepository = FakeItemRepository()
        fakeAnalytics = FakeAnalyticsTracker()
        viewModel = FeatureViewModel(
            repository = fakeRepository,
            analytics = fakeAnalytics,
        )
    }
    
    @Test
    fun `when initialized, loads data and shows loading then success`() = runTest {
        // Given
        val expectedItems = listOf(
            Item(id = "1", title = "Item 1"),
            Item(id = "2", title = "Item 2"),
        )
        fakeRepository.setItems(expectedItems)
        
        // When
        val viewModel = FeatureViewModel(fakeRepository, fakeAnalytics)
        
        // Then
        viewModel.uiState.test {
            assertEquals(FeatureUiState.Loading, awaitItem())
            assertEquals(FeatureUiState.Success(expectedItems), awaitItem())
        }
    }
    
    @Test
    fun `when repository fails, shows error state`() = runTest {
        // Given
        fakeRepository.setError(IOException("Network error"))
        
        // When
        val viewModel = FeatureViewModel(fakeRepository, fakeAnalytics)
        
        // Then
        viewModel.uiState.test {
            assertEquals(FeatureUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is FeatureUiState.Error)
            assertEquals("Network error", (errorState as FeatureUiState.Error).message)
        }
    }
    
    @Test
    fun `when refresh action, reloads data`() = runTest {
        // Given
        viewModel.uiState.test {
            skipItems(2) // Skip initial loading and success
            
            // When
            viewModel.onAction(FeatureAction.Refresh)
            
            // Then
            assertTrue(awaitItem() is FeatureUiState.Loading)
            assertTrue(awaitItem() is FeatureUiState.Success)
        }
    }
}
```

## Main Dispatcher Rule

```kotlin
/**
 * A JUnit TestRule that sets the main dispatcher to a test dispatcher.
 * This is required when testing ViewModels that use viewModelScope.
 */
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

## Testing Flows with Turbine

```kotlin
@Test
fun `search returns filtered results`() = runTest {
    // Given
    val allItems = listOf(
        Item(id = "1", title = "Apple"),
        Item(id = "2", title = "Banana"),
        Item(id = "3", title = "Apricot"),
    )
    fakeRepository.setItems(allItems)
    
    // When & Then
    viewModel.searchResults.test {
        // Initial state - empty query
        assertEquals(emptyList<Item>(), awaitItem())
        
        // Search for "App"
        viewModel.onSearchQueryChanged("App")
        
        // Debounce delay
        advanceTimeBy(400)
        
        // Filtered results
        val results = awaitItem()
        assertEquals(2, results.size)
        assertTrue(results.all { it.title.contains("App") })
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

## Fake Implementations

### Fake Repository
```kotlin
class FakeItemRepository : ItemRepository {
    
    private val items = mutableListOf<Item>()
    private val itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    
    private var errorToThrow: Exception? = null
    private var delayMs: Long = 0
    
    // Test configuration methods
    fun setItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        itemsFlow.value = items.toList()
    }
    
    fun setError(exception: Exception) {
        errorToThrow = exception
    }
    
    fun clearError() {
        errorToThrow = null
    }
    
    fun setDelay(ms: Long) {
        delayMs = ms
    }
    
    // Test verification methods
    fun getCreatedItems(): List<Item> = items.toList()
    
    fun wasDeleteCalled(id: String): Boolean = deletedIds.contains(id)
    
    private val deletedIds = mutableListOf<String>()
    
    // Repository implementation
    override fun getItems(): Flow<List<Item>> = flow {
        errorToThrow?.let { throw it }
        if (delayMs > 0) delay(delayMs)
        emitAll(itemsFlow)
    }
    
    override fun getItem(id: String): Flow<Item?> = itemsFlow
        .map { list -> list.find { it.id == id } }
    
    override suspend fun createItem(item: Item): Item {
        errorToThrow?.let { throw it }
        val newItem = item.copy(id = UUID.randomUUID().toString())
        items.add(newItem)
        itemsFlow.value = items.toList()
        return newItem
    }
    
    override suspend fun deleteItem(id: String) {
        errorToThrow?.let { throw it }
        deletedIds.add(id)
        items.removeAll { it.id == id }
        itemsFlow.value = items.toList()
    }
    
    override suspend fun sync(): Result<Unit> {
        errorToThrow?.let { return Result.failure(it) }
        return Result.success(Unit)
    }
}
```

### Fake Data Source
```kotlin
class FakeItemLocalDataSource : ItemLocalDataSource {
    
    private val items = mutableMapOf<String, ItemEntity>()
    private val itemsFlow = MutableStateFlow<List<ItemEntity>>(emptyList())
    
    private fun updateFlow() {
        itemsFlow.value = items.values.toList()
    }
    
    override fun getItems(): Flow<List<ItemEntity>> = itemsFlow
    
    override fun getItem(id: String): Flow<ItemEntity?> = 
        itemsFlow.map { it.find { item -> item.id == id } }
    
    override suspend fun insertItem(item: ItemEntity) {
        items[item.id] = item
        updateFlow()
    }
    
    override suspend fun insertItems(newItems: List<ItemEntity>) {
        newItems.forEach { items[it.id] = it }
        updateFlow()
    }
    
    override suspend fun deleteItem(id: String) {
        items.remove(id)
        updateFlow()
    }
    
    override suspend fun deleteAllItems() {
        items.clear()
        updateFlow()
    }
}
```

## Compose UI Testing

### Setup
```kotlin
class FeatureScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `loading state shows progress indicator`() {
        // When
        composeTestRule.setContent {
            AppTheme {
                FeatureScreen(
                    uiState = FeatureUiState.Loading,
                    onAction = {},
                )
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }
    
    @Test
    fun `success state shows item list`() {
        // Given
        val items = listOf(
            Item(id = "1", title = "First Item"),
            Item(id = "2", title = "Second Item"),
        )
        
        // When
        composeTestRule.setContent {
            AppTheme {
                FeatureScreen(
                    uiState = FeatureUiState.Success(items),
                    onAction = {},
                )
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("First Item")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Second Item")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking item triggers action`() {
        // Given
        var clickedId: String? = null
        val items = listOf(Item(id = "1", title = "Click Me"))
        
        composeTestRule.setContent {
            AppTheme {
                FeatureScreen(
                    uiState = FeatureUiState.Success(items),
                    onAction = { action ->
                        if (action is FeatureAction.ItemClicked) {
                            clickedId = action.id
                        }
                    },
                )
            }
        }
        
        // When
        composeTestRule
            .onNodeWithText("Click Me")
            .performClick()
        
        // Then
        assertEquals("1", clickedId)
    }
    
    @Test
    fun `error state shows retry button`() {
        // Given
        var retryClicked = false
        
        composeTestRule.setContent {
            AppTheme {
                FeatureScreen(
                    uiState = FeatureUiState.Error("Something went wrong"),
                    onAction = { action ->
                        if (action is FeatureAction.Retry) {
                            retryClicked = true
                        }
                    },
                )
            }
        }
        
        // When
        composeTestRule
            .onNodeWithText("Retry")
            .performClick()
        
        // Then
        assertTrue(retryClicked)
    }
}
```

### Testing with Navigation
```kotlin
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun `navigating to detail screen shows correct content`() {
        // Given - app starts on home screen
        composeTestRule
            .onNodeWithText("Home")
            .assertIsDisplayed()
        
        // When - tap on an item
        composeTestRule
            .onNodeWithText("Item 1")
            .performClick()
        
        // Then - detail screen is shown
        composeTestRule
            .onNodeWithText("Item 1 Details")
            .assertIsDisplayed()
    }
    
    @Test
    fun `back navigation returns to previous screen`() {
        // Navigate to detail
        composeTestRule
            .onNodeWithText("Item 1")
            .performClick()
        
        // Press back
        Espresso.pressBack()
        
        // Verify we're back on home
        composeTestRule
            .onNodeWithText("Home")
            .assertIsDisplayed()
    }
}
```

## Repository Testing

```kotlin
class ItemRepositoryImplTest {

    private lateinit var localDataSource: FakeItemLocalDataSource
    private lateinit var remoteDataSource: FakeItemRemoteDataSource
    private lateinit var repository: ItemRepositoryImpl
    
    @Before
    fun setup() {
        localDataSource = FakeItemLocalDataSource()
        remoteDataSource = FakeItemRemoteDataSource()
        repository = ItemRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }
    
    @Test
    fun `getItems returns local data`() = runTest {
        // Given
        val items = listOf(createTestEntity("1"), createTestEntity("2"))
        localDataSource.insertItems(items)
        
        // When
        val result = repository.getItems().first()
        
        // Then
        assertEquals(2, result.size)
    }
    
    @Test
    fun `sync fetches from remote and updates local`() = runTest {
        // Given
        val remoteItems = listOf(
            ItemDto(id = "1", title = "Remote Item"),
        )
        remoteDataSource.setItems(remoteItems)
        
        // When
        val result = repository.sync()
        
        // Then
        assertTrue(result.isSuccess)
        val localItems = localDataSource.getItems().first()
        assertEquals(1, localItems.size)
        assertEquals("Remote Item", localItems[0].title)
    }
}
```

## Best Practices

1. **Name tests descriptively**: Use backticks with full sentences
2. **Follow Given-When-Then**: Structure tests clearly
3. **One assertion per test**: Keep tests focused
4. **Use fakes over mocks**: More maintainable and realistic
5. **Test edge cases**: Empty lists, errors, null values
6. **Use test tags**: For Compose UI elements that need testing
7. **Keep tests fast**: Avoid real network calls and delays
8. **Test public API only**: Don't test private implementation details
