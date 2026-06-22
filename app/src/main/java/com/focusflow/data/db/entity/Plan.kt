package com.focusflow.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class Plan(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String = "",
    val category: String,
    val startDate: Long,
    val endDate: Long,
    val status: PlanStatus = PlanStatus.DRAFT,
    val coverColor: String = "#4F46E5",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)