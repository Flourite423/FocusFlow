package com.focusflow.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.focusflow.data.db.FocusFlowDatabase
import com.focusflow.data.db.entity.DailyStats
import com.focusflow.data.db.entity.DayAssignment
import com.focusflow.data.db.entity.Milestone
import com.focusflow.data.db.entity.Plan
import com.focusflow.data.db.entity.ReviewLog
import com.focusflow.data.db.entity.ReviewSchedule
import com.focusflow.data.db.entity.StudySession
import com.focusflow.data.db.entity.Task
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Export/import using org.json (built into Android, no extra dependency)
class BackupManager(private val db: FocusFlowDatabase) {

    suspend fun exportToJson(context: Context): Uri {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Plans
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

        // Milestones
        val msJson = JSONArray()
        for (m in db.milestoneDao().getAllSync()) {
            msJson.put(JSONObject().apply {
                put("id", m.id); put("planId", m.planId); put("title", m.title)
                put("description", m.description); put("targetDate", m.targetDate ?: JSONObject.NULL)
                put("order", m.order); put("status", m.status)
                put("createdAt", m.createdAt); put("updatedAt", m.updatedAt)
            })
        }
        root.put("milestones", msJson)

        // Tasks
        val tasksJson = JSONArray()
        for (t in db.taskDao().getAllSync()) {
            tasksJson.put(JSONObject().apply {
                put("id", t.id); put("milestoneId", t.milestoneId); put("title", t.title)
                put("description", t.description); put("estimatedMinutes", t.estimatedMinutes)
                put("actualMinutes", t.actualMinutes); put("priority", t.priority.value)
                put("status", t.status.value); put("dependsOn", t.dependsOn ?: JSONObject.NULL)
                put("dueDate", t.dueDate ?: JSONObject.NULL); put("completedAt", t.completedAt ?: JSONObject.NULL)
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

        // StudySessions
        val ssJson = JSONArray()
        for (s in db.studySessionDao().getAllSync()) {
            ssJson.put(JSONObject().apply {
                put("id", s.id); put("taskId", s.taskId); put("startTime", s.startTime)
                put("endTime", s.endTime ?: JSONObject.NULL); put("durationMinutes", s.durationMinutes)
                put("note", s.note); put("mood", s.mood ?: JSONObject.NULL); put("createdAt", s.createdAt)
            })
        }
        root.put("studySessions", ssJson)

        // ReviewSchedules
        val rsJson = JSONArray()
        for (rs in db.reviewScheduleDao().getAllSync()) {
            rsJson.put(JSONObject().apply {
                put("id", rs.id); put("taskId", rs.taskId); put("reviewIntervals", rs.reviewIntervals)
                put("currentRound", rs.currentRound); put("nextReviewDate", rs.nextReviewDate)
                put("lastReviewDate", rs.lastReviewDate ?: JSONObject.NULL); put("totalRounds", rs.totalRounds)
                put("status", rs.status); put("createdAt", rs.createdAt); put("updatedAt", rs.updatedAt)
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
            db.openHelper.writableDatabase.execSQL("DELETE FROM daily_stats")
            db.openHelper.writableDatabase.execSQL("DELETE FROM milestones")
            db.openHelper.writableDatabase.execSQL("DELETE FROM plans")

            // Import plans
            val plans = root.getJSONArray("plans")
            for (i in 0 until plans.length()) {
                val obj = plans.getJSONObject(i)
                db.planDao().upsert(Plan(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    category = obj.optString("category", "exam"),
                    startDate = obj.getLong("startDate"),
                    endDate = obj.getLong("endDate"),
                    status = com.focusflow.data.db.entity.PlanStatus.fromValue(obj.optString("status", "draft")),
                    coverColor = obj.optString("coverColor", "#4F46E5"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                ))
            }

            // Import milestones
            val milestones = root.getJSONArray("milestones")
            for (i in 0 until milestones.length()) {
                val obj = milestones.getJSONObject(i)
                db.milestoneDao().upsert(Milestone(
                    id = obj.getString("id"),
                    planId = obj.getString("planId"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    targetDate = if (obj.isNull("targetDate")) null else obj.getLong("targetDate"),
                    order = obj.optInt("order", 0),
                    status = obj.optString("status", "pending"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                ))
            }

            // Import tasks
            val tasks = root.getJSONArray("tasks")
            for (i in 0 until tasks.length()) {
                val obj = tasks.getJSONObject(i)
                db.taskDao().upsert(Task(
                    id = obj.getString("id"),
                    milestoneId = obj.getString("milestoneId"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    estimatedMinutes = obj.optInt("estimatedMinutes", 30),
                    actualMinutes = obj.optInt("actualMinutes", 0),
                    priority = com.focusflow.data.db.entity.Priority.fromValue(obj.optString("priority", "medium")),
                    status = com.focusflow.data.db.entity.TaskStatus.fromValue(obj.optString("status", "todo")),
                    dependsOn = if (obj.isNull("dependsOn")) null else obj.getString("dependsOn"),
                    dueDate = if (obj.isNull("dueDate")) null else obj.getLong("dueDate"),
                    completedAt = if (obj.isNull("completedAt")) null else obj.getLong("completedAt"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                ))
            }

            // Import day assignments
            val dayAssignments = root.getJSONArray("dayAssignments")
            for (i in 0 until dayAssignments.length()) {
                val obj = dayAssignments.getJSONObject(i)
                db.dayAssignmentDao().upsert(DayAssignment(
                    id = obj.getString("id"),
                    taskId = obj.getString("taskId"),
                    date = obj.getLong("date"),
                    order = obj.optInt("order", 0),
                    isTemporary = obj.optBoolean("isTemporary", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                ))
            }

            // Import study sessions
            val sessions = root.getJSONArray("studySessions")
            for (i in 0 until sessions.length()) {
                val obj = sessions.getJSONObject(i)
                db.studySessionDao().upsert(StudySession(
                    id = obj.getString("id"),
                    taskId = obj.getString("taskId"),
                    startTime = obj.getLong("startTime"),
                    endTime = if (obj.isNull("endTime")) null else obj.getLong("endTime"),
                    durationMinutes = obj.optInt("durationMinutes", 0),
                    note = obj.optString("note", ""),
                    mood = if (obj.isNull("mood")) null else obj.getString("mood"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                ))
            }

            // Import review schedules
            val schedules = root.getJSONArray("reviewSchedules")
            for (i in 0 until schedules.length()) {
                val obj = schedules.getJSONObject(i)
                db.reviewScheduleDao().upsert(ReviewSchedule(
                    id = obj.getString("id"),
                    taskId = obj.getString("taskId"),
                    reviewIntervals = obj.optString("reviewIntervals", "1,3,7,14,30"),
                    currentRound = obj.optInt("currentRound", 0),
                    nextReviewDate = obj.getLong("nextReviewDate"),
                    lastReviewDate = if (obj.isNull("lastReviewDate")) null else obj.getLong("lastReviewDate"),
                    totalRounds = obj.optInt("totalRounds", 5),
                    status = com.focusflow.data.db.entity.ReviewStatus.fromValue(obj.optString("status", "active")),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                ))
            }

            // Import review logs
            val logs = root.getJSONArray("reviewLogs")
            for (i in 0 until logs.length()) {
                val obj = logs.getJSONObject(i)
                db.reviewLogDao().upsert(ReviewLog(
                    id = obj.getString("id"),
                    scheduleId = obj.getString("scheduleId"),
                    round = obj.getInt("round"),
                    reviewedAt = obj.getLong("reviewedAt"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                ))
            }

            // Import daily stats
            val stats = root.getJSONArray("dailyStats")
            for (i in 0 until stats.length()) {
                val obj = stats.getJSONObject(i)
                db.dailyStatsDao().upsert(DailyStats(
                    date = obj.getLong("date"),
                    totalMinutes = obj.optInt("totalMinutes", 0),
                    tasksCompleted = obj.optInt("tasksCompleted", 0),
                    reviewsDone = obj.optInt("reviewsDone", 0),
                    streakDays = obj.optInt("streakDays", 0),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                ))
            }
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
