package com.focusflow.domain.usecase

import com.focusflow.data.db.dao.StudySessionDao
import com.focusflow.data.db.entity.TaskStatus
import com.focusflow.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val sessionDao: StudySessionDao
) {

    suspend operator fun invoke(taskId: String) {
        val now = System.currentTimeMillis()
        taskRepository.updateTaskStatus(taskId, TaskStatus.DONE, now)

        val totalMinutes = sessionDao.getByTaskId(taskId)
            .map { sessions -> sessions.sumOf { it.durationMinutes } }
            .first()
        taskRepository.updateActualMinutes(taskId, totalMinutes)
    }
}
