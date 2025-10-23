# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Build & Development Commands

### Build
```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Build release APK
.\gradlew.bat assembleRelease

# Clean build
.\gradlew.bat clean
```

### Run Tests
```powershell
# Run unit tests
.\gradlew.bat test

# Run Android instrumented tests
.\gradlew.bat connectedAndroidTest

# Run specific test
.\gradlew.bat test --tests "com.kiranawala.domain.use_cases.*"
```

### Debugging
- **View logs**: Use Logcat filtering for `KiranaLogger` tag
- **Supabase**: Check database/auth in Supabase Dashboard at `https://fnblhmddgregqfafqkeh.supabase.co`
- **Test OTP**: Development uses hardcoded test OTP (check Supabase dashboard)

## Architecture Overview

### Clean Architecture Pattern
The app follows **MVVM + Clean Architecture** with strict layer separation:

```
UI Layer (Compose) → ViewModel (StateFlow) → Repository → Data Sources (Supabase + Room)
```

**Key principle**: Data flows unidirectionally from data sources through repositories and ViewModels to UI. UI emits events/actions that flow back down through the layers.

### Module Organization

#### `presentation/` - UI Layer
- **Screens**: Full-screen Composables organized by feature (auth, store, cart, order, profile)
- **Components**: Reusable UI components (common, product, store)
- **ViewModels**: State management with StateFlow, never directly access repositories from UI
- **Navigation**: Type-safe navigation using Routes sealed class

#### `domain/` - Business Logic Layer
- **Models**: Pure Kotlin data classes (Customer, Store, Product, Order, Cart, etc.)
- **Repositories**: Interfaces defining data operations (never implementations)
- **UseCases**: Single-responsibility business operations (AddToCart, PlaceOrder, SendOTP, etc.)

#### `data/` - Data Layer
- **Repositories**: Concrete implementations of domain repository interfaces
- **Remote**: Supabase API integration (Postgrest for DB, Auth for authentication)
- **Local**: Room database entities, DAOs, and encrypted storage
- **Services**: Background services (LocalNotificationService)

#### `di/` - Dependency Injection
- **AppModule**: Core app dependencies (Context, PreferencesManager, NetworkConnectivityManager)
- **SupabaseModule**: Supabase client, Auth, Postgrest, Storage
- **DatabaseModule**: Room database and DAOs
- **RepositoryModule**: Repository implementations binding

#### `utils/` - Cross-cutting Concerns
- **SessionManager**: User session and authentication state management
- **Validators**: Input validation (phone, email, password)
- **Logger**: Centralized logging through `KiranaLogger`
- **Extensions**: Kotlin extension functions (MoneyExtensions, StringExtensions)

### State Management Strategy

**StateFlow pattern** for reactive UI updates:
- ViewModels expose `StateFlow<State>` for UI state
- UI collects StateFlow using `collectAsState()`
- State classes are sealed/data classes representing Loading, Success, Error states
- Example: `CartState.Success(cart)`, `CartState.Loading`, `CartState.Error(message)`

### Data Flow Patterns

#### Offline-First Architecture
1. **Write**: Save to local Room database first, then sync to Supabase
2. **Read**: Fetch from Room, refresh from Supabase in background
3. **Sync**: NetworkConnectivityManager monitors connectivity, triggers sync when online

#### Repository Pattern Implementation
- Repositories coordinate between local (Room) and remote (Supabase) data sources
- Emit `Flow<T>` for reactive data streams
- Return `Result<T>` for one-time operations (wraps Success/Failure)

### Authentication Flow

**Phone OTP Authentication** (Supabase Auth):
1. User enters phone number → `SendOTPUseCase`
2. Supabase sends OTP (test mode uses hardcoded OTP)
3. User verifies OTP → `VerifyOTPUseCase`
4. Session managed by `SessionManager` with encrypted token storage
5. `EncryptedStorageManager` uses AndroidX Security Crypto for sensitive data

### Navigation Architecture

- **NavigationGraph.kt**: Centralized navigation definition
- **Routes.kt**: Type-safe route definitions (sealed class)
- Session-aware navigation: checks `SessionManager.getCurrentUserId()` to redirect to auth if needed
- Cart state snapshot pattern in checkout to prevent Flow lifecycle issues

## Supabase Configuration

**BuildConfig fields** (in `app/build.gradle.kts`):
```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://fnblhmddgregqfafqkeh.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJ...\"")
```

**Key tables**:
- `customers`: User profiles with location data
- `stores`: Store information with geolocation
- `products`: Product catalog with inventory
- `orders`: Order records with customer/store relationships
- `order_items`: Individual items in orders
- `addresses`: Customer delivery addresses

## Important Development Notes

### Testing
- **No test framework configured yet** - Phase 11 pending
- Manual testing workflow documented in README.md
- Test with Supabase test mode OTP for authentication

### Known Issues
1. **Profile edit navigation**: Not working properly, tracked in README
2. **Firebase FCM**: google-services.json may need configuration for push notifications
3. **Payment integration**: Postponed (Razorpay not implemented)

### Code Conventions
- Kotlin official style guide
- Hilt for all dependency injection (use `@HiltViewModel` for ViewModels)
- Timber for logging (wrapped in `KiranaLogger`)
- Coroutines + Flow for async operations (never use callbacks)
- Material Design 3 components throughout UI
- All network calls must have error handling with `Result<T>` wrapper

### Critical Files
- `SessionManager.kt`: User authentication state, required for most operations
- `AppDatabase.kt`: Room database schema (version 1)
- `SupabaseModule.kt`: Supabase client configuration with JSON serialization settings
- `NavigationGraph.kt`: All screen routes and navigation logic
- `Routes.kt`: Navigation route constants

## Common Tasks

### Adding a new screen
1. Create Composable in `presentation/screens/<feature>/`
2. Create ViewModel extending AndroidViewModel with `@HiltViewModel`
3. Add route to `Routes.kt`
4. Add composable to `NavigationGraph.kt`
5. Inject required use cases via Hilt constructor injection

### Adding a new data entity
1. Create domain model in `domain/models/`
2. Create Room entity in `data/local/entities/`
3. Create DAO in `data/local/dao/`
4. Add to `AppDatabase` entities list
5. Increment database version and provide migration
6. Update repository to use new DAO

### Working with Supabase
- Use `client.postgrest` for database operations
- Use `client.auth` for authentication
- All Supabase calls return `Result<T>` or throw exceptions (wrap in try-catch)
- JSON serialization configured to `ignoreUnknownKeys = true`

### Handling authentication
- Always check `sessionManager.getCurrentUserId()` for user-specific operations
- Cart/Order operations require `customerId` parameter
- Logout clears both session and local encrypted storage
- Auth tokens stored via `EncryptedStorageManager`
