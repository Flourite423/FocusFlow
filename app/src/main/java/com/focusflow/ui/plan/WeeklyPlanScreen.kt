package com.focusflow.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.focusflow.ui.theme.FocusFlowColors
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlanScreen(
    navController: NavController,
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todayDayOfWeek = remember(LocalDate.now()) { LocalDate.now().dayOfWeek.value }
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("周计划") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Week summary
            item {
                Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("本周概览", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("已分配 ${uiState.totalAssigned} 个任务 · 点击某天添加或移除任务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Day cards
            items(days.size) { index ->
                val dayTasks = uiState.dayTasks[index] ?: emptyList()
                val isToday = index == todayDayOfWeek - 1
                val isSelected = uiState.selectedDayIndex == index

                DayCard(
                    dayName = days[index],
                    tasks = dayTasks,
                    isToday = isToday,
                    isSelected = isSelected,
                    onToggleSelect = { viewModel.selectDay(index) },
                    onRemoveTask = { taskId -> viewModel.removeTaskFromDay(taskId, index) }
                )
            }

            // Task pool picker (shown when a day is selected)
            if (uiState.selectedDayIndex != null && uiState.taskPool.isNotEmpty()) {
                item {
                    TaskPoolCard(
                        tasks = uiState.taskPool,
                        onAssign = { taskId -> viewModel.assignTaskToDay(taskId, uiState.selectedDayIndex!!) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    dayName: String,
    tasks: List<WeeklyPlanViewModel.DayTaskInfo>,
    isToday: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onRemoveTask: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleSelect() },
        elevation = CardDefaults.cardElevation(if (isToday || isSelected) 3.dp else 1.dp),
        colors = when {
            isSelected -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            isToday -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            else -> CardDefaults.cardColors()
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp),
                    tint = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else FocusFlowColors.planColor)
                Spacer(Modifier.width(8.dp))
                Text(dayName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.weight(1f))
                Text("${tasks.size} 个任务", style = MaterialTheme.typography.bodyMedium,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                SmallFloatingActionButton(
                    onClick = onToggleSelect,
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(Icons.Default.Add, "添加任务", modifier = Modifier.size(16.dp))
                }
            }

            // Task list
            if (tasks.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                tasks.forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp),
                            tint = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(task.taskTitle, style = MaterialTheme.typography.bodySmall,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${task.planName} · ${task.milestoneTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onRemoveTask(task.taskId) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, "移除", modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(4.dp))
                Text("暂无任务", style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TaskPoolCard(
    tasks: List<com.focusflow.data.db.dao.TaskWithPlanInfo>,
    onAssign: (String) -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("可分配的任务", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.height(4.dp))
            Text("点击将任务分配到所选日期", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))

            // Group by plan
            val grouped = tasks.groupBy { it.planName }
            grouped.forEach { (planName, planTasks) ->
                Text(planName, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(top = 4.dp))
                planTasks.forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onAssign(task.id) }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(task.milestoneTitle, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
                        }
                        Text("${task.estimatedMinutes}min", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
