package com.focusflow.data.db.converter

import androidx.room.TypeConverter
import com.focusflow.data.db.entity.Mood
import com.focusflow.data.db.entity.PlanStatus
import com.focusflow.data.db.entity.Priority
import com.focusflow.data.db.entity.TaskStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val intListType = object : TypeToken<List<Int>>() {}.type
    private val longListType = object : TypeToken<List<Long>>() {}.type

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

    @TypeConverter
    fun stringListToJson(list: List<String>?): String? = list?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToStringList(json: String?): List<String>? = json?.let { gson.fromJson(it, stringListType) }

    @TypeConverter
    fun intListToJson(list: List<Int>?): String? = list?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToIntList(json: String?): List<Int>? = json?.let { gson.fromJson(it, intListType) }

    @TypeConverter
    fun longListToJson(list: List<Long>?): String? = list?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToLongList(json: String?): List<Long>? = json?.let { gson.fromJson(it, longListType) }
}