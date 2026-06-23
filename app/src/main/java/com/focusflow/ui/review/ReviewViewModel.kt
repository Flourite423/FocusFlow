package com.focusflow.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.entity.ReviewSchedule
import com.focusflow.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    data class ReviewItem(
        val schedule: ReviewSchedule,
        val taskTitle: String
    )

    data class UiState(
        val dueReviews: List<ReviewItem> = emptyList(),
        val dueCount: Int = 0
    )

    val uiState: StateFlow<UiState> = combine(
        reviewRepository.getDueReviews(),
        reviewRepository.getDueReviewCount()
    ) { reviews, count ->
        val items = reviews.map { schedule ->
            val title = reviewRepository.getTaskTitle(schedule.taskId)
            ReviewItem(schedule = schedule, taskTitle = title)
        }
        UiState(dueReviews = items, dueCount = count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun markReviewed(scheduleId: String) {
        viewModelScope.launch {
            reviewRepository.markReviewed(scheduleId)
        }
    }
}
