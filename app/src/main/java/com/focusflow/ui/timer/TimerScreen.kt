package com.focusflow.ui.timer

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.focusflow.data.db.entity.Task
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    // Tick every second when running and not paused
    LaunchedEffect(uiState.isRunning, uiState.isPaused) {
        while (uiState.isRunning && !uiState.isPaused) {
            delay(1000)
            viewModel.tick()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer display
            val hours = uiState.elapsedSeconds / 3600
            val minutes = (uiState.elapsedSeconds % 3600) / 60
            val seconds = uiState.elapsedSeconds % 60
            val timeStr = if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = timeStr,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = if (uiState.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = when {
                    uiState.isRunning && uiState.isPaused -> "已暂停"
                    uiState.isRunning -> {
                        val taskInfo = uiState.currentTaskTitle ?: "学习中"
                        "$taskInfo..."
                    }
                    uiState.savedMinutes > 0 -> "已学习 ${uiState.savedMinutes} 分钟"
                    else -> "准备开始学习"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(48.dp))

            // Task selector (only when not running)
            if (!uiState.isRunning) {
                Text(
                    "选择任务（可选）",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                if (uiState.availableTasks.isEmpty()) {
                    Card(Modifier.fillMaxWidth()) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("暂无可用任务", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(uiState.availableTasks) { _, task ->
                            val isSelected = selectedTask?.id == task.id
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedTask = if (isSelected) null else task
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(task.title, style = MaterialTheme.typography.bodyMedium)
                                        Text("预估 ${task.estimatedMinutes}min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Start button
                Button(
                    onClick = { viewModel.startTimer(selectedTask?.id, selectedTask?.title) },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PlayArrow, "开始", modifier = Modifier.size(36.dp))
                }
            } else {
                // Controls when running
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop button
                    OutlinedButton(
                        onClick = { viewModel.stopTimer() },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Refresh, "结束", modifier = Modifier.size(24.dp))
                    }

                    // Pause/Resume button
                    FilledTonalButton(
                        onClick = {
                            if (uiState.isPaused) viewModel.resumeTimer()
                            else viewModel.pauseTimer()
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Refresh,
                            if (uiState.isPaused) "继续" else "暂停",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Today summary
            if (uiState.savedMinutes > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("今日累计", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.savedMinutes} 分钟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Complete task dialog after stopping timer
    if (uiState.showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompleteDialog() },
            title = { Text("完成任务？") },
            text = { Text("你刚刚学习了 ${uiState.lastStoppedMinutes} 分钟。是否将此任务标记为已完成？") },
            confirmButton = {
                TextButton(onClick = { viewModel.completeTask() }) {
                    Text("标记完成")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCompleteDialog() }) {
                    Text("暂不")
                }
            }
        )
    }
}
