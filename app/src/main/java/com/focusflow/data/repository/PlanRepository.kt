package com.focusflow.data.repository

import com.focusflow.data.db.dao.PlanDao
import com.focusflow.data.db.entity.Plan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlanRepository @Inject constructor(
    private val planDao: PlanDao
) {

    fun getAllPlans(): Flow<List<Plan>> = planDao.getAllPlans()

    fun getPlanById(id: String): Flow<Plan?> = planDao.getPlanById(id)

    fun getActivePlan(): Flow<Plan?> = planDao.getActivePlan()

    suspend fun upsert(plan: Plan) = planDao.upsert(plan)

    suspend fun updateStatus(id: String, status: String) = planDao.updateStatus(id, status, System.currentTimeMillis())

    suspend fun delete(plan: Plan) = planDao.delete(plan)
}