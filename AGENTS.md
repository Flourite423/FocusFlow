# Repository Guidelines

## Project Overview

FocusFlow is a **local-first Android learning plan app** built with Kotlin + Jetpack Compose. It helps users create structured study plans, track progress, manage review schedules, and maintain learning streaks. All data is stored locally on-device via Room (SQLite); no server or user accounts required.

**Key references**: `PRD.md` (requirements), `TECH_DESIGN.md` (architecture & data model), `RESEARCH.md` (background research).

---

## Architecture & Data Flow

**Pattern**: MVVM + Repository (four-layer separation)

```
UI (Compose Screen)
  ↓ collect StateFlow
ViewModel (UiState)
  ↓ calls
UseCase / Domain
  ↓ calls
Repository
  ↓ calls
Room DAO → SQLite
```

- **UI layer**: Jetpack Compose screens observe `StateFlow<UiState>` from ViewModels
- **ViewModel**: Holds `MutableStateFlow<UiState>`, launches coroutines in `viewModelScope`
- **Repository**: Single source of truth, wraps DAO calls, contains business logic
- **DAO**: Room interfaces returning `Flow<T>` for reactive queries
- **Service**: `TimerService` (Foreground Service for timer persistence), `ReviewReminderWorker` (WorkManager for scheduled notifications)

**Dependency injection**: Hilt (`@HiltViewModel`, `@Module`, `@Provides`).

**State management**: `StateFlow` + `data class UiState` pattern. Compose screens use `collectAsStateWithLifecycle()`.

---

## Key Directories

```
app/src/main/java/com/focusflow/
├── di/                  # Hilt modules (DB, Repository, DAO providers)
├── navigation/          # Jetpack Navigation routes + bottom tab setup
├── data/
│   ├── db/
│   │   ├── entity/      # Room @Entity classes (Plan, Task, StudySession, etc.)
│   │   ├── dao/         # Room @Dao interfaces (Flow-based queries)
│   │   └── converter/   # Room TypeConverters
│   ├── repository/      # Repository classes (business logic + DAO delegation)
│   └── backup/          # JSON import/export (BackupManager)
├── domain/
│   ├── model/           # Business models (optional, separate from Entity)
│   └── usecase/         # Use cases (GetTodayTasks, CalculateStreak, etc.)
├── ui/
│   ├── theme/           # Material 3 theme (Color, Type, Theme)
│   ├── components/      # Shared Compose components (StreakBadge, HeatmapCalendar, TaskCard)
│   ├── dashboard/       # Dashboard screen + ViewModel
│   ├── plan/            # Plan CRUD, weekly/daily planning screens
│   ├── timer/           # Timer screen + Foreground Service binding
│   ├── review/          # Review schedule + calendar view
│   └── settings/        # App settings + data backup
├── service/
│   ├── TimerService.kt          # Foreground Service (timer persistence)
│   └── ReviewReminderWorker.kt  # WorkManager (daily review notifications)
└── util/
    ├── DateUtils.kt             # Date/epoch conversion helpers
    ├── StreakCalculator.kt      # Streak logic (5min threshold, freeze days)
    └── ReviewScheduler.kt       # Review interval calculation
```

---

## Development Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run all unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.focusflow.util.StreakCalculatorTest"

# Clean build
./gradlew clean assembleDebug

# Check lint
./gradlew lint

# Generate Room schema export (for migration testing)
# Already configured via room { schemaDirectory("$projectDir/schemas") }
```

---

## Code Conventions & Common Patterns

### Kotlin Style

- **Naming**: `PascalCase` for classes, `camelCase` for functions/properties, `SCREAMING_SNAKE` for constants
- **Coroutines**: Use `viewModelScope.launch` in ViewModel, `suspend` functions in Repository/DAO
- **Null safety**: Leverage Kotlin null safety; avoid `!!` — use `?.let {}` or `?: return`
- **Enums**: Use `object` with `const val` strings for Room-stored enums (see `TaskStatus`, `Priority`, `PlanStatus`)

### Room Patterns

```kotlin
// Entity: UUID primary key, epoch millis for dates, string for enums
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,       // UUID.randomUUID().toString()
    val status: String = "todo",      // Use TaskStatus constants
    val createdAt: Long = System.currentTimeMillis(),
    ...
)

