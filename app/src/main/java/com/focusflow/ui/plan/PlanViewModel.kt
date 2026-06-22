package com.focusflow.ui.plan

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
class PlanViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val milestoneDao: MilestoneDao
) : ViewModel() {

    data class UiState(
        val plans: List<Plan> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<UiState> = combine(
        planRepository.getAllPlans(),
        _isLoading
    ) { plans, loading ->
        UiState(plans = plans, isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    init {
        _isLoading.value = false
    }

    fun createPlan(title: String, description: String, category: String, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            val plan = Plan(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                category = category,
                startDate = startDate,
                endDate = endDate,
                status = PlanStatus.ACTIVE
            )
            planRepository.upsert(plan)
        }
    }

    fun deletePlan(plan: Plan) {
        viewModelScope.launch {
            planRepository.delete(plan)
        }
    }

    fun activatePlan(planId: String) {
        viewModelScope.launch {
            planRepository.updateStatus(planId, PlanStatus.ACTIVE)
        }
    }

    fun archivePlan(planId: String) {
        viewModelScope.launch {
            planRepository.updateStatus(planId, PlanStatus.ARCHIVED)
        }
    }
}
