package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusflow.data.db.entity.Plan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :id")
    fun getPlanById(id: String): Flow<Plan?>

    @Query("SELECT * FROM plans WHERE status = 'active' ORDER BY createdAt DESC LIMIT 1")
    fun getActivePlan(): Flow<Plan?>

    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
    suspend fun getAllSync(): List<Plan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: Plan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plans: List<Plan>)

    @Query("UPDATE plans SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Delete
    suspend fun delete(plan: Plan)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deleteById(id: String)
    @Query("DELETE FROM plans")
    suspend fun deleteAll()
}