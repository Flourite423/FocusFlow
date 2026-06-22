package com.focusflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.repository.SessionRepository
import com.focusflow.data.repository.StatsRepository
import com.focusflow.data.repository.StreakRepository
import com.focusflow.data.repository.TaskRepository
import com.focusflow.data.db.DailyStatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val streakRepository: StreakRepository,
    private val sessionRepository: SessionRepository,
    private val dailyStatsDao: DailyStatsDao,
    private val statsRepository: StatsRepository
) : ViewModel() {

    data class UiState(
        val streakDays: Int = 0,
        val todayTotalMinutes: Int = 0,
        val todayRecommendedTasks: Int = 0,
        val heatmapData: Map<Long, Int> = emptyMap()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // TODO: Load dashboard data
    }
}