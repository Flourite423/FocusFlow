package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.data.db.entity.DayAssignment
import kotlinx.coroutines.flow.Flow

data class AssignmentWithTitle(
    val id: String,
    val taskId: String,
    val date: Long,
    val order: Int,
    val isTemporary: Boolean,
    val createdAt: Long,
    val taskTitle: String
)

data class AssignmentWithFullInfo(
    val id: String,
    val taskId: String,
    val date: Long,
    val order: Int,
    val isTemporary: Boolean,
    val createdAt: Long,
    val taskTitle: String,
    val estimatedMinutes: Int,
    val planName: String,
    val milestoneTitle: String
)

@Dao
interface DayAssignmentDao {
    @Query("SELECT * FROM day_assignments WHERE date = :date ORDER BY \"order\" ASC")
    fun getByDate(date: Long): Flow<List<DayAssignment>>

    @Query("SELECT * FROM day_assignments WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, \"order\" ASC")
    fun getByWeek(startDate: Long, endDate: Long): Flow<List<DayAssignment>>

    @Query("""
        SELECT da.id, da.taskId, da.date, da.`order`, da.isTemporary, da.createdAt, t.title as taskTitle
        FROM day_assignments da
        INNER JOIN tasks t ON t.id = da.taskId
        WHERE da.date BETWEEN :weekStart AND :weekEnd
        ORDER BY da.date ASC, da.`order` ASC
    """)
    fun getAssignmentsWithTitles(weekStart: Long, weekEnd: Long): Flow<List<AssignmentWithTitle>>

    @Query("""
        SELECT da.id, da.taskId, da.date, da.`order`, da.isTemporary, da.createdAt,
               t.title AS taskTitle, t.estimatedMinutes, p.title AS planName, m.title AS milestoneTitle
        FROM day_assignments da
        INNER JOIN tasks t ON t.id = da.taskId
        INNER JOIN milestones m ON m.id = t.milestoneId
        INNER JOIN plans p ON p.id = m.planId
        WHERE da.date BETWEEN :weekStart AND :weekEnd
        ORDER BY da.date ASC, da.`order` ASC
    """)
    fun getAssignmentsWithFullInfo(weekStart: Long, weekEnd: Long): Flow<List<AssignmentWithFullInfo>>

    @Query("SELECT COUNT(*) FROM day_assignments WHERE date = :date")
    fun getCountForDate(date: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(assignment: DayAssignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(assignments: List<DayAssignment>)

    @Query("DELETE FROM day_assignments WHERE taskId = :taskId AND date = :date")
    suspend fun removeTaskFromDate(taskId: String, date: Long)

    @Delete
    suspend fun delete(assignment: DayAssignment)

    @Query("DELETE FROM day_assignments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM day_assignments ORDER BY date ASC, \"order\" ASC")
    suspend fun getAllSync(): List<DayAssignment>
    @Query("DELETE FROM day_assignments")
    suspend fun deleteAll()
}
