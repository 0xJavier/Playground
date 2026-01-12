---
applyTo: "**/*Repository.kt,**/*RepositoryImpl.kt,**/repository/**/*.kt"
---

# Repository Instructions

## Repository Pattern Overview

Repositories are the single source of truth for data in the application. They abstract data sources from the rest of the app and coordinate data operations between local and remote sources.

## Repository Interface

Define repository interfaces in `core/data/repository/` or `core/domain/repository/`:

```kotlin
/**
 * Repository for managing [Item] data.
 * 
 * This repository coordinates between network and local data sources,
 * providing an offline-first experience.
 */
interface ItemRepository {
    
    /**
     * Returns a stream of all items.
     * Data is fetched from local database and synced with network when available.
     */
    fun getItems(): Flow<List<Item>>
    
    /**
     * Returns a stream of a specific item by ID.
     * @param id The unique identifier of the item.
     * @return Flow emitting the item or null if not found.
     */
    fun getItem(id: String): Flow<Item?>
    
    /**
     * Creates a new item.
     * @param item The item to create.
     * @return The created item with server-assigned ID.
     */
    suspend fun createItem(item: Item): Item
    
    /**
     * Updates an existing item.
     * @param item The item with updated data.
     */
    suspend fun updateItem(item: Item)
    
    /**
     * Deletes an item.
     * @param id The ID of the item to delete.
     */
    suspend fun deleteItem(id: String)
    
    /**
     * Syncs local data with remote server.
     * @return Result indicating success or failure.
     */
    suspend fun sync(): Result<Unit>
}
```

## Repository Implementation

### Offline-First Repository
```kotlin
class ItemRepositoryImpl @Inject constructor(
    private val localDataSource: ItemLocalDataSource,
    private val remoteDataSource: ItemRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ItemRepository {

    override fun getItems(): Flow<List<Item>> = localDataSource
        .getItems()
        .map { entities -> entities.map { it.toDomainModel() } }
        .flowOn(ioDispatcher)
    
    override fun getItem(id: String): Flow<Item?> = localDataSource
        .getItem(id)
        .map { it?.toDomainModel() }
        .flowOn(ioDispatcher)
    
    override suspend fun createItem(item: Item): Item = withContext(ioDispatcher) {
        // Try remote first
        try {
            val remoteItem = remoteDataSource.createItem(item.toNetworkModel())
            val domainItem = remoteItem.toDomainModel()
            localDataSource.insertItem(domainItem.toEntity())
            domainItem
        } catch (e: Exception) {
            // If offline, save locally with pending sync flag
            val localItem = item.copy(pendingSync = true)
            localDataSource.insertItem(localItem.toEntity())
            localItem
        }
    }
    
    override suspend fun updateItem(item: Item) = withContext(ioDispatcher) {
        localDataSource.updateItem(item.toEntity())
        try {
            remoteDataSource.updateItem(item.id, item.toNetworkModel())
        } catch (e: Exception) {
            // Mark for sync when online
            localDataSource.markPendingSync(item.id)
        }
    }
    
    override suspend fun deleteItem(id: String) = withContext(ioDispatcher) {
        localDataSource.deleteItem(id)
        try {
            remoteDataSource.deleteItem(id)
        } catch (e: Exception) {
            // Mark as deleted locally for sync
            localDataSource.markDeleted(id)
        }
    }
    
    override suspend fun sync(): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Fetch latest from remote
            val remoteItems = remoteDataSource.getItems()
            
            // Update local database
            localDataSource.syncItems(remoteItems.map { it.toEntity() })
            
            // Push pending local changes
            val pendingItems = localDataSource.getPendingSyncItems()
            pendingItems.forEach { item ->
                remoteDataSource.updateItem(item.id, item.toNetworkModel())
                localDataSource.clearPendingSync(item.id)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Simple Repository (without offline support)
```kotlin
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> = userDao
        .getCurrentUser()
        .map { it?.toDomainModel() }
        .flowOn(ioDispatcher)
    
    override suspend fun fetchCurrentUser(): User = withContext(ioDispatcher) {
        val networkUser = userApi.getCurrentUser()
        val user = networkUser.toDomainModel()
        userDao.insertUser(user.toEntity())
        user
    }
    
    override suspend fun updateProfile(profile: UserProfile): User = withContext(ioDispatcher) {
        val updatedUser = userApi.updateProfile(profile.toNetworkModel())
        val user = updatedUser.toDomainModel()
        userDao.updateUser(user.toEntity())
        user
    }
}
```

## Data Sources

### Local Data Source (Room DAO wrapper)
```kotlin
interface ItemLocalDataSource {
    fun getItems(): Flow<List<ItemEntity>>
    fun getItem(id: String): Flow<ItemEntity?>
    suspend fun insertItem(item: ItemEntity)
    suspend fun insertItems(items: List<ItemEntity>)
    suspend fun updateItem(item: ItemEntity)
    suspend fun deleteItem(id: String)
    suspend fun deleteAllItems()
}

