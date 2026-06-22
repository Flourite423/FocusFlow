package com.focusflow.ui.review

import androidx.lifecycle.ViewModel
import com.focusflow.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    data class UiState(
        val dueReviews: List<com.focusflow.data.db.entity.ReviewSchedule> = emptyList(),
        val dueCount: Int = 0,
        val selectedDateRange: String = "week"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // TODO: Load review data
    }
}