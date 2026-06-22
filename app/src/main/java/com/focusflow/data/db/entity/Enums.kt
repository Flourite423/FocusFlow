package com.focusflow.data.db.entity

enum class TaskStatus(val value: String) {
    TODO("todo"),
    IN_PROGRESS("in_progress"),
    DONE("done"),
    SKIPPED("skipped");

    companion object {
        fun fromValue(value: String): TaskStatus = values().first { it.value == value }
    }
}

enum class Priority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    companion object {
        fun fromValue(value: String): Priority = values().first { it.value == value }
    }
}

enum class PlanStatus(val value: String) {
    DRAFT("draft"),
    ACTIVE("active"),
    COMPLETED("completed"),
    ARCHIVED("archived");

    companion object {
        fun fromValue(value: String): PlanStatus = values().first { it.value == value }
    }
}

enum class Mood(val value: String) {
    RELAXED("relaxed"),
    NORMAL("normal"),
    STRUGGLING("struggling"),
    EXHAUSTED("exhausted");

    companion object {
        fun fromValue(value: String): Mood = values().first { it.value == value }
    }
}