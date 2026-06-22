package com.focusflow.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.entity.ReviewSchedule
import com.focusflow.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    data class UiState(
        val dueReviews: List<ReviewSchedule> = emptyList(),
        val dueCount: Int = 0
    )

    val uiState: StateFlow<UiState> = kotlinx.coroutines.flow.combine(
        reviewRepository.getDueReviews(),
        reviewRepository.getDueReviewCount()
    ) { reviews, count ->
        UiState(dueReviews = reviews, dueCount = count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun markReviewed(scheduleId: String) {
        viewModelScope.launch {
            reviewRepository.markReviewed(scheduleId)
        }
    }
}
