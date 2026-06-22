package com.focusflow.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.dao.DayAssignmentDao
import com.focusflow.data.repository.TaskRepository
import com.focusflow.util.weekStart
import com.focusflow.util.weekEnd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WeeklyPlanViewModel @Inject constructor(
    private val assignmentDao: DayAssignmentDao,
    private val taskRepository: TaskRepository
) : ViewModel() {

    data class UiState(
        val dayTasks: Map<Int, List<com.focusflow.data.db.entity.DayAssignment>> = emptyMap(),
        val totalAssigned: Int = 0
    )

    val uiState: StateFlow<UiState> = assignmentDao.getByWeek(weekStart(), weekEnd()).map { assignments ->
        val grouped = assignments.groupBy { ((it.date - weekStart()) / (24 * 60 * 60 * 1000)).toInt() }
        UiState(dayTasks = grouped, totalAssigned = assignments.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
}
