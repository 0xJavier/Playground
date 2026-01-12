---
applyTo: "**/*Module.kt,**/di/**/*.kt,**/*Component.kt"
---

# Hilt Dependency Injection Instructions

## Basic Setup

### Application Class
```kotlin
@HiltAndroidApp
class MainApplication : Application() {
    // Hilt will generate the application component
}
```

### Activity Setup
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Hilt can inject dependencies here
}
```

## Module Patterns

### Singleton Scoped Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
```

### Interface Bindings Module
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
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
```

### API Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideItemApi(retrofit: Retrofit): ItemApi {
        return retrofit.create(ItemApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }
}
```

### Database Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }
    
    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao {
        return database.itemDao()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
```

### Dispatcher Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

// Qualifier annotations
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainDispatcher
```

## ViewModel Injection

### HiltViewModel
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val getUserUseCase: GetUserUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // ViewModel implementation
}
```

### Usage in Compose
```kotlin
@Composable
fun FeatureRoute(
    viewModel: FeatureViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    FeatureScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}
```

### Assisted Injection
```kotlin
// For ViewModels that need runtime parameters
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val itemId: String,
    private val repository: ItemRepository,
) : ViewModel() {
    
    @AssistedFactory
    interface Factory {
        fun create(itemId: String): DetailViewModel
    }
}

// Usage
@Composable
fun DetailRoute(
    itemId: String,
    viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(itemId) }
    ),
) {
    // ...
}
```

## Scopes

### Available Scopes
```kotlin
// Application-wide singleton
@Singleton
@InstallIn(SingletonComponent::class)

// Activity lifecycle
@ActivityScoped
@InstallIn(ActivityComponent::class)

// ViewModel lifecycle
@ViewModelScoped
@InstallIn(ViewModelComponent::class)

// Activity Retained (survives configuration changes)
@ActivityRetainedScoped
@InstallIn(ActivityRetainedComponent::class)

// Service lifecycle
@ServiceScoped
@InstallIn(ServiceComponent::class)
```

### Choosing the Right Scope
```kotlin
// Singleton - Shared across entire app
@Singleton // Database, Retrofit, OkHttpClient, Repositories

// ViewModelScoped - Shared within a ViewModel
@ViewModelScoped // Use cases that cache data for a specific screen

// No scope - New instance every time (default)
// Lightweight objects, mappers, utilities
```

## Qualifiers

### Named Qualifiers
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("authenticated")
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    @Named("public")
    fun providePublicOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
}

// Usage
class ApiService @Inject constructor(
    @Named("authenticated") private val client: OkHttpClient,
)
```

### Custom Qualifiers (Preferred)
```kotlin
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthenticatedClient

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    @PublicClient
    fun providePublicOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
}

// Usage
class ApiService @Inject constructor(
    @AuthenticatedClient private val client: OkHttpClient,
)
```

## Testing with Hilt

### Test Module
```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class TestRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        impl: FakeItemRepository
    ): ItemRepository
}
```

### Instrumented Test Setup
```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FeatureScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Inject
    lateinit var repository: ItemRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun testFeatureScreen() {
        // Test implementation
    }
}
```

### Custom Test Runner
```kotlin
// In androidTest/kotlin/com/example/app/
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

// In build.gradle.kts
android {
    defaultConfig {
        testInstrumentationRunner = "com.example.app.HiltTestRunner"
    }
}
```

## Multi-Module Setup

### Feature Module
```kotlin
// feature/featurename/di/FeatureModule.kt
@Module
@InstallIn(ViewModelComponent::class)
abstract class FeatureModule {

    @Binds
    @ViewModelScoped
    abstract fun bindFeatureRepository(
        impl: FeatureRepositoryImpl
    ): FeatureRepository
}
```

### Core Module
```kotlin
// core/data/di/DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    // Provides shared data dependencies
}
```

## Best Practices

1. **Use `@Binds` over `@Provides`** for interface implementations (more efficient)
2. **Prefer custom qualifiers** over `@Named` (type-safe, refactor-friendly)
3. **Choose appropriate scopes** - don't over-scope (memory leaks)
4. **Keep modules focused** - one module per concern
5. **Use `@InstallIn` explicitly** - makes scope clear
6. **Test with fakes** - Use `@TestInstallIn` to replace real implementations
7. **Document dependencies** - Comment non-obvious dependency choices
8. **Use constructor injection** - Prefer over field injection for testability
