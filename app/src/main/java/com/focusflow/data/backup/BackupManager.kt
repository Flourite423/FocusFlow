package com.focusflow.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.focusflow.data.db.FocusFlowDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BackupManager(private val db: FocusFlowDatabase) {

    suspend fun exportToJson(context: Context): Uri {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val plansJson = JSONArray()
        for (p in db.planDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("title", p.title)
            obj.put("description", p.description)
            obj.put("category", p.category)
            obj.put("startDate", p.startDate)
            obj.put("endDate", p.endDate)
            obj.put("status", p.status)
            obj.put("coverColor", p.coverColor)
            obj.put("createdAt", p.createdAt)
            obj.put("updatedAt", p.updatedAt)
            plansJson.put(obj)
        }
        root.put("plans", plansJson)

        val msJson = JSONArray()
        for (m in db.milestoneDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("planId", m.planId)
            obj.put("title", m.title)
            obj.put("description", m.description)
            obj.put("targetDate", m.targetDate ?: JSONObject.NULL)
            obj.put("order", m.order)
            obj.put("status", m.status)
            obj.put("createdAt", m.createdAt)
            obj.put("updatedAt", m.updatedAt)
            msJson.put(obj)
        }
        root.put("milestones", msJson)

        val tasksJson = JSONArray()
        for (t in db.taskDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("milestoneId", t.milestoneId)
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("estimatedMinutes", t.estimatedMinutes)
            obj.put("actualMinutes", t.actualMinutes)
            obj.put("priority", t.priority)
            obj.put("status", t.status)
            obj.put("dependsOn", t.dependsOn ?: JSONObject.NULL)
            obj.put("dueDate", t.dueDate ?: JSONObject.NULL)
            obj.put("completedAt", t.completedAt ?: JSONObject.NULL)
            obj.put("createdAt", t.createdAt)
            obj.put("updatedAt", t.updatedAt)
            tasksJson.put(obj)
        }
        root.put("tasks", tasksJson)

        val daJson = JSONArray()
        for (da in db.dayAssignmentDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("id", da.id)
            obj.put("taskId", da.taskId)
            obj.put("date", da.date)
            obj.put("order", da.order)
            obj.put("isTemporary", da.isTemporary)
            obj.put("createdAt", da.createdAt)
            daJson.put(obj)
        }
        root.put("dayAssignments", daJson)

        val ssJson = JSONArray()
        for (s in db.studySessionDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("taskId", s.taskId)
            obj.put("startTime", s.startTime)
            obj.put("endTime", s.endTime ?: JSONObject.NULL)
            obj.put("durationMinutes", s.durationMinutes)
            obj.put("note", s.note)
            obj.put("mood", s.mood ?: JSONObject.NULL)
            obj.put("createdAt", s.createdAt)
            ssJson.put(obj)
        }
        root.put("studySessions", ssJson)

        val rsJson = JSONArray()
        for (rs in db.reviewScheduleDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("id", rs.id)
            obj.put("taskId", rs.taskId)
            obj.put("reviewIntervals", rs.reviewIntervals)
            obj.put("currentRound", rs.currentRound)
            obj.put("nextReviewDate", rs.nextReviewDate)
            obj.put("lastReviewDate", rs.lastReviewDate ?: JSONObject.NULL)
            obj.put("totalRounds", rs.totalRounds)
            obj.put("status", rs.status)
            obj.put("createdAt", rs.createdAt)
            obj.put("updatedAt", rs.updatedAt)
            rsJson.put(obj)
        }
        root.put("reviewSchedules", rsJson)

        val rlJson = JSONArray()
        for (rl in db.reviewLogDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("id", rl.id)
            obj.put("scheduleId", rl.scheduleId)
            obj.put("round", rl.round)
            obj.put("reviewedAt", rl.reviewedAt)
            obj.put("createdAt", rl.createdAt)
            rlJson.put(obj)
        }
        root.put("reviewLogs", rlJson)

        val dsJson = JSONArray()
        for (ds in db.dailyStatsDao().getAllSync()) {
            val obj = JSONObject()
            obj.put("date", ds.date)
            obj.put("totalMinutes", ds.totalMinutes)
            obj.put("tasksCompleted", ds.tasksCompleted)
            obj.put("reviewsDone", ds.reviewsDone)
            obj.put("streakDays", ds.streakDays)
            obj.put("updatedAt", ds.updatedAt)
            dsJson.put(obj)
        }
        root.put("dailyStats", dsJson)

        return writeToFile(context, "focusflow_backup_${dateStr()}.json", root.toString(2))
    }

    suspend fun importFromJson(context: Context, uri: Uri): Boolean {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonString)
        if (root.optInt("schemaVersion", 0) > 1) return false

        db.withTransaction {
            db.openHelper.writableDatabase.execSQL("DELETE FROM review_logs")
            db.openHelper.writableDatabase.execSQL("DELETE FROM review_schedules")
            db.openHelper.writableDatabase.execSQL("DELETE FROM study_sessions")
            db.openHelper.writableDatabase.execSQL("DELETE FROM day_assignments")
            db.openHelper.writableDatabase.execSQL("DELETE FROM tasks")
            db.openHelper.writableDatabase.execSQL("DELETE FROM milestones")
            db.openHelper.writableDatabase.execSQL("DELETE FROM plans")
            db.openHelper.writableDatabase.execSQL("DELETE FROM daily_stats")
        }
        return true
    }

    private fun writeToFile(context: Context, fileName: String, content: String): Uri {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Documents/FocusFlow")
        }
        val uri = context.contentResolver.insert(
            android.provider.MediaStore.Files.getContentUri("external"), contentValues
        )!!
        context.contentResolver.openOutputStream(uri)!!.use { it.write(content.toByteArray()) }
        return uri
    }

    private fun dateStr(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}
