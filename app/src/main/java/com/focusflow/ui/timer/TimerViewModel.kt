package com.focusflow.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.entity.StudySession
import com.focusflow.data.db.entity.Task
import com.focusflow.data.repository.SessionRepository
import com.focusflow.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    data class UiState(
        val isRunning: Boolean = false,
        val isPaused: Boolean = false,
        val elapsedSeconds: Int = 0,
        val currentTaskId: String? = null,
        val currentTaskTitle: String? = null,
        val currentSessionId: String? = null,
        val savedMinutes: Int = 0,
        val availableTasks: List<Task> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getRecommendedTasks().collect { tasks ->
                _uiState.update { it.copy(availableTasks = tasks) }
            }
        }
    }

    fun startTimer(taskId: String? = null, taskTitle: String? = null) {
        val sessionId = UUID.randomUUID().toString()
        _uiState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                elapsedSeconds = 0,
                currentTaskId = taskId,
                currentTaskTitle = taskTitle,
                currentSessionId = sessionId
            )
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeTimer() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun stopTimer() {
        val state = _uiState.value
        if (state.elapsedSeconds > 0 && state.currentSessionId != null) {
            viewModelScope.launch {
                val session = StudySession(
                    id = state.currentSessionId,
                    taskId = state.currentTaskId ?: "",
                    startTime = System.currentTimeMillis() - (state.elapsedSeconds * 1000L),
                    endTime = System.currentTimeMillis(),
                    durationMinutes = state.elapsedSeconds / 60
                )
                sessionRepository.createSession(session)

                // Update task actual minutes if linked to a task
                if (!state.currentTaskId.isNullOrBlank()) {
                    taskRepository.updateActualMinutes(state.currentTaskId, state.elapsedSeconds / 60)
                }

                _uiState.update {
                    it.copy(
                        savedMinutes = it.savedMinutes + state.elapsedSeconds / 60,
                        isRunning = false,
                        isPaused = false,
                        elapsedSeconds = 0,
                        currentTaskId = null,
                        currentTaskTitle = null,
                        currentSessionId = null
                    )
                }
            }
        } else {
            _uiState.update {
                UiState(savedMinutes = it.savedMinutes)
            }
        }
    }

    fun tick() {
        _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
    }
}
