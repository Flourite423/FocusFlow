package com.focusflow.ui.plan

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.focusflow.data.db.entity.Milestone
import com.focusflow.data.db.entity.Task
import com.focusflow.data.db.entity.TaskStatus
import com.focusflow.ui.components.FocusProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: String,
    navController: NavController,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddMilestone by remember { mutableStateOf(false) }
    var addTaskToMilestone by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plan?.title ?: "计划详情") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMilestone = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加里程碑")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Plan info card
            uiState.plan?.let { plan ->
                item {
                    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(plan.title, style = MaterialTheme.typography.titleLarge)
                            if (plan.description.isNotBlank()) {
                                Text(plan.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(8.dp))
                            val totalTasks = uiState.progress.sumOf { it.totalTasks }
                            val completedTasks = uiState.progress.sumOf { it.completedTasks }
                            if (totalTasks > 0) {
                                FocusProgressBar(
                                    progress = completedTasks.toFloat() / totalTasks,
                                    label = "总进度: $completedTasks/$totalTasks"
                                )
                            }
                        }
                    }
                }
            }

            // Milestones
            if (uiState.milestones.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text("还没有里程碑，点击 + 添加", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                itemsIndexed(uiState.milestones) { _, milestone ->
                    val progress = uiState.progress.find { it.milestoneId == milestone.id }
                    val tasks = uiState.tasksByMilestone[milestone.id] ?: emptyList()
                    MilestoneCard(
                        milestone = milestone,
                        totalTasks = progress?.totalTasks ?: 0,
                        completedTasks = progress?.completedTasks ?: 0,
                        tasks = tasks,
                        onAddTask = { addTaskToMilestone = milestone.id },
                        onToggleTask = { task -> viewModel.toggleTaskStatus(task) },
                        onAssignToToday = { task -> viewModel.assignTaskToToday(task.id) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                }
            }
        }
    }

    if (showAddMilestone) {
        AddMilestoneDialog(
            onDismiss = { showAddMilestone = false },
            onAdd = { title, desc ->
                viewModel.addMilestone(planId, title, desc)
                showAddMilestone = false
            }
        )
    }

    addTaskToMilestone?.let { milestoneId ->
        AddTaskDialog(
            onDismiss = { addTaskToMilestone = null },
            onAdd = { title, desc ->
                viewModel.addTask(milestoneId, title, desc)
                addTaskToMilestone = null
            }
        )
    }
}

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    totalTasks: Int,
    completedTasks: Int,
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onToggleTask: (Task) -> Unit,
    onAssignToToday: (Task) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth().clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(milestone.title, style = MaterialTheme.typography.titleSmall)
                    if (milestone.description.isNotBlank()) {
                        Text(milestone.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onAddTask) {
                    Icon(Icons.Default.Add, contentDescription = "添加任务", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(8.dp))
            if (totalTasks > 0) {
                FocusProgressBar(
                    progress = completedTasks.toFloat() / totalTasks,
                    label = "$completedTasks/$totalTasks 任务"
                )
            } else {
                Text("暂无任务，点击 + 添加", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Task list (expandable)
            AnimatedVisibility(visible = expanded && tasks.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    tasks.forEach { task ->
                        TaskRow(
                            task = task,
                            onToggle = { onToggleTask(task) },
                            onAssignToToday = { onAssignToToday(task) },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                }
            }

            // Hint when collapsed
            if (!expanded && tasks.isNotEmpty()) {
                Text(
                    "点击展开查看 ${tasks.size} 个任务",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task, onToggle: () -> Unit, onAssignToToday: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.status == TaskStatus.DONE,
            onCheckedChange = { onToggle() }
        )
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.status == TaskStatus.DONE)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            if (task.description.isNotBlank()) {
                Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onAssignToToday) {
            Icon(Icons.Default.DateRange, contentDescription = "分配到今天", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AddMilestoneDialog(onDismiss: () -> Unit, onAdd: (title: String, description: String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加里程碑") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("里程碑名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述（可选）") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onAdd(title, description) }, enabled = title.isNotBlank()) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (title: String, description: String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加任务") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("任务名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述（可选）") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onAdd(title, description) }, enabled = title.isNotBlank()) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
