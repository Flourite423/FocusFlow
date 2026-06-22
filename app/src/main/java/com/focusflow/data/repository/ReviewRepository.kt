package com.focusflow.data.repository

import com.focusflow.data.db.dao.ReviewLogDao
import com.focusflow.data.db.dao.ReviewScheduleDao
import com.focusflow.data.db.entity.ReviewLog
import com.focusflow.data.db.entity.ReviewSchedule
import com.focusflow.util.toEpochMillis
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val scheduleDao: ReviewScheduleDao,
    private val reviewLogDao: ReviewLogDao
) {
    fun getDueReviews(): Flow<List<ReviewSchedule>> {
        return scheduleDao.getDueReviews(LocalDate.now().toEpochMillis())
    }

    fun getReviewsForRange(rangeStart: Long, rangeEnd: Long): Flow<List<ReviewSchedule>> =
        scheduleDao.getReviewsForRange(rangeStart, rangeEnd)

    fun getDueReviewCount(): Flow<Int> {
        return scheduleDao.getDueReviewCount(LocalDate.now().toEpochMillis())
    }

    suspend fun createSchedule(taskId: String, intervals: List<Int> = listOf(1, 3, 7, 14, 30)) {
        val schedule = ReviewSchedule(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            reviewIntervals = intervals.joinToString(","),
            nextReviewDate = LocalDate.now().plusDays(intervals[0].toLong()).toEpochMillis(),
            totalRounds = intervals.size
        )
        scheduleDao.upsert(schedule)
    }

    suspend fun markReviewed(scheduleId: String) {
        val schedule = scheduleDao.getById(scheduleId) ?: return
        val intervals = schedule.reviewIntervals.split(",").map { it.trim().toInt() }
        val nextRound = schedule.currentRound + 1
        val nextDate = if (nextRound < intervals.size) {
            LocalDate.now().plusDays(intervals[nextRound].toLong()).toEpochMillis()
        } else {
            Long.MAX_VALUE
        }
        scheduleDao.markReviewed(scheduleId, System.currentTimeMillis(), nextDate)
        reviewLogDao.upsert(ReviewLog(
            id = UUID.randomUUID().toString(),
            scheduleId = scheduleId,
            round = nextRound,
            reviewedAt = System.currentTimeMillis()
        ))
    }
}
