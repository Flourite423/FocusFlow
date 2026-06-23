package com.focusflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.data.db.entity.DayAssignment
import kotlinx.coroutines.flow.Flow

@Dao
interface DayAssignmentDao {
    @Query("SELECT * FROM day_assignments WHERE date = :date ORDER BY \"order\" ASC")
    fun getByDate(date: Long): Flow<List<DayAssignment>>

    @Query("SELECT * FROM day_assignments WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, \"order\" ASC")
    fun getByWeek(startDate: Long, endDate: Long): Flow<List<DayAssignment>>

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