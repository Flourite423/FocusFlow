package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.data.db.entity.ReviewLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: ReviewLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(logs: List<ReviewLog>)

    @Query("SELECT * FROM review_logs WHERE scheduleId = :scheduleId ORDER BY round ASC")
    fun getByScheduleId(scheduleId: String): Flow<List<ReviewLog>>

    @Delete
    suspend fun delete(log: ReviewLog)

    @Query("DELETE FROM review_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM review_logs ORDER BY reviewedAt DESC")
    suspend fun getAllSync(): List<ReviewLog>
}