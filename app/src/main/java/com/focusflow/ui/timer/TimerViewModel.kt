package com.focusflow.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.data.db.entity.StudySession
import com.focusflow.data.db.entity.Task
import com.focusflow.data.repository.SessionRepository
import com.focusflow.data.repository.StatsRepository
import com.focusflow.util.todayEpoch
import com.focusflow.data.repository.TaskRepository
import com.focusflow.domain.usecase.CompleteTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class PomodoroPhase(val label: String, val durationMinutes: Int) {
    WORK("专注", 25),
    SHORT_BREAK("短休息", 5),
    LONG_BREAK("长休息", 15);

    val durationSeconds: Int get() = durationMinutes * 60
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val statsRepository: StatsRepository
) : ViewModel() {

    data class UiState(
        val phase: PomodoroPhase = PomodoroPhase.WORK,
        val isRunning: Boolean = false,
        val isPaused: Boolean = false,
        val remainingSeconds: Int = PomodoroPhase.WORK.durationSeconds,
        val totalSeconds: Int = PomodoroPhase.WORK.durationSeconds,
        val completedPomodoros: Int = 0,
        val currentTaskId: String? = null,
        val currentTaskTitle: String? = null,
        val currentSessionId: String? = null,
        val savedMinutes: Int = 0,
        val availableTasks: List<Task> = emptyList(),
        val showCompleteDialog: Boolean = false,
        val lastStoppedTaskId: String? = null,
        val lastStoppedMinutes: Int = 0
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
        val phase = _uiState.value.phase
        _uiState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                remainingSeconds = phase.durationSeconds,
                totalSeconds = phase.durationSeconds,
                currentTaskId = taskId,
                currentTaskTitle = taskTitle,
                currentSessionId = if (phase == PomodoroPhase.WORK) UUID.randomUUID().toString() else it.currentSessionId
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
        if (!state.isRunning) return

        // If in WORK phase, persist the session
        if (state.phase == PomodoroPhase.WORK) {
            val elapsedSeconds = state.totalSeconds - state.remainingSeconds
            if (elapsedSeconds > 0) {
                viewModelScope.launch {
                    val minutes = elapsedSeconds / 60
                    if (!state.currentTaskId.isNullOrBlank() && state.currentSessionId != null) {
                        val session = StudySession(
                            id = state.currentSessionId,
                            taskId = state.currentTaskId,
                            startTime = System.currentTimeMillis() - (elapsedSeconds * 1000L),
                            endTime = System.currentTimeMillis(),
                            durationMinutes = minutes
                        )
                        sessionRepository.createSession(session)
                        taskRepository.updateActualMinutes(state.currentTaskId, minutes)
                    }
                    if (minutes > 0) {
                        statsRepository.addStudyMinutes(todayEpoch(), minutes)
                    }
                }
            }
        }

        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                remainingSeconds = it.phase.durationSeconds,
                totalSeconds = it.phase.durationSeconds,
                currentTaskId = null,
                currentTaskTitle = null,
                currentSessionId = null,
                savedMinutes = it.savedMinutes + (state.totalSeconds - state.remainingSeconds) / 60
            )
        }
    }

    fun tick() {
        val state = _uiState.value
        if (!state.isRunning || state.isPaused) return

        if (state.remainingSeconds > 0) {
            _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
        } else {
            // Phase completed — auto-transition
            onPhaseComplete()
        }
    }

    private fun onPhaseComplete() {
        val state = _uiState.value

        if (state.phase == PomodoroPhase.WORK) {
            // Persist work session
            val minutes = state.totalSeconds / 60
            if (!state.currentTaskId.isNullOrBlank() && state.currentSessionId != null) {
                viewModelScope.launch {
                    val session = StudySession(
                        id = state.currentSessionId,
                        taskId = state.currentTaskId,
                        startTime = System.currentTimeMillis() - (state.totalSeconds * 1000L),
                        endTime = System.currentTimeMillis(),
                        durationMinutes = minutes
                    )
                    sessionRepository.createSession(session)
                    taskRepository.updateActualMinutes(state.currentTaskId, minutes)
                    statsRepository.addStudyMinutes(todayEpoch(), minutes)
                }
            }

            val newCompleted = state.completedPomodoros + 1
            val nextPhase = if (newCompleted % 4 == 0) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK

            _uiState.update {
                it.copy(
                    phase = nextPhase,
                    isRunning = false,
                    isPaused = false,
                    remainingSeconds = nextPhase.durationSeconds,
                    totalSeconds = nextPhase.durationSeconds,
                    completedPomodoros = newCompleted,
                    savedMinutes = it.savedMinutes + minutes,
                    currentSessionId = null
                )
            }
        } else {
            // Break completed — back to WORK
            _uiState.update {
                it.copy(
                    phase = PomodoroPhase.WORK,
                    isRunning = false,
                    isPaused = false,
                    remainingSeconds = PomodoroPhase.WORK.durationSeconds,
                    totalSeconds = PomodoroPhase.WORK.durationSeconds
                )
            }
        }
    }

    fun skipToNext() {
        val state = _uiState.value
        val nextPhase = when (state.phase) {
            PomodoroPhase.WORK -> {
                val newCompleted = state.completedPomodoros + 1
                if (newCompleted % 4 == 0) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
            }
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> PomodoroPhase.WORK
        }
        _uiState.update {
            it.copy(
                phase = nextPhase,
                isRunning = false,
                isPaused = false,
                remainingSeconds = nextPhase.durationSeconds,
                totalSeconds = nextPhase.durationSeconds,
                completedPomodoros = if (state.phase == PomodoroPhase.WORK) state.completedPomodoros + 1 else state.completedPomodoros
            )
        }
    }

    fun completeTask() {
        val taskId = _uiState.value.lastStoppedTaskId ?: return
        viewModelScope.launch {
            completeTaskUseCase(taskId)
            dismissCompleteDialog()
        }
    }

    fun dismissCompleteDialog() {
        _uiState.update {
            it.copy(showCompleteDialog = false, lastStoppedTaskId = null, lastStoppedMinutes = 0)
        }
    }
}
