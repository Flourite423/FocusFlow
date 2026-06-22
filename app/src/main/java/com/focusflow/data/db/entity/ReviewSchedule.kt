package com.focusflow.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_schedules",
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
        Index(value = ["nextReviewDate"])
    ]
)
data class ReviewSchedule(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val reviewIntervals: String, // JSON array of intervals in days
    val currentRound: Int = 0,
    val nextReviewDate: Long,
    val lastReviewDate: Long? = null,
    val totalRounds: Int = 5,
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)