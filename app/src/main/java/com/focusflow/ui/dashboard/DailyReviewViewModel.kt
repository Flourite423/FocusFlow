package com.focusflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.entity.DailyStats
import com.focusflow.data.repository.SessionRepository
import com.focusflow.data.repository.StatsRepository
import com.focusflow.data.repository.StreakRepository
import com.focusflow.util.todayStart
import com.focusflow.util.todayEnd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyReviewViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val streakRepository: StreakRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    data class UiState(
        val yesterdayMinutes: Int = 0,
        val yesterdayTasksCompleted: Int = 0,
        val currentStreak: Int = 0,
        val hasData: Boolean = false
    )

    private val yesterday: Long = LocalDate.now().minusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val uiState: StateFlow<UiState> = combine(
        statsRepository.getStatsForDate(yesterday),
        streakRepository.observeStreak()
    ) { stats, streak ->
        UiState(
            yesterdayMinutes = stats?.totalMinutes ?: 0,
            yesterdayTasksCompleted = stats?.tasksCompleted ?: 0,
            currentStreak = streak,
            hasData = stats != null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
}
