package com.focusflow.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.entity.Task
import com.focusflow.data.db.entity.TaskStatus
import com.focusflow.data.repository.TaskRepository
import com.focusflow.domain.usecase.CompleteTaskUseCase
import com.focusflow.util.todayEpoch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
        val completedTasks: Int = 0
    )

    // Use tasks assigned to today via DayAssignment
    val uiState: StateFlow<UiState> = taskRepository.getTasksForDate(todayEpoch()).map { tasks ->
        UiState(
            tasks = tasks,
            totalTasks = tasks.size,
            completedTasks = tasks.count { it.status == TaskStatus.DONE }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            completeTaskUseCase(taskId)
        }
    }
}
