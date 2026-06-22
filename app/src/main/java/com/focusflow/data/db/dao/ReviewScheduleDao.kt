package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.data.db.entity.ReviewSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewScheduleDao {
    @Query("SELECT * FROM review_schedules WHERE status = 'active' AND nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    fun getDueReviews(now: Long): Flow<List<ReviewSchedule>>

    @Query("SELECT * FROM review_schedules WHERE nextReviewDate BETWEEN :startDate AND :endDate ORDER BY nextReviewDate ASC")
    fun getReviewsForRange(startDate: Long, endDate: Long): Flow<List<ReviewSchedule>>

    @Query("SELECT COUNT(*) FROM review_schedules WHERE status = 'active' AND nextReviewDate <= :now")
    fun getDueReviewCount(now: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: ReviewSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedules: List<ReviewSchedule>)

    @Query("""
        UPDATE review_schedules
        SET currentRound = currentRound + 1,
            lastReviewDate = :now,
            nextReviewDate = CASE
                WHEN currentRound + 1 >= totalRounds THEN 0
                ELSE :nextReviewDate
            END,
            status = CASE
                WHEN currentRound + 1 >= totalRounds THEN 'completed'
                ELSE 'active'
            END,
            updatedAt = :now
        WHERE id = :id
    """)
    suspend fun markReviewed(id: String, now: Long, nextReviewDate: Long)

    @Query("SELECT * FROM review_schedules WHERE id = :id")
    suspend fun getById(id: String): ReviewSchedule?

    @Delete
    suspend fun delete(schedule: ReviewSchedule)

    @Query("DELETE FROM review_schedules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM review_schedules ORDER BY nextReviewDate ASC")
    suspend fun getAllSync(): List<ReviewSchedule>

    @Query("SELECT COUNT(*) FROM review_schedules WHERE nextReviewDate <= :today AND status = 'active'")
    suspend fun getDueReviewCountSync(today: Long): Int
}
