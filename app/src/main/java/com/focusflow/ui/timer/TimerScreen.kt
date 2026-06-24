package com.focusflow.ui.timer

import android.media.AudioManager
import android.media.ToneGenerator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.focusflow.data.db.entity.Task
import com.focusflow.ui.theme.Mint400
import com.focusflow.ui.theme.Mint500
import com.focusflow.ui.theme.Violet400
import com.focusflow.ui.theme.Violet500
import kotlinx.coroutines.delay

private fun playPhaseCompleteSound() {
    try {
        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        tg.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        tg.release()
    } catch (_: Exception) { }
}

@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Tick every second
    LaunchedEffect(uiState.isRunning, uiState.isPaused) {
        while (uiState.isRunning && !uiState.isPaused) {
            delay(1000)
            viewModel.tick()
        }
    }

    // Play sound when phase countdown hits zero
    var prevRemaining by remember { mutableStateOf(uiState.remainingSeconds) }
    LaunchedEffect(uiState.remainingSeconds, uiState.phase) {
        if (prevRemaining == 1 && uiState.remainingSeconds == 0) {
            playPhaseCompleteSound()
        }
        prevRemaining = uiState.remainingSeconds
    }

    val phaseColor = when (uiState.phase) {
        PomodoroPhase.WORK -> MaterialTheme.colorScheme.primary
        PomodoroPhase.SHORT_BREAK -> Mint500
        PomodoroPhase.LONG_BREAK -> Violet500
    }

    val phaseColorLight = when (uiState.phase) {
        PomodoroPhase.WORK -> MaterialTheme.colorScheme.primaryContainer
        PomodoroPhase.SHORT_BREAK -> Mint400.copy(alpha = 0.2f)
        PomodoroPhase.LONG_BREAK -> Violet400.copy(alpha = 0.2f)
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)
        ) {
            // Phase label
            item {
                Spacer(Modifier.height(16.dp))
                PhaseChip(uiState.phase, phaseColor, phaseColorLight)
            }

            // Circular timer
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                    CircularProgressIndicator(
                        progress = if (uiState.totalSeconds > 0) uiState.remainingSeconds.toFloat() / uiState.totalSeconds else 0f,
                        color = phaseColor,
                        modifier = Modifier.size(240.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minutes = uiState.remainingSeconds / 60
                        val seconds = uiState.remainingSeconds % 60
                        Text(
                            String.format("%02d:%02d", minutes, seconds),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Light,
                            color = phaseColor
                        )
                        if (uiState.currentTaskTitle != null) {
                            Text(
                                uiState.currentTaskTitle!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Pomodoro dots
            item {
                PomodoroDots(completedPomodoros = uiState.completedPomodoros, isWorkPhase = uiState.phase == PomodoroPhase.WORK)
            }

            // Controls
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Skip button
                    OutlinedButton(
                        onClick = { viewModel.skipToNext() },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        Text("⏭", fontSize = 18.sp)
                    }

                    // Play/Pause/Stop
                    if (!uiState.isRunning) {
                        Button(
                            onClick = { viewModel.startTimer(uiState.currentTaskId, uiState.currentTaskTitle) },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = phaseColor)
                        ) {
                            Icon(Icons.Default.PlayArrow, "开始", modifier = Modifier.size(32.dp))
                        }
                    } else {
                        // Stop
                        OutlinedButton(
                            onClick = { viewModel.stopTimer() },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape
                        ) {
                            Text("■", fontSize = 20.sp, color = MaterialTheme.colorScheme.error)
                        }
                        // Pause/Resume
                        FilledTonalButton(
                            onClick = { if (uiState.isPaused) viewModel.resumeTimer() else viewModel.pauseTimer() },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Refresh,
                                if (uiState.isPaused) "继续" else "暂停",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Task selection (only when not running and in WORK phase)
            if (!uiState.isRunning && uiState.phase == PomodoroPhase.WORK) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("选择任务（可选）", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (uiState.availableTasks.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("暂无可用任务", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    itemsIndexed(uiState.availableTasks) { _, task ->
                        val isSelected = uiState.currentTaskId == task.id
                        TaskSelectionCard(task = task, isSelected = isSelected, phaseColor = phaseColor) {
                            viewModel.startTimer(task.id, task.title)
                        }
                    }
                }
            }

            // Today's total
            if (uiState.savedMinutes > 0) {
                item {
                    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("今日累计", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${uiState.savedMinutes} 分钟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Complete task dialog
    if (uiState.showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompleteDialog() },
            title = { Text("完成任务？") },
            text = { Text("是否将此任务标记为已完成？") },
            confirmButton = { TextButton(onClick = { viewModel.completeTask() }) { Text("标记完成") } },
            dismissButton = { TextButton(onClick = { viewModel.dismissCompleteDialog() }) { Text("暂不") } }
        )
    }
}

@Composable
private fun PhaseChip(phase: PomodoroPhase, color: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            phase.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun CircularProgressIndicator(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val padding = strokeWidth / 2
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(padding, padding)

        // Background track
        drawArc(
            color = color.copy(alpha = 0.15f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun PomodoroDots(completedPomodoros: Int, isWorkPhase: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { index ->
            val isCompleted = index < (completedPomodoros % 4)
            val isCurrent = isWorkPhase && index == (completedPomodoros % 4)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    )
            )
        }
        if (completedPomodoros >= 4) {
            Text(
                "×${completedPomodoros / 4}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskSelectionCard(task: Task, isSelected: Boolean, phaseColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) phaseColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = phaseColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyMedium)
                Text("预估 ${task.estimatedMinutes}min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
