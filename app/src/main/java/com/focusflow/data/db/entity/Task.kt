package com.focusflow.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Milestone::class,
            parentColumns = ["id"],
            childColumns = ["milestoneId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["dependsOn"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["milestoneId"]),
        Index(value = ["dependsOn"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"])
    ]
)
data class Task(
    @PrimaryKey
    val id: String,
    val milestoneId: String,
    val title: String,
    val description: String = "",
    val estimatedMinutes: Int = 30,
    val actualMinutes: Int = 0,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    val dependsOn: String? = null,
    val dueDate: Long? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)