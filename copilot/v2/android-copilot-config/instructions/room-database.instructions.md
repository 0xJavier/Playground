---
applyTo: "**/*Dao.kt,**/*Database.kt,**/*Entity.kt,**/database/**/*.kt"
---

# Room Database Instructions

## Database Setup

### Database Class
```kotlin
@Database(
    entities = [
        ItemEntity::class,
        UserEntity::class,
        // Add other entities here
    ],
    version = 1,
    exportSchema = true, // Enable for migration testing
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun itemDao(): ItemDao
    abstract fun userDao(): UserDao
}
```

### Database Module (Hilt)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    )
    .addMigrations(MIGRATION_1_2) // Add migrations as needed
    .fallbackToDestructiveMigration() // Remove in production!
    .build()
    
    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao = database.itemDao()
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}
```

## Entity Classes

### Basic Entity
```kotlin
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_synced", defaultValue = "1")
    val isSynced: Boolean = true,
)
```

### Entity with Foreign Key
```kotlin
@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["item_id"]),
    ]
)
data class CommentEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "item_id")
    val itemId: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "author_id")
    val authorId: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
```

### Embedded Objects
```kotlin
data class AddressEmbedded(
    @ColumnInfo(name = "street")
    val street: String,
    
    @ColumnInfo(name = "city")
    val city: String,
    
    @ColumnInfo(name = "zip_code")
    val zipCode: String,
    
    @ColumnInfo(name = "country")
    val country: String,
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @Embedded(prefix = "address_")
    val address: AddressEmbedded?,
)
```

## DAO (Data Access Object)

### Basic DAO
```kotlin
@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY created_at DESC")
    fun getAll(): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE id = :id")
    fun getById(id: String): Flow<ItemEntity?>
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getByIdOnce(id: String): ItemEntity?
    
    @Query("SELECT * FROM items WHERE title LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ItemEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)
    
    @Update
    suspend fun update(item: ItemEntity)
    
    @Delete
    suspend fun delete(item: ItemEntity)
    
    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM items")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM items")
    suspend fun getCount(): Int
    
    @Query("SELECT COUNT(*) FROM items")
    fun observeCount(): Flow<Int>
}
```

### DAO with Transactions
```kotlin
@Dao
interface ItemWithCommentsDao {

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemWithComments(id: String): Flow<ItemWithComments?>
    
    @Transaction
    @Query("SELECT * FROM items")
    fun getAllItemsWithComments(): Flow<List<ItemWithComments>>
    
    @Transaction
    suspend fun replaceItems(items: List<ItemEntity>) {
        deleteAll()
        insertAll(items)
    }
    
    @Query("DELETE FROM items")
    suspend fun deleteAll()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)
}
```

### Relation Classes
```kotlin
data class ItemWithComments(
    @Embedded
    val item: ItemEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    val comments: List<CommentEntity>,
)

data class UserWithItems(
    @Embedded
    val user: UserEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "author_id"
    )
    val items: List<ItemEntity>,
)
```

## Type Converters

```kotlin
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
    
    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString(it) }
    }
    
    @TypeConverter
    fun fromStatus(status: ItemStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toStatus(value: String): ItemStatus {
        return ItemStatus.valueOf(value)
    }
}
```

## Database Migrations

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new column
        database.execSQL(
            "ALTER TABLE items ADD COLUMN category TEXT DEFAULT 'general'"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS tags (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                color TEXT NOT NULL
            )
        """.trimIndent())
        
        // Create junction table for many-to-many
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS item_tags (
                item_id TEXT NOT NULL,
                tag_id TEXT NOT NULL,
                PRIMARY KEY (item_id, tag_id),
                FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
                FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // Create index
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_item_tags_tag_id ON item_tags(tag_id)"
        )
    }
}
```

## Testing Database

### In-Memory Database for Testing
```kotlin
@RunWith(AndroidJUnit4::class)
class ItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var itemDao: ItemDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
        .allowMainThreadQueries() // Only for testing!
        .build()
        
        itemDao = database.itemDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveItem() = runTest {
        // Given
        val item = ItemEntity(
            id = "1",
            title = "Test Item",
            description = "Description",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        
        // When
        itemDao.insert(item)
        
        // Then
        val retrieved = itemDao.getByIdOnce("1")
        assertEquals(item, retrieved)
    }
    
    @Test
    fun flowEmitsUpdates() = runTest {
        // Given
        val item1 = createTestItem("1", "First")
        val item2 = createTestItem("2", "Second")
        
        itemDao.getAll().test {
            // Initial empty state
            assertEquals(emptyList<ItemEntity>(), awaitItem())
            
            // Insert first item
            itemDao.insert(item1)
            assertEquals(listOf(item1), awaitItem())
            
            // Insert second item
            itemDao.insert(item2)
            assertEquals(listOf(item1, item2), awaitItem())
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

## Best Practices

1. **Use Flow for Queries**: Return `Flow` for reactive updates
2. **Add Indices**: For columns used in WHERE and JOIN clauses
3. **Export Schema**: Enable `exportSchema = true` for migration testing
4. **Test Migrations**: Use `MigrationTestHelper` for migration tests
5. **Use Transactions**: For operations that should be atomic
6. **Avoid Main Thread**: Never perform database operations on main thread
7. **Use OnConflictStrategy**: Define behavior for duplicate keys
8. **Keep Entities Simple**: Use embedded objects for complex types
