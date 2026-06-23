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
import com.focusflow.domain.usecase.CompleteTaskUseCase
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
    fun providePlanRepository(planDao: PlanDao, milestoneDao: MilestoneDao, taskDao: TaskDao): PlanRepository =
        PlanRepository(planDao, milestoneDao, taskDao)

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
    fun provideReviewRepository(scheduleDao: ReviewScheduleDao, logDao: ReviewLogDao, taskDao: TaskDao): ReviewRepository =
        ReviewRepository(scheduleDao, logDao, taskDao)

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
    fun provideCompleteTaskUseCase(taskRepo: TaskRepository, sessionRepo: SessionRepository, reviewRepo: ReviewRepository): CompleteTaskUseCase =
        CompleteTaskUseCase(taskRepo, sessionRepo, reviewRepo)
}
