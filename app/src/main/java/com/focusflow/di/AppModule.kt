package com.focusflow.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.focusflow.data.backup.BackupManager
import com.focusflow.data.db.FocusFlowDatabase
import com.focusflow.data.db.dao.DailyStatsDao
import com.focusflow.data.db.dao.DayAssignmentDao
import com.focusflow.data.db.dao.MilestoneDao
import com.focusflow.data.db.dao.PlanDao
import com.focusflow.data.db.dao.ReviewLogDao
import com.focusflow.data.db.dao.ReviewScheduleDao
import com.focusflow.data.db.dao.StudySessionDao
import com.focusflow.data.db.dao.TaskDao
import com.focusflow.data.repository.PlanRepository
import com.focusflow.data.repository.ReviewRepository
import com.focusflow.data.repository.SessionRepository
import com.focusflow.data.repository.StatsRepository
import com.focusflow.data.repository.StreakRepository
import com.focusflow.data.repository.TaskRepository
import com.focusflow.domain.usecase.CalculateStreakUseCase
import com.focusflow.domain.usecase.CompleteTaskUseCase
import com.focusflow.domain.usecase.GetReviewScheduleUseCase
import com.focusflow.domain.usecase.GetTodayTasksUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focusflow_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusFlowDatabase =
        Room.databaseBuilder(
            context,
            FocusFlowDatabase::class.java,
            "focusflow.db"
        ).build()

    @Provides
    @Singleton
    fun providePlanDao(db: FocusFlowDatabase): PlanDao = db.planDao()

    @Provides
    @Singleton
    fun provideMilestoneDao(db: FocusFlowDatabase): MilestoneDao = db.milestoneDao()

    @Provides
    @Singleton
    fun provideTaskDao(db: FocusFlowDatabase): TaskDao = db.taskDao()

    @Provides
    @Singleton
    fun provideDayAssignmentDao(db: FocusFlowDatabase): DayAssignmentDao = db.dayAssignmentDao()

    @Provides
    @Singleton
    fun provideStudySessionDao(db: FocusFlowDatabase): StudySessionDao = db.studySessionDao()

    @Provides
    @Singleton
    fun provideReviewScheduleDao(db: FocusFlowDatabase): ReviewScheduleDao = db.reviewScheduleDao()

    @Provides
    @Singleton
    fun provideReviewLogDao(db: FocusFlowDatabase): ReviewLogDao = db.reviewLogDao()

    @Provides
    @Singleton
    fun provideDailyStatsDao(db: FocusFlowDatabase): DailyStatsDao = db.dailyStatsDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun providePlanRepository(dao: PlanDao): PlanRepository = PlanRepository(dao)

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao, assignmentDao: DayAssignmentDao): TaskRepository =
        TaskRepository(taskDao, assignmentDao)

    @Provides
    @Singleton
    fun provideSessionRepository(sessionDao: StudySessionDao): SessionRepository =
        SessionRepository(sessionDao)

    @Provides
    @Singleton
    fun provideReviewRepository(scheduleDao: ReviewScheduleDao, logDao: ReviewLogDao): ReviewRepository =
        ReviewRepository(scheduleDao, logDao)

    @Provides
    @Singleton
    fun provideStreakRepository(statsDao: DailyStatsDao, prefs: DataStore<Preferences>): StreakRepository =
        StreakRepository(statsDao, prefs)

    @Provides
    @Singleton
    fun provideStatsRepository(statsDao: DailyStatsDao): StatsRepository = StatsRepository(statsDao)

    @Provides
    @Singleton
    fun provideBackupManager(db: FocusFlowDatabase): BackupManager = BackupManager(db)

    @Provides
    fun provideGetTodayTasksUseCase(repo: TaskRepository, assignmentDao: DayAssignmentDao): GetTodayTasksUseCase =
        GetTodayTasksUseCase(repo, assignmentDao)

    @Provides
    fun provideCompleteTaskUseCase(repo: TaskRepository, sessionDao: StudySessionDao): CompleteTaskUseCase =
        CompleteTaskUseCase(repo, sessionDao)

    @Provides
    fun provideCalculateStreakUseCase(repo: StreakRepository): CalculateStreakUseCase =
        CalculateStreakUseCase(repo)

    @Provides
    fun provideGetReviewScheduleUseCase(repo: ReviewRepository): GetReviewScheduleUseCase =
        GetReviewScheduleUseCase(repo)
}
