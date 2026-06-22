package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.data.db.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE milestoneId = :milestoneId ORDER BY createdAt ASC")
    fun getByMilestoneId(milestoneId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): Task?

    @Query("SELECT * FROM tasks ORDER BY createdAt ASC")
    suspend fun getAllSync(): List<Task>

    @Query("""
        SELECT t.* FROM tasks t
        INNER JOIN day_assignments da ON da.taskId = t.id
        WHERE da.date = :date
        ORDER BY da.`order` ASC, t.priority DESC
    """)
    fun getTasksForDate(date: Long): Flow<List<Task>>

    @Query("""
        SELECT t.* FROM tasks t
        INNER JOIN milestones m ON m.id = t.milestoneId
        INNER JOIN plans p ON p.id = m.planId
        WHERE p.status = 'active'
          AND t.status IN ('todo', 'in_progress')
          AND (t.dueDate IS NULL OR t.dueDate >= :today)
        ORDER BY
            CASE t.priority
                WHEN 'urgent' THEN 0
                WHEN 'high' THEN 1
                WHEN 'medium' THEN 2
                WHEN 'low' THEN 3
            END,
            CASE WHEN t.dueDate IS NULL THEN 1 ELSE 0 END,
            t.dueDate ASC,
            t.createdAt ASC
        LIMIT 10
    """)
    fun getTodayRecommended(today: Long): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE milestoneId = :milestoneId AND status = 'done'")
    fun getCompletedCount(milestoneId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE milestoneId = :milestoneId")
    fun getTotalCount(milestoneId: String): Flow<Int>

    @Query("SELECT status FROM tasks WHERE id = :id")
    fun getTaskStatus(id: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tasks: List<Task>)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE tasks SET actualMinutes = actualMinutes + :minutes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateActualMinutes(id: String, minutes: Int, updatedAt: Long)

    @Query("UPDATE tasks SET completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCompletedAt(id: String, completedAt: Long, updatedAt: Long)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
}
