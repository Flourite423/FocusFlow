package com.focusflow.data.db.converter

import androidx.room.TypeConverter
import com.focusflow.data.db.entity.Mood
import com.focusflow.data.db.entity.PlanStatus
import com.focusflow.data.db.entity.Priority
import com.focusflow.data.db.entity.TaskStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

object Converters {
    private val json = Json { ignoreUnknownKeys = true }

    // --- Enum converters ---

    @TypeConverter
    fun taskStatusToString(status: TaskStatus?): String? = status?.value

    @TypeConverter
    fun stringToTaskStatus(value: String?): TaskStatus? = value?.let { TaskStatus.fromValue(it) }

    @TypeConverter
    fun priorityToString(priority: Priority?): String? = priority?.value

    @TypeConverter
    fun stringToPriority(value: String?): Priority? = value?.let { Priority.fromValue(it) }

    @TypeConverter
    fun planStatusToString(status: PlanStatus?): String? = status?.value

    @TypeConverter
    fun stringToPlanStatus(value: String?): PlanStatus? = value?.let { PlanStatus.fromValue(it) }

    @TypeConverter
    fun moodToString(mood: Mood?): String? = mood?.value

    @TypeConverter
    fun stringToMood(value: String?): Mood? = value?.let { Mood.fromValue(it) }

    // --- List converters (kotlinx-serialization) ---

    @TypeConverter
    fun stringListToJson(list: List<String>?): String? =
        list?.let { json.encodeToString(it) }

    @TypeConverter
    fun jsonToStringList(jsonStr: String?): List<String>? =
        jsonStr?.let { json.decodeFromString<List<String>>(it) }

    @TypeConverter
    fun intListToJson(list: List<Int>?): String? =
        list?.let { json.encodeToString(it) }

    @TypeConverter
    fun jsonToIntList(jsonStr: String?): List<Int>? =
        jsonStr?.let { json.decodeFromString<List<Int>>(it) }

    @TypeConverter
    fun longListToJson(list: List<Long>?): String? =
        list?.let { json.encodeToString(it) }

    @TypeConverter
    fun jsonToLongList(jsonStr: String?): List<Long>? =
        jsonStr?.let { json.decodeFromString<List<Long>>(it) }
}
