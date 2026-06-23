# Repository Guidelines

## Project Overview

FocusFlow is a **local-first Android learning plan app** built with Kotlin + Jetpack Compose. It helps users create structured study plans, track progress, manage review schedules, and maintain learning streaks. All data is stored locally on-device via Room (SQLite); no server or user accounts required.

**Key references**: `PRD.md` (requirements), `TECH_DESIGN.md` (architecture & data model).

---

## Architecture & Data Flow

**Pattern**: MVVM + Repository (four-layer separation)

```
UI (Compose Screen)
  ↓ collect StateFlow
ViewModel (UiState)
  ↓ calls
Repository
  ↓ calls
Room DAO → SQLite
```

- **UI layer**: Jetpack Compose screens observe `StateFlow<UiState>` from ViewModels using `collectAsStateWithLifecycle()`
- **ViewModel**: Holds `MutableStateFlow<UiState>`, launches coroutines in `viewModelScope`
- **Repository**: Single source of truth, wraps DAO calls, data access only
- **UseCase**: Cross-entity business logic orchestration (only `CompleteTaskUseCase` exists)
- **DAO**: Room interfaces returning `Flow<T>` for reactive queries, `suspend` for writes
- **Service**: `TimerService` (Foreground Service for timer persistence), `ReviewReminderWorker` (WorkManager for scheduled notifications)

**Dependency injection**: Hilt (`@HiltViewModel`, `@Module`, `@Provides`, `@Singleton`).

**State management**: `StateFlow` + `data class UiState` pattern. Compose screens use `collectAsStateWithLifecycle()`.

**Data flow**:
```
创建计划 → 添加里程碑 → 创建任务 → 分配到今天 → 计时学习 → 完成任务（自动创建复习） → 复习打卡 → 仪表盘统计
```

---

## Key Directories

```
app/src/main/java/com/focusflow/
├── di/                  # Hilt modules (AppModule: DB, DAOs, Repositories, UseCases)
├── navigation/          # Jetpack Navigation routes (Screen sealed interface) + bottom tab setup
├── data/
│   ├── db/
│   │   ├── entity/      # Room @Entity classes (Plan, Milestone, Task, DayAssignment, StudySession, ReviewSchedule, ReviewLog, DailyStats)
│   │   ├── dao/         # Room @Dao interfaces (Flow-based queries)
│   │   └── converter/   # Room TypeConverters (enums ↔ String, lists ↔ JSON)
│   ├── repository/      # Repository classes (PlanRepository, TaskRepository, SessionRepository, ReviewRepository, StreakRepository, StatsRepository)
│   └── backup/          # JSON import/export (BackupManager)
├── domain/
│   ├── model/           # MilestoneProgress (domain-level data class)
│   └── usecase/         # CompleteTaskUseCase (task completion + review schedule creation)
├── ui/
│   ├── theme/           # Material 3 theme (Color, Type, Theme) with FocusFlowColors
│   ├── components/      # Shared Compose components (FocusProgressBar, OnboardingGuide)
│   ├── dashboard/       # DashboardScreen + DailyReviewScreen
│   ├── plan/            # PlanListScreen, PlanDetailScreen, WeeklyPlanScreen, DailyPlanScreen
│   ├── timer/           # TimerScreen + Foreground Service binding
│   ├── review/          # ReviewScreen (due reviews + mark reviewed)
│   └── settings/        # SettingsScreen (theme, goal, notifications, freeze, backup)
├── service/
│   ├── TimerService.kt          # Foreground Service (timer persistence)
│   └── ReviewReminderWorker.kt  # WorkManager (daily review notifications)
└── util/
    ├── DateUtils.kt             # Date/epoch conversion (todayEpoch, weekStart, weekEnd, monthStart, monthEnd)
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
```

---

## Code Conventions & Common Patterns

### Kotlin Style

