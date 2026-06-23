package com.focusflow.domain.model

data class MilestoneProgress(
    val milestoneId: String,
    val milestoneTitle: String,
    val totalTasks: Int,
    val completedTasks: Int
)
