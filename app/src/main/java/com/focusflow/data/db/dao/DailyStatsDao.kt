package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusflow.data.db.entity.DailyStats
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getByDate(date: Long): Flow<DailyStats?>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getByDateSync(date: Long): DailyStats?

    @Query("SELECT * FROM daily_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getDateRange(startDate: Long, endDate: Long): Flow<List<DailyStats>>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DailyStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: DailyStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(statsList: List<DailyStats>)

    @Query("UPDATE daily_stats SET totalMinutes = :totalMinutes, tasksCompleted = :tasksCompleted, reviewsDone = :reviewsDone, streakDays = :streakDays, updatedAt = :updatedAt WHERE date = :date")
    suspend fun update(date: Long, totalMinutes: Int, tasksCompleted: Int, reviewsDone: Int, streakDays: Int, updatedAt: Long)

    @Delete
    suspend fun delete(stats: DailyStats)

    @Query("DELETE FROM daily_stats WHERE date = :date")
    suspend fun deleteByDate(date: Long)

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    suspend fun getAllSync(): List<DailyStats>
}