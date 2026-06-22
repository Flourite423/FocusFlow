package com.focusflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.dao.DailyStatsDao
import com.focusflow.data.db.entity.Task
import com.focusflow.data.repository.SessionRepository
import com.focusflow.data.repository.StatsRepository
import com.focusflow.data.repository.StreakRepository
import com.focusflow.data.repository.TaskRepository
import com.focusflow.util.todayStart
import com.focusflow.util.todayEnd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val streakRepository: StreakRepository,
    private val sessionRepository: SessionRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    data class UiState(
        val streakDays: Int = 0,
        val todayMinutes: Int = 0,
        val todayTasks: List<Task> = emptyList(),
        val completedTasks: Int = 0,
        val totalTasks: Int = 0,
        val weekMinutes: Int = 0,
        val heatmapData: Map<Long, Int> = emptyMap()
    )

    val uiState: StateFlow<UiState> = combine(
        streakRepository.observeStreak(),
        sessionRepository.getTotalMinutesForDay(todayStart(), todayEnd()),
        taskRepository.getRecommendedTasks(),
        statsRepository.getHeatmapData(16)
    ) { streak, minutes, tasks, heatmap ->
        UiState(
            streakDays = streak,
            todayMinutes = minutes,
            todayTasks = tasks,
            completedTasks = tasks.count { it.status.value == "done" },
            totalTasks = tasks.size,
            heatmapData = heatmap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
}
