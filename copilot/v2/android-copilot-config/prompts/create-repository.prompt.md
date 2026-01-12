# Create Repository

Use this prompt when you need to create a new repository with data sources.

## Prompt

```
Create a repository for [ENTITY_NAME] with the following requirements:

Data Operations:
- [LIST CRUD AND QUERY OPERATIONS]

Data Sources:
- Local: Room database (offline-first)
- Remote: REST API (optional)

Include:
1. Repository interface in core/data/repository/
2. Repository implementation with offline-first pattern
3. Local data source (DAO wrapper)
4. Remote data source (API wrapper) if needed
5. Entity class for Room
6. DTO class for network
7. Mapper extensions between layers
8. Hilt module for DI
9. Fake repository for testing

Domain model properties:
- [LIST MODEL PROPERTIES]
```

## Variables to Replace

- `[ENTITY_NAME]`: The name of the entity (e.g., "Task", "User", "Order")
- `[LIST CRUD AND QUERY OPERATIONS]`: Operations like getAll, getById, create, update, delete, search
- `[LIST MODEL PROPERTIES]`: Properties of your domain model

## Example Usage

```
Create a repository for Task with the following requirements:

Data Operations:
- Get all tasks (as Flow)
- Get task by ID (as Flow)
- Get tasks by status (completed/pending)
- Search tasks by title
- Create new task
- Update task
- Delete task
- Mark task as completed
- Sync tasks with server

Data Sources:
- Local: Room database (offline-first)
- Remote: REST API

Include:
1. Repository interface in core/data/repository/
2. Repository implementation with offline-first pattern
3. Local data source (DAO wrapper)
4. Remote data source (API wrapper)
5. Entity class for Room
6. DTO class for network
7. Mapper extensions between layers
8. Hilt module for DI
9. Fake repository for testing

Domain model properties:
- id: String
- title: String
- description: String?
- isCompleted: Boolean
- priority: Priority (LOW, MEDIUM, HIGH)
- dueDate: Instant?
- createdAt: Instant
- updatedAt: Instant
```

## Expected Output Structure

### TaskRepository.kt (Interface)
```kotlin
interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    fun getTask(id: String): Flow<Task?>
    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>>
    fun searchTasks(query: String): Flow<List<Task>>
    suspend fun createTask(task: Task): Task
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
    suspend fun markCompleted(id: String, isCompleted: Boolean)
    suspend fun sync(): Result<Unit>
}
```

### TaskRepositoryImpl.kt
```kotlin
class TaskRepositoryImpl @Inject constructor(
    private val localDataSource: TaskLocalDataSource,
    private val remoteDataSource: TaskRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TaskRepository {
    // Implementation with offline-first pattern
}
```

### TaskEntity.kt
```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    val priority: String,
    @ColumnInfo(name = "due_date") val dueDate: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = false,
)
```

### TaskDto.kt
```kotlin
@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    @SerialName("is_completed") val isCompleted: Boolean,
    val priority: String,
    @SerialName("due_date") val dueDate: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
```

### Mappers.kt
```kotlin
fun TaskDto.toDomainModel(): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = Priority.valueOf(priority),
    dueDate = dueDate?.let { Instant.parse(it) },
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority.name,
    dueDate = dueDate?.toEpochMilli(),
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)

fun TaskEntity.toDomainModel(): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = Priority.valueOf(priority),
    dueDate = dueDate?.let { Instant.ofEpochMilli(it) },
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)
```

### FakeTaskRepository.kt
```kotlin
class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
    
    fun setTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        tasksFlow.value = tasks.toList()
    }
    
    override fun getTasks(): Flow<List<Task>> = tasksFlow
    
    // ... other implementations
}
```
