package com.focusflow.ui.timer

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    uiState.isRunning -> "学习中..."
                    uiState.savedMinutes > 0 -> "已学习 ${uiState.savedMinutes} 分钟"
                    else -> "准备开始学习"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(48.dp))

            // Controls
            if (!uiState.isRunning) {
                Button(
                    onClick = { viewModel.startTimer() },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PlayArrow, "开始", modifier = Modifier.size(36.dp))
                }
            } else {
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
}
