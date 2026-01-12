# Create Compose Screen

Use this prompt when you need to create a new Compose screen with best practices.

## Prompt

```
Create a Compose screen for [SCREEN_NAME] that [DESCRIPTION].

Requirements:
- Create a stateless Screen composable that receives UI state and emits actions
- Create a Route composable that connects to the ViewModel
- Support loading, success, and error states
- Include proper Preview annotations with multiple configurations
- Use Material 3 components and theming
- Support accessibility with content descriptions
- Make the screen adaptive for different screen sizes if needed

UI State should include:
- [LIST THE DATA THE SCREEN NEEDS]

Actions the screen should handle:
- [LIST USER INTERACTIONS]

Include these components:
- [LIST UI COMPONENTS NEEDED - e.g., TopAppBar, FloatingActionButton, List, etc.]
```

## Variables to Replace

- `[SCREEN_NAME]`: Name of the screen (e.g., "ItemList", "Settings", "OrderDetails")
- `[DESCRIPTION]`: What the screen displays and its purpose
- `[LIST THE DATA THE SCREEN NEEDS]`: The data properties for the success state
- `[LIST USER INTERACTIONS]`: User actions like click, swipe, input change
- `[LIST UI COMPONENTS NEEDED]`: Material components to include

## Example Usage

```
Create a Compose screen for TaskList that displays a list of tasks with their completion status.

Requirements:
- Create a stateless Screen composable that receives UI state and emits actions
- Create a Route composable that connects to the ViewModel
- Support loading, success, and error states
- Include proper Preview annotations with multiple configurations
- Use Material 3 components and theming
- Support accessibility with content descriptions
- Make the screen adaptive for different screen sizes if needed

UI State should include:
- List of tasks (id, title, description, isCompleted, dueDate)
- Filter option (All, Active, Completed)
- Search query

Actions the screen should handle:
- Task clicked (navigate to detail)
- Task checkbox toggled
- Filter changed
- Search query changed
- Add task FAB clicked
- Pull to refresh

Include these components:
- TopAppBar with search toggle
- SearchBar (expandable)
- FilterChips for task status
- LazyColumn with task items
- FloatingActionButton for adding tasks
- SwipeRefresh for pull to refresh
- Empty state when no tasks
```

## Expected Output Pattern

```kotlin
@Composable
fun TaskListRoute(
    viewModel: TaskListViewModel = hiltViewModel(),
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToAddTask: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    TaskListScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is TaskListAction.TaskClicked -> onNavigateToTaskDetail(action.taskId)
                is TaskListAction.AddTaskClicked -> onNavigateToAddTask()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    uiState: TaskListUiState,
    onAction: (TaskListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TaskListTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = { onAction(TaskListAction.SearchQueryChanged(it)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(TaskListAction.AddTaskClicked) }) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        when (uiState) {
            is TaskListUiState.Loading -> {
                LoadingContent(Modifier.padding(paddingValues))
            }
            is TaskListUiState.Success -> {
                TaskListContent(
                    tasks = uiState.filteredTasks,
                    selectedFilter = uiState.filter,
                    onTaskClick = { onAction(TaskListAction.TaskClicked(it)) },
                    onTaskCheckChanged = { id, checked -> 
                        onAction(TaskListAction.TaskCheckChanged(id, checked)) 
                    },
                    onFilterChanged = { onAction(TaskListAction.FilterChanged(it)) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
            is TaskListUiState.Error -> {
                ErrorContent(
                    message = uiState.message,
                    onRetry = { onAction(TaskListAction.Refresh) },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TaskListScreenPreview() {
    AppTheme {
        TaskListScreen(
            uiState = TaskListUiState.Success(
                tasks = listOf(
                    Task(id = "1", title = "Buy groceries", isCompleted = false),
                    Task(id = "2", title = "Call mom", isCompleted = true),
                ),
                filter = TaskFilter.All,
                searchQuery = "",
            ),
            onAction = {},
        )
    }
}
```
