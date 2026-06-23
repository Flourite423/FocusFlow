package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.data.db.entity.Milestone
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE planId = :planId ORDER BY \"order\" ASC")
    fun getByPlanId(planId: String): Flow<List<Milestone>>

    @Query("""
        SELECT
            m.id AS milestoneId,
            m.title AS milestoneTitle,
            COUNT(t.id) AS totalTasks,
            SUM(CASE WHEN t.status = 'done' THEN 1 ELSE 0 END) AS completedTasks
        FROM milestones m
        LEFT JOIN tasks t ON t.milestoneId = m.id
        WHERE m.planId = :planId
        GROUP BY m.id, m.title
        ORDER BY m."order" ASC
    """)
    fun getPlanProgress(planId: String): Flow<List<ProgressTuple>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(milestone: Milestone)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(milestones: List<Milestone>)

    @Query("SELECT * FROM milestones WHERE planId = :planId ORDER BY \"order\" ASC")
    suspend fun getAllSync(planId: String): List<Milestone>

    @Delete
    suspend fun delete(milestone: Milestone)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteById(id: String)

    data class ProgressTuple(
        val milestoneId: String,
        val milestoneTitle: String,
        val totalTasks: Int,
        val completedTasks: Int
    )
    @Query("DELETE FROM milestones")
    suspend fun deleteAll()
}