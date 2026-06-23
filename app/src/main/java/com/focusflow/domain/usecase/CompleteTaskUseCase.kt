package com.focusflow.domain.usecase

import com.focusflow.data.db.dao.StudySessionDao
import com.focusflow.data.db.entity.TaskStatus
import com.focusflow.data.repository.ReviewRepository
import com.focusflow.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val sessionDao: StudySessionDao,
    private val reviewRepository: ReviewRepository
) {

    suspend operator fun invoke(taskId: String) {
        val now = System.currentTimeMillis()
        taskRepository.updateTaskStatus(taskId, TaskStatus.DONE, now)

        // Sum up actual study minutes from sessions
        val totalMinutes = sessionDao.getByTaskId(taskId)
            .map { sessions -> sessions.sumOf { it.durationMinutes } }
            .first()
        taskRepository.updateActualMinutes(taskId, totalMinutes)

        // Create review schedule for spaced repetition
        reviewRepository.createSchedule(taskId)
    }
}
