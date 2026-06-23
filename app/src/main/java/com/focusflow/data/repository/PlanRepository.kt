package com.focusflow.data.repository

import com.focusflow.data.db.dao.MilestoneDao
import com.focusflow.data.db.dao.PlanDao
import com.focusflow.data.db.dao.TaskDao
import com.focusflow.data.db.entity.Milestone
import com.focusflow.data.db.entity.Plan
import com.focusflow.data.db.entity.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class PlanRepository @Inject constructor(
    private val planDao: PlanDao,
    private val milestoneDao: MilestoneDao,
    private val taskDao: TaskDao
) {

    // Plan operations
    fun getAllPlans(): Flow<List<Plan>> = planDao.getAllPlans()

    fun getPlanById(id: String): Flow<Plan?> = planDao.getPlanById(id)

    fun getActivePlan(): Flow<Plan?> = planDao.getActivePlan()

    suspend fun upsert(plan: Plan) = planDao.upsert(plan)

    suspend fun updateStatus(id: String, status: String) = planDao.updateStatus(id, status, System.currentTimeMillis())

    suspend fun delete(plan: Plan) = planDao.delete(plan)

    // Milestone operations
    fun getMilestonesByPlanId(planId: String): Flow<List<Milestone>> = milestoneDao.getByPlanId(planId)

    fun getPlanProgress(planId: String): Flow<List<MilestoneDao.ProgressTuple>> = milestoneDao.getPlanProgress(planId)

    suspend fun upsertMilestone(milestone: Milestone) = milestoneDao.upsert(milestone)

    // Task operations (for PlanDetail)
    fun getTasksByMilestoneId(milestoneId: String): Flow<List<Task>> = taskDao.getByMilestoneId(milestoneId)

    suspend fun upsertTask(task: Task) = taskDao.upsert(task)

    suspend fun deleteTask(taskId: String) = taskDao.deleteById(taskId)
}
