package com.focusflow.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.dao.TaskWithPlanInfo
import com.focusflow.data.db.entity.Task
import com.focusflow.data.db.entity.TaskStatus
import com.focusflow.data.repository.TaskRepository
import com.focusflow.domain.usecase.CompleteTaskUseCase
import com.focusflow.util.todayEpoch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyPlanViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    data class UiState(
        val tasks: List<Task> = emptyList(),
        val totalTasks: Int = 0,
        val completedTasks: Int = 0,
        val taskPool: List<TaskWithPlanInfo> = emptyList(),
        val showTaskPool: Boolean = false
    )

    private val _showTaskPool = MutableStateFlow(false)

    val uiState: StateFlow<UiState> = combine(
        taskRepository.getTasksForDate(todayEpoch()),
        taskRepository.getUnassignedTasksForDate(todayEpoch()),
        _showTaskPool
    ) { tasks, pool, showPool ->
        UiState(
            tasks = tasks,
            totalTasks = tasks.size,
            completedTasks = tasks.count { it.status == TaskStatus.DONE },
            taskPool = pool,
            showTaskPool = showPool
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            completeTaskUseCase(taskId)
        }
    }

    fun toggleTaskPool() {
        _showTaskPool.value = !_showTaskPool.value
    }

    fun addTaskToToday(taskId: String) {
        viewModelScope.launch {
            taskRepository.assignTaskToDay(taskId, todayEpoch())
        }
    }

    fun removeTaskFromToday(taskId: String) {
        viewModelScope.launch {
            taskRepository.removeTaskFromDate(taskId, todayEpoch())
        }
    }
}
