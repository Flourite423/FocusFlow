package com.focusflow.domain.usecase

import com.focusflow.data.db.dao.DayAssignmentDao
import com.focusflow.data.db.entity.Task
import com.focusflow.data.repository.TaskRepository
import com.focusflow.util.toEpochMillis
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val assignmentDao: DayAssignmentDao
) {

    operator fun invoke(): Flow<List<Task>> {
        val today = LocalDate.now().toEpochMillis()
        return taskRepository.getTasksForDate(today)
    }
}
