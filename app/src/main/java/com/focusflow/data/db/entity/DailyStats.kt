package com.focusflow.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStats(
    @PrimaryKey
    val date: Long,
    val totalMinutes: Int = 0,
    val tasksCompleted: Int = 0,
    val reviewsDone: Int = 0,
    val streakDays: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)