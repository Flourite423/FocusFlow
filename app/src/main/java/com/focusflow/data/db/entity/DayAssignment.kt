package com.focusflow.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_assignments",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["date"])
    ]
)
data class DayAssignment(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val date: Long,
    val order: Int = 0,
    val isTemporary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)