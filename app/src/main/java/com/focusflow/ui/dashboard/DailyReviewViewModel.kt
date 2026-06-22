package com.focusflow.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DailyReviewViewModel @Inject constructor() : ViewModel() {

    data class UiState(
        val todayStats: String = "",
        val completedTasks: Int = 0,
        val totalTasks: Int = 0,
        val mood: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // TODO: Load daily review data
    }
}