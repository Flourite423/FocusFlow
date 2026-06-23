package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusflow.data.db.entity.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getByTaskId(taskId: String): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime ASC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<StudySession>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions WHERE startTime BETWEEN :startOfDay AND :endOfDay")
    fun getTotalMinutesForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions WHERE startTime BETWEEN :startOfWeek AND :endOfWeek")
    fun getTotalMinutesForWeek(startOfWeek: Long, endOfWeek: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: StudySession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(sessions: List<StudySession>)

    @Query("UPDATE study_sessions SET endTime = :endTime, durationMinutes = :duration, note = :note, mood = :mood WHERE id = :id")
    suspend fun finish(id: String, endTime: Long, duration: Int, note: String, mood: String?)

    @Delete
    suspend fun delete(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    suspend fun getAllSync(): List<StudySession>
    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}