package com.focusflow.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
                items(uiState.milestones, key = { it.id }) { milestone ->
                    val progress = uiState.progress.find { it.milestoneId == milestone.id }
                    MilestoneCard(
                        milestone = milestone,
                        totalTasks = progress?.totalTasks ?: 0,
                        completedTasks = progress?.completedTasks ?: 0
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
}

@Composable
private fun MilestoneCard(milestone: Milestone, totalTasks: Int, completedTasks: Int) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(milestone.title, style = MaterialTheme.typography.titleSmall)
            if (milestone.description.isNotBlank()) {
                Text(milestone.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            if (totalTasks > 0) {
                FocusProgressBar(
                    progress = completedTasks.toFloat() / totalTasks,
                    label = "$completedTasks/$totalTasks 任务"
                )
            } else {
                Text("暂无任务", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
