package com.focusflow.data.repository

import com.focusflow.data.db.dao.DayAssignmentDao
import com.focusflow.data.db.dao.TaskDao
import com.focusflow.data.db.entity.DayAssignment
import com.focusflow.data.db.entity.Task
import com.focusflow.data.db.entity.TaskStatus
import com.focusflow.util.toEpochMillis
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val assignmentDao: DayAssignmentDao
) {

    fun getTasksForDate(date: Long): Flow<List<Task>> = taskDao.getTasksForDate(date)

    fun getRecommendedTasks(): Flow<List<Task>> = taskDao.getTodayRecommended(LocalDate.now().toEpochMillis())

    suspend fun getTaskById(id: String): Task? = taskDao.getById(id)

    suspend fun updateTaskStatus(id: String, status: TaskStatus, completedAt: Long? = null) {
        val now = System.currentTimeMillis()
        taskDao.updateStatus(id, status.value, now)
        if (status == TaskStatus.DONE && completedAt != null) {
            taskDao.updateCompletedAt(id, completedAt, now)
        } else if (status == TaskStatus.TODO) {
            // Clear completedAt when un-completing
            taskDao.updateCompletedAt(id, 0L, now)
        }
    }

    suspend fun updateActualMinutes(id: String, minutes: Int) =
        taskDao.updateActualMinutes(id, minutes, System.currentTimeMillis())

    suspend fun assignTaskToDay(taskId: String, date: Long) {
        assignmentDao.upsert(DayAssignment(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            date = date
        ))
    }

    suspend fun delete(task: Task) = taskDao.delete(task)
}