// DAO: Return Flow for reactive queries, suspend for writes
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE milestoneId = :id")
    fun getByMilestoneId(id: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: Task)
}

// Repository: Business logic on top of DAO
class TaskRepository(private val taskDao: TaskDao) {
    fun getTasksForDate(date: Long): Flow<List<Task>> =
        taskDao.getTasksForDate(date)
}
```

### Compose Patterns

```kotlin
// Screen: Collect UiState, delegate events to ViewModel
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(uiState = uiState, onTaskClick = viewModel::completeTask)
}

// ViewModel: StateFlow + data class UiState
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getTodayTasks().collect { tasks ->
                _uiState.update { it.copy(todayTasks = tasks) }
            }
        }
    }
}
```

### Date Handling

- All dates stored as `Long` (epoch millis) in Room
- Use `LocalDate` for date-only logic, convert via extension functions in `DateUtils.kt`
- Day boundaries: midnight in device local timezone
- Week boundaries: Monday 00:00 to Sunday 23:59

### Error Handling

- DAO operations wrapped in Room transactions for multi-table writes
- Repository functions are `suspend` — exceptions propagate to ViewModel
- ViewModel catches exceptions in `viewModelScope.launch` and updates `UiState.error`
- UI shows `Snackbar` for user-facing errors

---

## Important Files

| File | Purpose |
|------|---------|
| `FocusFlowDatabase.kt` | Room `@Database` definition, singleton instance |
| `AppModule.kt` | Hilt module providing Database, DAOs, Repositories |
| `AppNavigation.kt` | NavHost, bottom tab routing |
| `TimerService.kt` | Foreground Service for persistent timer |
| `ReviewReminderWorker.kt` | WorkManager for daily review notifications |
| `BackupManager.kt` | JSON export/import with schema versioning |
| `StreakCalculator.kt` | Streak logic (5min threshold, freeze days, monthly reset) |
| `ReviewScheduler.kt` | Review interval calculation (default [1,3,7,14,30] days) |
| `build.gradle.kts` (app) | Dependencies, SDK versions, Room schema config |

---

## Runtime & Tooling

| Tool | Version / Notes |
|------|----------------|
| **Language** | Kotlin 2.0+ |
| **Min SDK** | API 26 (Android 8.0) |
| **Target SDK** | API 35 (latest stable) |
| **Build** | Gradle 8.x + KSP (for Room annotation processing) |
| **DI** | Hilt 2.x |
| **Database** | Room 2.6.x |
| **UI** | Jetpack Compose (BOM 2024.12+) + Material 3 |
| **Charts** | Vico 2.x (Compose-native) |
| **Serialization** | kotlinx-serialization (JSON backup) |
| **DataStore** | Preferences DataStore (user settings) |
| **IDE** | Android Studio (Ladybug or later) |

---

## Testing & QA

### Unit Tests (`test/`)

- **Framework**: JUnit 5 + MockK
- **What to test**: `StreakCalculator`, `ReviewScheduler`, ViewModel logic, Repository mapping
- **Run**: `./gradlew test`

```kotlin
@Test
fun `streak continues through freeze day`() {
    val stats = listOf(
        DailyStats(date = today, totalMinutes = 30),
        DailyStats(date = yesterday, totalMinutes = 0), // missed
        DailyStats(date = twoDaysAgo, totalMinutes = 20)
    )
    val streak = StreakCalculator.calculate(stats, freezeUsed = 0, freezeLimit = 2)
    assertEquals(3, streak) // yesterday was frozen
}
```

### Instrumented Tests (`androidTest/`)

- **Framework**: AndroidX Test + Compose UI Testing
- **What to test**: Room DAO queries, Compose screen rendering, Navigation flows
- **Run**: `./gradlew connectedAndroidTest`

### Coverage

- Unit test coverage for `domain/` and `util/` is expected
- DAO tests verify query correctness against in-memory Room database
- No coverage requirement for UI layer (manual QA + Compose preview)