- **Naming**: `PascalCase` for classes, `camelCase` for functions/properties, `SCREAMING_SNAKE` for constants
- **Coroutines**: Use `viewModelScope.launch` in ViewModel, `suspend` functions in Repository/DAO
- **Null safety**: Leverage Kotlin null safety; avoid `!!` — use `?.let {}` or `?: return`
- **Enums**: Use `enum class` with `.value` property and `fromValue()` companion (with safe `firstOrNull` fallback)

### Room Patterns

```kotlin
// Entity: UUID primary key, epoch millis for dates, String for enums
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,       // UUID.randomUUID().toString()
    val status: TaskStatus = TaskStatus.TODO,
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

// Repository: Wraps DAO, no business logic
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val assignmentDao: DayAssignmentDao
) {
    fun getTasksForDate(date: Long): Flow<List<Task>> = taskDao.getTasksForDate(date)
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
- Enum `fromValue()` uses `firstOrNull` with safe defaults (never crashes)

### Theme System

- `FocusFlowTheme` accepts `themeMode: Int` (0=system, 1=light, 2=dark)
- `FocusFlowColors` object defines functional colors (planColor, timerColor, streakColor, etc.)
- Heatmap colors are theme-aware (light/dark variants)
- Theme mode persisted in DataStore, observed in MainActivity

---

## Important Files

|File|Purpose|
|---|---|
|`FocusFlowDatabase.kt`|Room `@Database` definition, singleton instance|
|`AppModule.kt`|Hilt module providing Database, DAOs, Repositories, UseCases|
|`AppNavigation.kt`|NavHost, bottom tab routing (5 tabs: 仪表盘/计划/计时/复习/我的)|
|`TimerService.kt`|Foreground Service for persistent timer|
|`ReviewReminderWorker.kt`|WorkManager for daily review notifications|
|`BackupManager.kt`|JSON export/import with safe null handling|
|`StreakCalculator.kt`|Streak logic (5min threshold, freeze days)|
|`ReviewScheduler.kt`|Review interval calculation (default [1,3,7,14,30] days)|
|`CompleteTaskUseCase.kt`|Task completion → update status + sum sessions + create review schedule|
|`build.gradle.kts` (app)|Dependencies, SDK versions, Room schema config|

---

## Runtime & Tooling

|Tool|Version / Notes|
|---|---|
|**Language**|Kotlin 2.3.20|
|**Min SDK**|API 26 (Android 8.0)|
|**Target SDK**|API 35 (Android 15)|
|**Build**|Gradle 9.6.0 + KSP 2.3.6|
|**AGP**|9.0.1|
|**DI**|Hilt 2.59.2|
|**Database**|Room 2.8.4|
|**UI**|Jetpack Compose (BOM 2026.06.00) + Material 3|
|**Charts**|Vico 2.0.0-beta.1|
|**Serialization**|kotlinx-serialization 1.8.1|
|**DataStore**|Preferences DataStore 1.1.7|
|**JVM Target**|17|
|**IDE**|Android Studio (Ladybug or later)|

---

## Testing & QA

### Unit Tests (`test/`)

- **Framework**: JUnit 5 + MockK
- **What to test**: `StreakCalculator`, `ReviewScheduler`, ViewModel logic, Repository mapping
- **Run**: `./gradlew test`

### Instrumented Tests (`androidTest/`)

- **Framework**: AndroidX Test + Compose UI Testing
- **What to test**: Room DAO queries, Compose screen rendering, Navigation flows
- **Run**: `./gradlew connectedAndroidTest`

### Coverage

- Unit test coverage for `domain/` and `util/` is expected
- DAO tests verify query correctness against in-memory Room database
- No coverage requirement for UI layer (manual QA + Compose preview)

### CI

- GitHub Actions on push to main/develop and PRs to main
- Steps: assembleDebug → testDebugUnitTest → lintDebug (non-blocking)
- Artifacts: debug APK, test report, lint report (7-day retention)
