# AGENTS.md

Guidelines for AI coding agents working in this Android codebase.

**Toujours répondre en français à l'utilisateur.**

## Build & Run Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (unsigned)
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug

# Build + install + launch on connected device
bash build-and-run.sh              # debug
bash build-and-run.sh --clean      # clean first
bash build-and-run.sh --release    # release variant

# ADB device check
adb devices

# App logs
adb logcat -s ScoresKeeper
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Testing

No test suite exists yet. The project has `testInstrumentationRunner` declared but no test files or test dependencies.

## Project Structure

Single-module Android app using **Clean Architecture** (3 layers):

```
com.scoreskeeper/
├── domain/
│   ├── model/          # Pure Kotlin data classes (Game, Player, Session, etc.)
│   ├── repository/     # Repository interfaces (abstraction boundary)
│   └── usecase/        # Single-responsibility use cases (operator fun invoke)
├── data/
│   ├── local/          # Room DB: entities, DAOs, mapper extensions
│   ├── repository/     # Repository implementations
│   ├── di/             # Hilt modules (DatabaseModule, RepositoryModule)
│   └── backup/         # Google Drive sync (DriveBackupService, SyncManager, GoogleAuthHelper)
└── presentation/
    ├── navigation/     # NavGraph, Screen sealed class (string routes)
    ├── theme/          # Color, Theme, Typography
    ├── components/     # Reusable composables (PlayerAvatar, ScoreRow)
    └── screens/        # Feature screens: home/, game/, session/, settings/
```

## Tech Stack

- **Language:** Kotlin 2.0.20, JVM target 17
- **UI:** Jetpack Compose (BOM 2024.11.00), Material 3
- **DI:** Hilt 2.52 (KSP annotation processing)
- **Database:** Room 2.6.1 (`scores_keeper.db`, version 1, no migrations)
- **Navigation:** Navigation Compose 2.8.4
- **Charts:** Vico 2.0.0-alpha.28
- **Async:** Kotlin Coroutines + Flow
- **Preferences:** DataStore Preferences
- **Backup:** Google Drive REST API v3, Credential Manager
- **SDK:** minSdk 26, targetSdk 35, compileSdk 35

## Code Style

### Formatting
- **4-space indentation**, no tabs
- **Trailing commas** on all multi-line lists (parameters, properties, collections)
- `kotlin.code.style=official` (Gradle property)

### Imports
- **Wildcard imports** allowed in: Room DAOs (`androidx.room.*`), Compose screens (`androidx.compose.foundation.layout.*`, `androidx.compose.material3.*`, `androidx.compose.runtime.*`, `androidx.compose.material.icons.filled.*`), coroutine flows (`kotlinx.coroutines.flow.*`)
- **Explicit imports** in domain and data layers (models, repositories, mappers)

### Naming Conventions
| Element | Convention | Example |
|---|---|---|
| Classes | PascalCase | `GameRepositoryImpl`, `HomeViewModel` |
| Files | Match primary class | `GameDetailScreen.kt` |
| Functions | camelCase | `getAllGames()`, `onNameChange()` |
| Variables | camelCase | `gameToDelete`, `syncState` |
| Constants | UPPER_SNAKE_CASE in `companion object` | `SYNC_DEBOUNCE_MS`, `DB_NAME` |
| DB tables | snake_case | `round_scores`, `session_players` |
| Nav routes | snake_case strings | `"create_game"`, `"game/{gameId}"` |
| Entities | `*Entity` suffix | `GameEntity`, `PlayerEntity` |
| DAOs | `*Dao` suffix | `GameDao`, `SessionDao` |
| Repositories | `*Repository` / `*RepositoryImpl` | `GameRepository` / `GameRepositoryImpl` |
| Use cases | `*UseCase` suffix | `CreateGameUseCase` |
| ViewModels | `*ViewModel` suffix | `SessionViewModel` |
| UI state | `*UiState` suffix | `CreateGameUiState` |
| Screens | `*Screen` suffix | `HomeScreen` |

### Language
- **UI strings are in French**, hardcoded in Composables (not in `strings.xml`)
- **Code identifiers are in English** (class names, functions, variables)

## Architecture Patterns

### Dependency Injection (Hilt)
- `@HiltAndroidApp` on `ScoresKeeperApp`, `@AndroidEntryPoint` on `MainActivity`
- `@HiltViewModel @Inject constructor(...)` on all ViewModels
- `@Inject constructor` on use cases, repository impls, services
- `@Singleton` on services (`DriveBackupService`, `SyncManager`, etc.)
- `DatabaseModule`: `@Provides` for Room DB and DAOs
- `RepositoryModule`: `@Binds` for interface-to-implementation bindings

### Use Cases
- One class per use case, single responsibility
- `operator fun invoke()` — callable syntax: `val games = getAllGamesUseCase()`
- `suspend` for writes, `Flow` return for reads
- No base class, no Result wrapper

### ViewModels & State
- `MutableStateFlow` + `_state` / `state` pattern with `.asStateFlow()`
- Single `*UiState` data class per screen
- State updates: `_uiState.update { it.copy(...) }`
- Flow to StateFlow: `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)`
- Nav args via `SavedStateHandle`: `checkNotNull(savedStateHandle["gameId"])`

### Compose UI
- `collectAsStateWithLifecycle()` for StateFlow collection
- `hiltViewModel()` for ViewModel injection
- `Modifier` as last parameter with default `Modifier`
- Navigation via hoisted lambdas (`onNavigateBack`, `onNavigateToGame`)
- Private composables for sub-sections within screen files
- `@OptIn(ExperimentalMaterial3Api::class)` as needed

### Room Database
- Entities with `@PrimaryKey(autoGenerate = true)`, `Long` IDs default `0`
- Foreign keys with `CASCADE` delete, indexed FK columns
- DAOs return `Flow<List<*>>` for observable queries, `suspend` for writes
- Entity ↔ Domain mapping via extension functions in `EntityMapper.kt`
- `OnConflictStrategy.REPLACE` for inserts

### Error Handling
- Try-catch with `Log.e(TAG, ...)` for I/O operations (backup, sync)
- Early return with `?: return` for null guards
- `when` on sealed classes for result handling (`BackupResult`, `GoogleSignInResult`)
- Validation via early return in ViewModels (`if (state.name.isBlank()) return`)
- No custom exception classes

## Build Configuration Notes

- Version catalog: `gradle/libs.versions.toml`
- Packaging exclusions for Google API conflicts: `META-INF/INDEX.LIST`, `META-INF/DEPENDENCIES`, etc.
- ProGuard rules in `app/proguard-rules.pro` (keeps Room entities, `PlayerPointsResult`)
- Release builds have `isMinifyEnabled = true`
- Edge-to-edge enabled via `enableEdgeToEdge()` in `MainActivity`
