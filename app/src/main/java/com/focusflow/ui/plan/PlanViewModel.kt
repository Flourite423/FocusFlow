package com.focusflow.ui.plan

import androidx.lifecycle.ViewModel
import com.focusflow.data.repository.PlanRepository
import com.focusflow.data.repository.TaskRepository
import com.focusflow.data.db.MilestoneDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val taskRepository: TaskRepository,
    private val milestoneDao: MilestoneDao
) : ViewModel() {

    data class UiState(
        val plans: List<com.focusflow.data.db.entity.Plan> = emptyList(),
        val selectedPlan: com.focusflow.data.db.entity.Plan? = null,
        val milestones: List<com.focusflow.data.db.entity.Milestone> = emptyList(),
        val tasks: List<com.focusflow.data.db.entity.Task> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // TODO: Load plan data
    }
}