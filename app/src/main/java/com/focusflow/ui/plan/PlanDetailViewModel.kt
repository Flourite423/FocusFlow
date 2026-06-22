package com.focusflow.ui.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.dao.MilestoneDao
import com.focusflow.data.db.entity.Milestone
import com.focusflow.data.db.entity.Plan
import com.focusflow.data.db.entity.PlanStatus
import com.focusflow.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planRepository: PlanRepository,
    private val milestoneDao: MilestoneDao
) : ViewModel() {

    private val planId: String = savedStateHandle.get<String>("planId") ?: ""

    data class UiState(
        val plan: Plan? = null,
        val milestones: List<Milestone> = emptyList(),
        val progress: List<MilestoneDao.ProgressTuple> = emptyList()
    )

    val uiState: StateFlow<UiState> = combine(
        planRepository.getPlanById(planId),
        milestoneDao.getByPlanId(planId),
        milestoneDao.getPlanProgress(planId)
    ) { plan, milestones, progress ->
        UiState(plan = plan, milestones = milestones, progress = progress)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun addMilestone(planId: String, title: String, description: String) {
        viewModelScope.launch {
            val milestone = Milestone(
                id = UUID.randomUUID().toString(),
                planId = planId,
                title = title,
                description = description
            )
            milestoneDao.upsert(milestone)
        }
    }
}
