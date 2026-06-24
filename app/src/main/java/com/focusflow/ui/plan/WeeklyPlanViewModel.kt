package com.focusflow.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.dao.AssignmentWithFullInfo
import com.focusflow.data.db.dao.TaskWithPlanInfo
import com.focusflow.data.repository.TaskRepository
import com.focusflow.util.weekStart
import com.focusflow.util.weekEnd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeeklyPlanViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    data class DayTaskInfo(
        val taskId: String,
        val taskTitle: String,
        val planName: String,
        val milestoneTitle: String,
        val isTemporary: Boolean
    )

    data class UiState(
        val dayTasks: Map<Int, List<DayTaskInfo>> = emptyMap(),
        val totalAssigned: Int = 0,
        val taskPool: List<TaskWithPlanInfo> = emptyList(),
        val selectedDayIndex: Int? = null
    )

    private val weekStartMs = weekStart()
    private val zone = ZoneId.systemDefault()

    // Which day is selected for adding tasks (0=Mon, 6=Sun)
    private val _selectedDay = MutableStateFlow<Int?>(null)

    // Task pool for the selected day (unassigned tasks)
    private val taskPoolFlow = _selectedDay.flatMapLatest { dayIndex ->
        if (dayIndex == null) {
            MutableStateFlow(emptyList())
        } else {
            val date = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .plusDays(dayIndex.toLong())
                .atStartOfDay(zone).toInstant().toEpochMilli()
            taskRepository.getUnassignedTasksForDate(date)
        }
    }

    private val assignmentsFlow = taskRepository.getAssignmentsWithFullInfo(weekStartMs, weekEnd())

    val uiState: StateFlow<UiState> = combine(
        assignmentsFlow,
        taskPoolFlow,
        _selectedDay
    ) { assignments, pool, selectedDay ->
        val grouped = assignments.groupBy { ((it.date - weekStartMs) / (24 * 60 * 60 * 1000)).toInt() }
        val dayInfoMap = grouped.mapValues { (_, dayAssignments) ->
            dayAssignments.map {
                DayTaskInfo(
                    taskId = it.taskId,
                    taskTitle = it.taskTitle,
                    planName = it.planName,
                    milestoneTitle = it.milestoneTitle,
                    isTemporary = it.isTemporary
                )
            }
        }
        UiState(
            dayTasks = dayInfoMap,
            totalAssigned = assignments.size,
            taskPool = pool,
            selectedDayIndex = selectedDay
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun selectDay(dayIndex: Int?) {
        _selectedDay.value = if (_selectedDay.value == dayIndex) null else dayIndex
    }

    fun assignTaskToDay(taskId: String, dayIndex: Int) {
        viewModelScope.launch {
            val date = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .plusDays(dayIndex.toLong())
                .atStartOfDay(zone).toInstant().toEpochMilli()
            taskRepository.assignTaskToDay(taskId, date)
        }
    }

    fun removeTaskFromDay(taskId: String, dayIndex: Int) {
        viewModelScope.launch {
            val date = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .plusDays(dayIndex.toLong())
                .atStartOfDay(zone).toInstant().toEpochMilli()
            taskRepository.removeTaskFromDate(taskId, date)
        }
    }
}
