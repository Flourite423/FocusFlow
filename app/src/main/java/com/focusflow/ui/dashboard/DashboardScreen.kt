package com.focusflow.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.focusflow.data.db.entity.Task
import com.focusflow.navigation.Screen
import com.focusflow.ui.components.OnboardingGuide
import com.focusflow.ui.theme.FocusFlowColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showOnboarding by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val greeting = remember {
        val hour = LocalTime.now().hour
        when {
            hour < 6 -> "夜深了"
            hour < 12 -> "早上好"
            hour < 14 -> "中午好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FocusFlow") },
                actions = {
                    SmallFloatingActionButton(
                        onClick = { showOnboarding = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "帮助", modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting
            item {
                Text(greeting, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("今天也要加油学习哦", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Stats row
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), { Icon(Icons.Default.Favorite, null, tint = FocusFlowColors.streakColor) }, "${uiState.streakDays}", "连续天数")
                    StatCard(Modifier.weight(1f), { Icon(Icons.Default.DateRange, null, tint = FocusFlowColors.planColor) }, "${uiState.todayMinutes}min", "今日学习")
                    StatCard(Modifier.weight(1f), { Icon(Icons.Default.CheckCircle, null, tint = FocusFlowColors.timerColor) }, "${uiState.completedTasks}/${uiState.totalTasks}", "今日任务")
                }
            }

            // Quick actions
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(Modifier.weight(1f), { Icon(Icons.Default.Star, null, tint = FocusFlowColors.streakColor) }, "今日计划", "查看今天的任务") { navController.navigate(Screen.DailyPlan.createRoute()) }
                    QuickActionCard(Modifier.weight(1f), { Icon(Icons.Default.DateRange, null, tint = FocusFlowColors.reviewColor) }, "周计划", "查看本周安排") { navController.navigate(Screen.WeeklyPlan.createRoute()) }
                }
            }
            // Daily review quick action
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(Modifier.weight(1f), { Icon(Icons.Default.CheckCircle, null, tint = FocusFlowColors.timerColor) }, "每日回顾", "查看昨日学习统计") { navController.navigate(Screen.DailyReview.createRoute()) }
                }
            }

            // Today's tasks
            item { Text("今日任务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            if (uiState.todayTasks.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("暂无任务，去计划页面创建", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(uiState.todayTasks.size) { index -> TaskRow(uiState.todayTasks[index]) }
            }

            // Heatmap
            item { Text("学习热力图", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item { HeatmapGrid(uiState.heatmapData, isDark) }
        }
    }

    if (showOnboarding) { OnboardingGuide(onDismiss = { showOnboarding = false }) }
}

@Composable
private fun StatCard(modifier: Modifier, icon: @Composable () -> Unit, value: String, label: String) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            icon(); Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionCard(modifier: Modifier, icon: @Composable () -> Unit, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            icon(); Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val isDone = task.status.value == "done"
            Box(Modifier.size(12.dp).clip(CircleShape).background(if (isDone) FocusFlowColors.timerColor else MaterialTheme.colorScheme.outline))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge, color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                Text("预估 ${task.estimatedMinutes}min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeatmapGrid(data: Map<Long, Int>, isDark: Boolean) {
    val today = LocalDate.now()
    val startDate = today.minusWeeks(16)
    val zone = ZoneId.systemDefault()

    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("最近 16 周", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 0 until 16 * 7) {
                    val date = startDate.plusDays(i.toLong())
                    val epoch = date.atStartOfDay(zone).toInstant().toEpochMilli()
                    val minutes = data[epoch] ?: 0
                    val color = if (isDark) {
                        when {
                            minutes == 0 -> FocusFlowColors.heatmapEmptyDark
                            minutes < 15 -> FocusFlowColors.heatmapLowDark
                            minutes < 30 -> FocusFlowColors.heatmapMediumDark
                            minutes < 60 -> FocusFlowColors.heatmapHighDark
                            else -> FocusFlowColors.heatmapMaxDark
                        }
                    } else {
                        when {
                            minutes == 0 -> FocusFlowColors.heatmapEmpty
                            minutes < 15 -> FocusFlowColors.heatmapLow
                            minutes < 30 -> FocusFlowColors.heatmapMedium
                            minutes < 60 -> FocusFlowColors.heatmapHigh
                            else -> FocusFlowColors.heatmapMax
                        }
                    }
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color))
                }
            }
        }
    }
}

// Extension to check if color is dark
private fun Color.luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue)
