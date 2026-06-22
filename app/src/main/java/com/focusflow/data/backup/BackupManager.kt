package com.focusflow.data.backup

import android.content.Context
import android.net.Uri
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

        // Plans (status is PlanStatus enum -> use .value)
        val plansJson = JSONArray()
        for (p in db.planDao().getAllSync()) {
            plansJson.put(JSONObject().apply {
                put("id", p.id); put("title", p.title); put("description", p.description)
                put("category", p.category); put("startDate", p.startDate); put("endDate", p.endDate)
                put("status", p.status.value); put("coverColor", p.coverColor)
                put("createdAt", p.createdAt); put("updatedAt", p.updatedAt)
            })
        }
        root.put("plans", plansJson)

        // Milestones (status is String)
        val msJson = JSONArray()
        for (p2 in db.planDao().getAllSync()) for (m in db.milestoneDao().getAllSync(p2.id)) {
            msJson.put(JSONObject().apply {
                put("id", m.id); put("planId", m.planId); put("title", m.title)
                put("description", m.description)
                put("targetDate", if (m.targetDate != null) m.targetDate else JSONObject.NULL)
                put("order", m.order); put("status", m.status)
                put("createdAt", m.createdAt); put("updatedAt", m.updatedAt)
            })
        }
        root.put("milestones", msJson)

        // Tasks (status is TaskStatus enum, priority is Priority enum)
        val tasksJson = JSONArray()
        for (t in db.taskDao().getAllSync()) {
            tasksJson.put(JSONObject().apply {
                put("id", t.id); put("milestoneId", t.milestoneId); put("title", t.title)
                put("description", t.description); put("estimatedMinutes", t.estimatedMinutes)
                put("actualMinutes", t.actualMinutes); put("priority", t.priority.value)
                put("status", t.status.value)
                put("dependsOn", if (t.dependsOn != null) t.dependsOn else JSONObject.NULL)
                put("dueDate", if (t.dueDate != null) t.dueDate else JSONObject.NULL)
                put("completedAt", if (t.completedAt != null) t.completedAt else JSONObject.NULL)
                put("createdAt", t.createdAt); put("updatedAt", t.updatedAt)
            })
        }
        root.put("tasks", tasksJson)

        // DayAssignments
        val daJson = JSONArray()
        for (da in db.dayAssignmentDao().getAllSync()) {
            daJson.put(JSONObject().apply {
                put("id", da.id); put("taskId", da.taskId); put("date", da.date)
                put("order", da.order); put("isTemporary", da.isTemporary); put("createdAt", da.createdAt)
            })
        }
        root.put("dayAssignments", daJson)

        // StudySessions (mood is Mood? enum -> use .value if not null)
        val ssJson = JSONArray()
        for (s in db.studySessionDao().getAllSync()) {
            ssJson.put(JSONObject().apply {
                put("id", s.id); put("taskId", s.taskId); put("startTime", s.startTime)
                put("endTime", if (s.endTime != null) s.endTime else JSONObject.NULL)
                put("durationMinutes", s.durationMinutes); put("note", s.note)
                put("mood", if (s.mood != null) s.mood.value else JSONObject.NULL)
                put("createdAt", s.createdAt)
            })
        }
        root.put("studySessions", ssJson)

        // ReviewSchedules (status is String)
        val rsJson = JSONArray()
        for (rs in db.reviewScheduleDao().getAllSync()) {
            rsJson.put(JSONObject().apply {
                put("id", rs.id); put("taskId", rs.taskId); put("reviewIntervals", rs.reviewIntervals)
                put("currentRound", rs.currentRound); put("nextReviewDate", rs.nextReviewDate)
                put("lastReviewDate", if (rs.lastReviewDate != null) rs.lastReviewDate else JSONObject.NULL)
                put("totalRounds", rs.totalRounds); put("status", rs.status)
                put("createdAt", rs.createdAt); put("updatedAt", rs.updatedAt)
            })
        }
        root.put("reviewSchedules", rsJson)

        // ReviewLogs
        val rlJson = JSONArray()
        for (rl in db.reviewLogDao().getAllSync()) {
            rlJson.put(JSONObject().apply {
                put("id", rl.id); put("scheduleId", rl.scheduleId); put("round", rl.round)
                put("reviewedAt", rl.reviewedAt); put("createdAt", rl.createdAt)
            })
        }
        root.put("reviewLogs", rlJson)

        // DailyStats
        val dsJson = JSONArray()
        for (ds in db.dailyStatsDao().getAllSync()) {
            dsJson.put(JSONObject().apply {
                put("date", ds.date); put("totalMinutes", ds.totalMinutes)
                put("tasksCompleted", ds.tasksCompleted); put("reviewsDone", ds.reviewsDone)
                put("streakDays", ds.streakDays); put("updatedAt", ds.updatedAt)
            })
        }
        root.put("dailyStats", dsJson)

        return writeToFile(context, "focusflow_backup_${dateStr()}.json", root.toString(2))
    }

    suspend fun importFromJson(context: Context, uri: Uri): Boolean {
        // TODO: Implement full import with entity reconstruction
        // Requires careful handling of enum vs String status fields
        return false
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
