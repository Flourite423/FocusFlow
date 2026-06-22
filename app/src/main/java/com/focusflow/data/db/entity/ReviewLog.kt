package com.focusflow.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_logs",
    foreignKeys = [
        ForeignKey(
            entity = ReviewSchedule::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["scheduleId"])]
)
data class ReviewLog(
    @PrimaryKey
    val id: String,
    val scheduleId: String,
    val round: Int,
    val reviewedAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)