class ItemLocalDataSourceImpl @Inject constructor(
    private val itemDao: ItemDao,
) : ItemLocalDataSource {
    
    override fun getItems(): Flow<List<ItemEntity>> = itemDao.getAll()
    
    override fun getItem(id: String): Flow<ItemEntity?> = itemDao.getById(id)
    
    override suspend fun insertItem(item: ItemEntity) = itemDao.insert(item)
    
    override suspend fun insertItems(items: List<ItemEntity>) = itemDao.insertAll(items)
    
    override suspend fun updateItem(item: ItemEntity) = itemDao.update(item)
    
    override suspend fun deleteItem(id: String) = itemDao.deleteById(id)
    
    override suspend fun deleteAllItems() = itemDao.deleteAll()
}
```

### Remote Data Source (API wrapper)
```kotlin
interface ItemRemoteDataSource {
    suspend fun getItems(): List<ItemDto>
    suspend fun getItem(id: String): ItemDto
    suspend fun createItem(item: CreateItemRequest): ItemDto
    suspend fun updateItem(id: String, item: UpdateItemRequest): ItemDto
    suspend fun deleteItem(id: String)
}

class ItemRemoteDataSourceImpl @Inject constructor(
    private val itemApi: ItemApi,
) : ItemRemoteDataSource {
    
    override suspend fun getItems(): List<ItemDto> = itemApi.getItems()
    
    override suspend fun getItem(id: String): ItemDto = itemApi.getItem(id)
    
    override suspend fun createItem(item: CreateItemRequest): ItemDto = 
        itemApi.createItem(item)
    
    override suspend fun updateItem(id: String, item: UpdateItemRequest): ItemDto = 
        itemApi.updateItem(id, item)
    
    override suspend fun deleteItem(id: String) = itemApi.deleteItem(id)
}
```

## Model Mapping

### Extension Functions for Mapping
```kotlin
// Network DTO to Domain Model
fun ItemDto.toDomainModel(): Item = Item(
    id = id,
    title = title,
    description = description,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
)

// Domain Model to Entity
fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)

// Entity to Domain Model
fun ItemEntity.toDomainModel(): Item = Item(
    id = id,
    title = title,
    description = description,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

// Domain Model to Network Request
fun Item.toNetworkModel(): CreateItemRequest = CreateItemRequest(
    title = title,
    description = description,
)
```

## Hilt Module for Repositories

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        impl: ItemRepositoryImpl
    ): ItemRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindItemLocalDataSource(
        impl: ItemLocalDataSourceImpl
    ): ItemLocalDataSource
    
    @Binds
    @Singleton
    abstract fun bindItemRemoteDataSource(
        impl: ItemRemoteDataSourceImpl
    ): ItemRemoteDataSource
}
```

## Testing Repositories

### Fake Repository for Testing
```kotlin
class FakeItemRepository : ItemRepository {
    
    private val items = mutableListOf<Item>()
    private val itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    
    private var shouldThrowError = false
    private var errorToThrow: Exception = Exception("Test error")
    
    fun setItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        itemsFlow.value = items.toList()
    }
    
    fun setError(exception: Exception) {
        shouldThrowError = true
        errorToThrow = exception
    }
    
    fun clearError() {
        shouldThrowError = false
    }
    
    override fun getItems(): Flow<List<Item>> {
        if (shouldThrowError) {
            return flow { throw errorToThrow }
        }
        return itemsFlow
    }
    
    override fun getItem(id: String): Flow<Item?> {
        if (shouldThrowError) {
            return flow { throw errorToThrow }
        }
        return itemsFlow.map { list -> list.find { it.id == id } }
    }
    
    override suspend fun createItem(item: Item): Item {
        if (shouldThrowError) throw errorToThrow
        val newItem = item.copy(id = UUID.randomUUID().toString())
        items.add(newItem)
        itemsFlow.value = items.toList()
        return newItem
    }
    
    override suspend fun updateItem(item: Item) {
        if (shouldThrowError) throw errorToThrow
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items[index] = item
            itemsFlow.value = items.toList()
        }
    }
    
    override suspend fun deleteItem(id: String) {
        if (shouldThrowError) throw errorToThrow
        items.removeAll { it.id == id }
        itemsFlow.value = items.toList()
    }
    
    override suspend fun sync(): Result<Unit> {
        if (shouldThrowError) return Result.failure(errorToThrow)
        return Result.success(Unit)
    }
}
```

## Best Practices

1. **Single Source of Truth**: Repository should be the only way to access a data type
2. **Expose Flows**: Use `Flow` for reactive data streams
3. **Handle Offline**: Implement offline-first patterns for better UX
4. **Abstract Data Sources**: Don't expose implementation details (Room, Retrofit)
5. **Use Proper Threading**: Apply `flowOn(ioDispatcher)` for database/network operations
6. **Map at Boundaries**: Convert between layers (DTO → Domain → Entity)
7. **Document Public APIs**: Use KDoc for public interface methods
8. **Create Fake Implementations**: For easier testing without mocks
