package com.focusflow.data.backup

import android.content.Context
import android.net.Uri
import com.focusflow.data.db.FocusFlowDatabase
import com.focusflow.data.db.entity.DailyStats
import com.focusflow.data.db.entity.DayAssignment
import com.focusflow.data.db.entity.Milestone
import com.focusflow.data.db.entity.Plan
import com.focusflow.data.db.entity.ReviewLog
import com.focusflow.data.db.entity.ReviewSchedule
import com.focusflow.data.db.entity.StudySession
import com.focusflow.data.db.entity.Task
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class BackupData(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val plans: List<Plan> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val dayAssignments: List<DayAssignment> = emptyList(),
    val studySessions: List<StudySession> = emptyList(),
    val reviewSchedules: List<ReviewSchedule> = emptyList(),
    val reviewLogs: List<ReviewLog> = emptyList(),
    val dailyStats: List<DailyStats> = emptyList()
)

class BackupManager(private val db: FocusFlowDatabase) {

    private val json = Json { prettyPrint = true }

    suspend fun exportToJson(context: Context): Uri {
        val data = BackupData(
            exportedAt = System.currentTimeMillis(),
            plans = db.planDao().getAllSync(),
            milestones = db.milestoneDao().getAllSync(),
            tasks = db.taskDao().getAllSync(),
            dayAssignments = db.dayAssignmentDao().getAllSync(),
            studySessions = db.studySessionDao().getAllSync(),
            reviewSchedules = db.reviewScheduleDao().getAllSync(),
            reviewLogs = db.reviewLogDao().getAllSync(),
            dailyStats = db.dailyStatsDao().getAllSync()
        )
        val jsonString = json.encodeToString(data)
        return writeToFile(context, "focusflow_backup_${dateStr()}.json", jsonString)
    }

    suspend fun importFromJson(context: Context, uri: Uri): Boolean {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val data = json.decodeFromString<BackupData>(jsonString)
        if (data.schemaVersion > 1) return false

        db.withTransaction {
            clearAllTables()
            data.plans.forEach { db.planDao().upsert(it) }
            data.milestones.forEach { db.milestoneDao().upsert(it) }
            data.tasks.forEach { db.taskDao().upsert(it) }
            data.dayAssignments.forEach { db.dayAssignmentDao().upsert(it) }
            data.studySessions.forEach { db.studySessionDao().upsert(it) }
            data.reviewSchedules.forEach { db.reviewScheduleDao().upsert(it) }
            data.reviewLogs.forEach { db.reviewLogDao().upsert(it) }
            data.dailyStats.forEach { db.dailyStatsDao().upsert(it) }
        }
        return true
    }

    private fun clearAllTables() {
        val database = db.openHelper.writableDatabase
        database.execSQL("DELETE FROM review_logs")
        database.execSQL("DELETE FROM review_schedules")
        database.execSQL("DELETE FROM study_sessions")
        database.execSQL("DELETE FROM day_assignments")
        database.execSQL("DELETE FROM tasks")
        database.execSQL("DELETE FROM milestones")
        database.execSQL("DELETE FROM plans")
        database.execSQL("DELETE FROM daily_stats")
    }

    private fun writeToFile(context: Context, fileName: String, content: String): Uri {
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Documents/FocusFlow")
        }
        val uri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)!!
        resolver.openOutputStream(uri).use { outputStream: OutputStream? ->
            outputStream?.write(content.toByteArray())
        }
        return uri
    }

    private fun dateStr(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}