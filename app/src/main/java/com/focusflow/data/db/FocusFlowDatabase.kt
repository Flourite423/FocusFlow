package com.focusflow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.focusflow.data.db.converter.Converters
import com.focusflow.data.db.dao.DailyStatsDao
import com.focusflow.data.db.dao.DayAssignmentDao
import com.focusflow.data.db.dao.MilestoneDao
import com.focusflow.data.db.dao.PlanDao
import com.focusflow.data.db.dao.ReviewLogDao
import com.focusflow.data.db.dao.ReviewScheduleDao
import com.focusflow.data.db.dao.StudySessionDao
import com.focusflow.data.db.dao.TaskDao
import com.focusflow.data.db.entity.DailyStats
import com.focusflow.data.db.entity.DayAssignment
import com.focusflow.data.db.entity.Milestone
import com.focusflow.data.db.entity.Plan
import com.focusflow.data.db.entity.ReviewLog
import com.focusflow.data.db.entity.ReviewSchedule
import com.focusflow.data.db.entity.StudySession
import com.focusflow.data.db.entity.Task

@Database(
    entities = [
        Plan::class,
        Milestone::class,
        Task::class,
        DayAssignment::class,
        StudySession::class,
        ReviewSchedule::class,
        ReviewLog::class,
        DailyStats::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FocusFlowDatabase : RoomDatabase() {

    abstract fun planDao(): PlanDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun taskDao(): TaskDao
    abstract fun dayAssignmentDao(): DayAssignmentDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun reviewScheduleDao(): ReviewScheduleDao
    abstract fun reviewLogDao(): ReviewLogDao
    abstract fun dailyStatsDao(): DailyStatsDao

    companion object {
        @Volatile
        private var INSTANCE: FocusFlowDatabase? = null

        fun getInstance(context: Context): FocusFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusFlowDatabase::class.java,
                    "focusflow_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}