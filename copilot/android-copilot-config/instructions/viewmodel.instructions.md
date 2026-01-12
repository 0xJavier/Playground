---
applyTo: "**/*ViewModel.kt"
---

# ViewModel Instructions

## ViewModel Structure

### Basic ViewModel Template
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: SomeRepository,
    private val savedStateHandle: SavedStateHandle, // For navigation arguments
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    
    // Public immutable state
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
    
    // One-time events channel (for navigation, snackbars, etc.)
    private val _events = Channel<FeatureEvent>()
    val events: Flow<FeatureEvent> = _events.receiveAsFlow()
    
    init {
        loadData()
    }
    
    fun onAction(action: FeatureAction) {
        when (action) {
            is FeatureAction.Refresh -> loadData()
            is FeatureAction.ItemClicked -> handleItemClick(action.itemId)
            is FeatureAction.DeleteItem -> deleteItem(action.itemId)
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = FeatureUiState.Loading
            repository.getData()
                .catch { exception ->
                    _uiState.value = FeatureUiState.Error(
                        message = exception.message ?: "Unknown error"
                    )
                }
                .collect { data ->
                    _uiState.value = FeatureUiState.Success(data = data)
                }
        }
    }
    
    private fun handleItemClick(itemId: String) {
        viewModelScope.launch {
            _events.send(FeatureEvent.NavigateToDetail(itemId))
        }
    }
    
    private fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                repository.deleteItem(itemId)
                _events.send(FeatureEvent.ShowSnackbar("Item deleted"))
            } catch (e: Exception) {
                _events.send(FeatureEvent.ShowSnackbar("Failed to delete item"))
            }
        }
    }
}
```

## UI State Definition

### Sealed Interface for UI State
```kotlin
sealed interface FeatureUiState {
    data object Loading : FeatureUiState
    
    data class Success(
        val items: List<ItemUiModel>,
        val isRefreshing: Boolean = false,
    ) : FeatureUiState
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
    ) : FeatureUiState
}
```

### Complex UI State with Multiple Data Sources
```kotlin
data class DashboardUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val recentItems: List<Item> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val errorMessage: String? = null,
) {
    val hasError: Boolean get() = errorMessage != null
    val isEmpty: Boolean get() = recentItems.isEmpty() && notifications.isEmpty()
}
```

## Action/Event Patterns

### Actions (User Intent)
```kotlin
sealed interface FeatureAction {
    data object Refresh : FeatureAction
    data object Retry : FeatureAction
    data class ItemClicked(val itemId: String) : FeatureAction
    data class SearchQueryChanged(val query: String) : FeatureAction
    data class FilterSelected(val filter: FilterType) : FeatureAction
    data class DeleteItem(val itemId: String) : FeatureAction
}
```

### Events (One-time Effects)
```kotlin
sealed interface FeatureEvent {
    data class NavigateToDetail(val itemId: String) : FeatureEvent
    data class ShowSnackbar(val message: String) : FeatureEvent
    data object NavigateBack : FeatureEvent
    data class ShowDialog(val dialogType: DialogType) : FeatureEvent
}
```

## State Combination Patterns

### Combining Multiple Flows
```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        userRepository.getUserProfile(),
        itemsRepository.getRecentItems(),
    ) { userProfile, recentItems ->
        DashboardUiState(
            isLoading = false,
            userProfile = userProfile,
            recentItems = recentItems,
        )
    }
    .catch { exception ->
        emit(DashboardUiState(
            isLoading = false,
            errorMessage = exception.message,
        ))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )
}
```

### Search with Debounce
```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    
    val uiState: StateFlow<SearchUiState> = searchQuery
        .debounce(300) // Debounce for 300ms
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchUiState.Empty)
            } else {
                searchRepository.search(query)
                    .map { results ->
                        if (results.isEmpty()) {
                            SearchUiState.NoResults(query)
                        } else {
                            SearchUiState.Success(results)
                        }
                    }
                    .onStart { emit(SearchUiState.Loading) }
                    .catch { emit(SearchUiState.Error(it.message ?: "Search failed")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.Empty,
        )
    
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
```

## SavedStateHandle for Navigation Arguments

### Accessing Navigation Arguments
```kotlin
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ItemRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Type-safe navigation argument access
    private val itemId: String = checkNotNull(savedStateHandle["itemId"])
    
    // Or with nullable handling
    private val optionalParam: String? = savedStateHandle["optionalParam"]
    
    val uiState: StateFlow<DetailUiState> = repository
        .getItem(itemId)
        .map { item -> DetailUiState.Success(item) }
        .catch { emit(DetailUiState.Error(it.message ?: "Failed to load item")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState.Loading,
        )
}
```

### Persisting State with SavedStateHandle
```kotlin
@HiltViewModel
class FormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // This survives process death
    var title by savedStateHandle.saveable { mutableStateOf("") }
        private set
    
    var description by savedStateHandle.saveable { mutableStateOf("") }
        private set
    
    fun onTitleChanged(newTitle: String) {
        title = newTitle
    }
    
    fun onDescriptionChanged(newDescription: String) {
        description = newDescription
    }
}
```

## Testing ViewModels

### Test Structure
```kotlin
class FeatureViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var repository: FakeRepository
    private lateinit var viewModel: FeatureViewModel
    
    @Before
    fun setup() {
        repository = FakeRepository()
        viewModel = FeatureViewModel(repository)
    }
    
    @Test
    fun `initial state is loading`() = runTest {
        // Given a new ViewModel
        val viewModel = FeatureViewModel(repository)
        
        // Then initial state is Loading
        assertEquals(FeatureUiState.Loading, viewModel.uiState.value)
    }
    
    @Test
    fun `successful data load updates state to success`() = runTest {
        // Given repository returns data
        val expectedData = listOf(Item("1", "Test"))
        repository.setItems(expectedData)
        
        // When ViewModel loads data
        val viewModel = FeatureViewModel(repository)
        
        // Then state is Success with data
        viewModel.uiState.test {
            assertEquals(FeatureUiState.Loading, awaitItem())
            assertEquals(FeatureUiState.Success(expectedData), awaitItem())
        }
    }
    
    @Test
    fun `error from repository shows error state`() = runTest {
        // Given repository throws error
        repository.setError(Exception("Network error"))
        
        // When ViewModel loads data
        val viewModel = FeatureViewModel(repository)
        
        // Then state is Error
        viewModel.uiState.test {
            assertEquals(FeatureUiState.Loading, awaitItem())
            assertTrue(awaitItem() is FeatureUiState.Error)
        }
    }
}
```

## Best Practices

1. **Never expose MutableStateFlow** - Always expose as `StateFlow`
2. **Use viewModelScope** - Coroutines are automatically cancelled on ViewModel clear
3. **Handle errors gracefully** - Catch exceptions and update UI state accordingly
4. **Keep ViewModels thin** - Delegate business logic to use cases/repositories
5. **Use WhileSubscribed(5_000)** - Standard timeout for stateIn to handle configuration changes
6. **Avoid Android framework dependencies** - ViewModel should be testable without Android
7. **Use sealed interfaces** - For type-safe state and action handling
8. **Consider process death** - Use SavedStateHandle for critical state
