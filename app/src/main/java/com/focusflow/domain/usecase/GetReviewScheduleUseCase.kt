package com.focusflow.domain.usecase

import com.focusflow.data.db.entity.ReviewSchedule
import com.focusflow.data.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReviewScheduleUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {

    fun getDueReviews(): Flow<List<ReviewSchedule>> = reviewRepository.getDueReviews()

    fun getReviewsForRange(rangeStart: Long, rangeEnd: Long): Flow<List<ReviewSchedule>> =
        reviewRepository.getReviewsForRange(rangeStart, rangeEnd)

    fun getDueReviewCount(): Flow<Int> = reviewRepository.getDueReviewCount()

    suspend fun createSchedule(taskId: String, intervals: List<Int> = listOf(1, 3, 7, 14, 30)) =
        reviewRepository.createSchedule(taskId, intervals)

    suspend fun markReviewed(scheduleId: String) = reviewRepository.markReviewed(scheduleId)
}