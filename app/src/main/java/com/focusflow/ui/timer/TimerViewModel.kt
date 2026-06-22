package com.focusflow.ui.timer

import androidx.lifecycle.ViewModel
import com.focusflow.data.repository.SessionRepository
import com.focusflow.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    data class UiState(
        val currentTask: com.focusflow.data.db.entity.Task? = null,
        val elapsedSeconds: Long = 0,
        val isRunning: Boolean = false,
        val sessionType: String = "focus"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        // TODO: Load timer data
    }
}