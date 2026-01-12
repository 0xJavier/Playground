# Write Tests

Use this prompt when you need to create tests for existing code.

## Prompt for ViewModel Tests

```
Write unit tests for [VIEWMODEL_NAME] ViewModel.

Test the following scenarios:
- Initial state is correct (typically loading)
- Successful data load updates state to success
- Error from repository shows error state
- Each user action is handled correctly
- One-time events are emitted properly
- Edge cases: empty data, null values, etc.

Use:
- JUnit 5
- Turbine for Flow testing
- Fake repository implementations (no mocks)
- MainDispatcherRule for coroutine testing

Follow the Given-When-Then pattern with descriptive test names using backticks.
```

## Prompt for Repository Tests

```
Write unit tests for [REPOSITORY_NAME] Repository.

Test the following scenarios:
- Data flows from local source correctly
- Remote sync updates local database
- Error handling for network failures
- Offline-first behavior (local data when network fails)
- CRUD operations work correctly

Use:
- JUnit 5
- Fake data sources
- UnconfinedTestDispatcher for immediate execution
- Turbine for Flow testing
```

## Prompt for Compose UI Tests

```
Write UI tests for [SCREEN_NAME] Screen.

Test the following:
- Loading state displays progress indicator
- Success state shows expected content
- Error state shows error message and retry button
- User interactions trigger correct actions
- Accessibility: content descriptions are present
- Different screen states render correctly

Use:
- createComposeRule
- Semantic matchers (onNodeWithText, onNodeWithTag)
- performClick, performTextInput for interactions
- assertIsDisplayed, assertExists for assertions
```

## Example: ViewModel Test

```
Write unit tests for TaskListViewModel ViewModel.

Test the following scenarios:
- Initial state is Loading
- When repository returns tasks, state is Success with tasks
- When repository throws error, state is Error with message
- When user clicks task, navigation event is emitted
- When user toggles task completion, repository is called
- When user pulls to refresh, data is reloaded
- When tasks list is empty, state includes isEmpty flag

Use:
- JUnit 5
- Turbine for Flow testing
- Fake repository implementations (no mocks)
- MainDispatcherRule for coroutine testing

Follow the Given-When-Then pattern with descriptive test names using backticks.
```

## Expected Output Pattern

```kotlin
class TaskListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var viewModel: TaskListViewModel
    
    @Before
    fun setup() {
        fakeRepository = FakeTaskRepository()
    }
    
    private fun createViewModel() = TaskListViewModel(fakeRepository)
    
    @Test
    fun `initial state is Loading`() = runTest {
        // When
        viewModel = createViewModel()
        
        // Then
        assertEquals(TaskListUiState.Loading, viewModel.uiState.value)
    }
    
    @Test
    fun `when repository returns tasks, state is Success`() = runTest {
        // Given
        val tasks = listOf(
            Task(id = "1", title = "Task 1"),
            Task(id = "2", title = "Task 2"),
        )
        fakeRepository.setTasks(tasks)
        
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.uiState.test {
            assertEquals(TaskListUiState.Loading, awaitItem())
            val successState = awaitItem() as TaskListUiState.Success
            assertEquals(tasks, successState.tasks)
        }
    }
    
    @Test
    fun `when repository throws error, state is Error`() = runTest {
        // Given
        fakeRepository.setError(IOException("Network error"))
        
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.uiState.test {
            assertEquals(TaskListUiState.Loading, awaitItem())
            val errorState = awaitItem() as TaskListUiState.Error
            assertEquals("Network error", errorState.message)
        }
    }
    
    @Test
    fun `when task clicked, navigation event is emitted`() = runTest {
        // Given
        fakeRepository.setTasks(listOf(Task(id = "1", title = "Task 1")))
        viewModel = createViewModel()
        
        // When
        viewModel.events.test {
            viewModel.onAction(TaskListAction.TaskClicked("1"))
            
            // Then
            assertEquals(TaskListEvent.NavigateToDetail("1"), awaitItem())
        }
    }
    
    @Test
    fun `when toggle completion, repository is updated`() = runTest {
        // Given
        val task = Task(id = "1", title = "Task 1", isCompleted = false)
        fakeRepository.setTasks(listOf(task))
        viewModel = createViewModel()
        
        // When
        viewModel.onAction(TaskListAction.ToggleCompleted("1", true))
        advanceUntilIdle()
        
        // Then
        assertTrue(fakeRepository.wasToggleCompletedCalled("1", true))
    }
    
    @Test
    fun `when tasks list is empty, success state has isEmpty true`() = runTest {
        // Given
        fakeRepository.setTasks(emptyList())
        
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.uiState.test {
            skipItems(1) // Skip loading
            val successState = awaitItem() as TaskListUiState.Success
            assertTrue(successState.isEmpty)
        }
    }
}
```

## Tips for Good Tests

1. **Use descriptive names**: `when X happens, Y should occur`
2. **One assertion per test**: Keep tests focused
3. **Use fakes, not mocks**: More realistic and maintainable
4. **Test behavior, not implementation**: Don't verify internal method calls
5. **Cover edge cases**: Empty lists, null values, errors
6. **Use test helpers**: Create factory functions for test data
