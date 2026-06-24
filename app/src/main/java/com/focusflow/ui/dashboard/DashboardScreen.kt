package com.focusflow.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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

            // Stats row — primary metric (streak) is larger
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryStatCard(Modifier.weight(1.3f), uiState.streakDays)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStatCard(FocusFlowColors.planColor, "${uiState.todayMinutes}min", "今日学习")
                        MiniStatCard(FocusFlowColors.timerColor, "${uiState.completedTasks}/${uiState.totalTasks}", "今日任务")
                    }
                }
            }

            // Quick actions — 3-column grid
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionChip(Modifier.weight(1f), Icons.Default.Star, "今日计划", FocusFlowColors.planColor) { navController.navigate(Screen.DailyPlan.createRoute()) }
                    QuickActionChip(Modifier.weight(1f), Icons.Default.DateRange, "周计划", FocusFlowColors.reviewColor) { navController.navigate(Screen.WeeklyPlan.createRoute()) }
                    QuickActionChip(Modifier.weight(1f), Icons.Default.CheckCircle, "回顾", FocusFlowColors.timerColor) { navController.navigate(Screen.DailyReview.createRoute()) }
                }
            }

            // Today's tasks
            item { Text("今日任务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            if (uiState.todayTasks.isEmpty() && uiState.todayAssignments.isEmpty()) {
                item {
                    EmptyTaskCard(onCreatePlan = { navController.navigate(Screen.PlanList.createRoute()) })
                }
            } else {
                items(uiState.todayAssignments.size) { index ->
                    val assignment = uiState.todayAssignments[index]
                    val isDone = uiState.todayTasks.any { it.id == assignment.taskId && it.status.value == "done" }
                    AnimatedTaskRow(
                        taskTitle = assignment.taskTitle,
                        planName = assignment.planName,
                        estimatedMinutes = assignment.estimatedMinutes,
                        isDone = isDone
                    )
                }
            }

            // Heatmap with legend
            item { Text("学习热力图", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item { HeatmapGrid(uiState.heatmapData, isDark) }
        }
    }

    if (showOnboarding) { OnboardingGuide(onDismiss = { showOnboarding = false }) }
}

// === Primary stat card (larger, prominent) ===

@Composable
private fun PrimaryStatCard(modifier: Modifier, streakDays: Int) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Favorite, null, tint = FocusFlowColors.streakColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text("$streakDays", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("连续天数", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

// === Mini stat cards (secondary metrics) ===

@Composable
private fun MiniStatCard(accentColor: Color, value: String, label: String) {
    Card(elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accentColor))
            Spacer(Modifier.width(8.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// === Quick action chip (compact) ===

@Composable
private fun QuickActionChip(modifier: Modifier, icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            Modifier.fillMaxWidth().height(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// === Empty state card ===

@Composable
private fun EmptyTaskCard(onCreatePlan: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onCreatePlan),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, "创建计划", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
            Text("还没有今天的任务", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text("点击创建你的第一个学习计划", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// === Animated task row with strikethrough ===

@Composable
private fun AnimatedTaskRow(taskTitle: String, planName: String?, estimatedMinutes: Int, isDone: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (isDone) FocusFlowColors.timerColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(400),
        label = "taskBg"
    )
    val dotScale by animateFloatAsState(
        targetValue = if (isDone) 1.3f else 1f,
        animationSpec = tween(300),
        label = "dotScale"
    )

    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(if (isDone) 0.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(12.dp).scale(dotScale).clip(CircleShape)
                    .background(if (isDone) FocusFlowColors.timerColor else MaterialTheme.colorScheme.outline)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    taskTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Row {
                    if (planName != null) {
                        Text(planName, style = MaterialTheme.typography.labelSmall, color = FocusFlowColors.planColor.copy(alpha = 0.8f))
                        Text(" · ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${estimatedMinutes}min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            AnimatedVisibility(visible = isDone, enter = fadeIn(), exit = fadeOut()) {
                Icon(Icons.Default.CheckCircle, "已完成", tint = FocusFlowColors.timerColor, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// === Heatmap with legend ===

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

            // Color legend
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text("少", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                val legendColors = if (isDark) listOf(
                    FocusFlowColors.heatmapEmptyDark, FocusFlowColors.heatmapLowDark,
                    FocusFlowColors.heatmapMediumDark, FocusFlowColors.heatmapHighDark, FocusFlowColors.heatmapMaxDark
                ) else listOf(
                    FocusFlowColors.heatmapEmpty, FocusFlowColors.heatmapLow,
                    FocusFlowColors.heatmapMedium, FocusFlowColors.heatmapHigh, FocusFlowColors.heatmapMax
                )
                legendColors.forEach { c ->
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(c))
                    Spacer(Modifier.width(3.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text("多", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Extension to check if color is dark
private fun Color.luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue)
