package com.focusflow.ui.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.entity.Milestone
import com.focusflow.data.db.entity.Plan
import com.focusflow.data.db.entity.Task
import com.focusflow.data.db.entity.TaskStatus
import com.focusflow.data.repository.PlanRepository
import com.focusflow.data.repository.TaskRepository
import com.focusflow.domain.model.MilestoneProgress
import com.focusflow.domain.usecase.CompleteTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planRepository: PlanRepository,
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    private val planId: String = savedStateHandle.get<String>("planId") ?: ""

    data class UiState(
        val plan: Plan? = null,
        val milestones: List<Milestone> = emptyList(),
        val progress: List<MilestoneProgress> = emptyList(),
        val tasksByMilestone: Map<String, List<Task>> = emptyMap()
    )

    private val milestonesFlow = planRepository.getMilestonesByPlanId(planId)

    private val tasksFlow = milestonesFlow.flatMapLatest { milestones ->
        if (milestones.isEmpty()) {
            MutableStateFlow(emptyMap())
        } else {
            val flows = milestones.map { ms ->
                planRepository.getTasksByMilestoneId(ms.id).map { tasks -> ms.id to tasks }
            }
            combine(flows) { pairs -> pairs.toMap() }
        }
    }

    val uiState: StateFlow<UiState> = combine(
        planRepository.getPlanById(planId),
        milestonesFlow,
        planRepository.getPlanProgress(planId),
        tasksFlow
    ) { plan, milestones, progress, tasksByMilestone ->
        UiState(plan = plan, milestones = milestones, progress = progress, tasksByMilestone = tasksByMilestone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun addMilestone(planId: String, title: String, description: String) {
        viewModelScope.launch {
            val milestone = Milestone(id = UUID.randomUUID().toString(), planId = planId, title = title, description = description)
            planRepository.upsertMilestone(milestone)
        }
    }

    fun addTask(milestoneId: String, title: String, description: String) {
        viewModelScope.launch {
            val task = Task(id = UUID.randomUUID().toString(), milestoneId = milestoneId, title = title, description = description)
            planRepository.upsertTask(task)
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
            taskRepository.updateTaskStatus(task.id, newStatus, System.currentTimeMillis())
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch { completeTaskUseCase(taskId) }
    }

    fun assignTaskToToday(taskId: String) {
        viewModelScope.launch { taskRepository.assignTaskToDay(taskId, com.focusflow.util.todayEpoch()) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { planRepository.deleteTask(taskId) }
    }
}
