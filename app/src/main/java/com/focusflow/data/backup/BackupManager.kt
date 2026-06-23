package com.focusflow.data.backup

import android.content.Context
import android.net.Uri
import com.focusflow.data.db.FocusFlowDatabase
import com.focusflow.data.db.entity.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BackupManager(private val db: FocusFlowDatabase) {

    suspend fun exportToJson(context: Context): Uri {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("exportedAt", System.currentTimeMillis())

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

        val daJson = JSONArray()
        for (da in db.dayAssignmentDao().getAllSync()) {
            daJson.put(JSONObject().apply {
                put("id", da.id); put("taskId", da.taskId); put("date", da.date)
                put("order", da.order); put("isTemporary", da.isTemporary); put("createdAt", da.createdAt)
            })
        }
        root.put("dayAssignments", daJson)

        val ssJson = JSONArray()
        for (s in db.studySessionDao().getAllSync()) {
            ssJson.put(JSONObject().apply {
                put("id", s.id); put("taskId", s.taskId); put("startTime", s.startTime)
                put("endTime", if (s.endTime != null) s.endTime else JSONObject.NULL)
                put("durationMinutes", s.durationMinutes); put("note", s.note)
                put("mood", if (s.mood != null) s.mood else JSONObject.NULL)
                put("createdAt", s.createdAt)
            })
        }
        root.put("studySessions", ssJson)

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

        val rlJson = JSONArray()
        for (rl in db.reviewLogDao().getAllSync()) {
            rlJson.put(JSONObject().apply {
                put("id", rl.id); put("scheduleId", rl.scheduleId); put("round", rl.round)
                put("reviewedAt", rl.reviewedAt); put("createdAt", rl.createdAt)
            })
        }
        root.put("reviewLogs", rlJson)

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
            ?: throw IOException("Failed to write backup file")
    }

    suspend fun importFromJson(context: Context, uri: Uri): Boolean {
        return try {
            val json = readFromUri(context, uri) ?: return false
            val root = JSONObject(json)
            val schemaVersion = root.optInt("schemaVersion", 1)
            if (schemaVersion > 1) return false

            db.reviewLogDao().deleteAll()
            db.reviewScheduleDao().deleteAll()
            db.studySessionDao().deleteAll()
            db.dayAssignmentDao().deleteAll()
            db.taskDao().deleteAll()
            db.milestoneDao().deleteAll()
            db.planDao().deleteAll()
            db.dailyStatsDao().deleteAll()

            val plans = mutableListOf<Plan>()
            val plansArr = root.optJSONArray("plans") ?: JSONArray()
            for (i in 0 until plansArr.length()) {
                val o = plansArr.getJSONObject(i)
                plans.add(Plan(id = o.getString("id"), title = o.getString("title"), description = o.optString("description", ""),
                    category = o.optString("category", "general"), startDate = o.getLong("startDate"), endDate = o.getLong("endDate"),
                    status = PlanStatus.fromValue(o.optString("status", "draft")), coverColor = o.optString("coverColor", "#4F46E5"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())))
            }
            if (plans.isNotEmpty()) db.planDao().upsert(plans)

            val milestones = mutableListOf<Milestone>()
            val msArr = root.optJSONArray("milestones") ?: JSONArray()
            for (i in 0 until msArr.length()) {
                val o = msArr.getJSONObject(i)
                milestones.add(Milestone(id = o.getString("id"), planId = o.getString("planId"), title = o.getString("title"),
                    description = o.optString("description", ""), targetDate = if (o.isNull("targetDate")) null else o.getLong("targetDate"),
                    order = o.optInt("order", 0), status = o.optString("status", "pending"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())))
            }
            if (milestones.isNotEmpty()) db.milestoneDao().upsert(milestones)

            val tasks = mutableListOf<Task>()
            val tasksArr = root.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tasksArr.length()) {
                val o = tasksArr.getJSONObject(i)
                tasks.add(Task(id = o.getString("id"), milestoneId = o.getString("milestoneId"), title = o.getString("title"),
                    description = o.optString("description", ""), estimatedMinutes = o.optInt("estimatedMinutes", 30),
                    actualMinutes = o.optInt("actualMinutes", 0), priority = Priority.fromValue(o.optString("priority", "medium")),
                    status = TaskStatus.fromValue(o.optString("status", "todo")),
                    dependsOn = if (o.isNull("dependsOn")) null else o.getString("dependsOn"),
                    dueDate = if (o.isNull("dueDate")) null else o.getLong("dueDate"),
                    completedAt = if (o.isNull("completedAt")) null else o.getLong("completedAt"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())))
            }
            if (tasks.isNotEmpty()) db.taskDao().upsert(tasks)

            val assignments = mutableListOf<DayAssignment>()
            val daArr = root.optJSONArray("dayAssignments") ?: JSONArray()
            for (i in 0 until daArr.length()) {
                val o = daArr.getJSONObject(i)
                assignments.add(DayAssignment(id = o.getString("id"), taskId = o.getString("taskId"), date = o.getLong("date"),
                    order = o.optInt("order", 0), isTemporary = o.optBoolean("isTemporary", false),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())))
            }
            if (assignments.isNotEmpty()) db.dayAssignmentDao().upsert(assignments)

            val sessions = mutableListOf<StudySession>()
            val ssArr = root.optJSONArray("studySessions") ?: JSONArray()
            for (i in 0 until ssArr.length()) {
                val o = ssArr.getJSONObject(i)
                sessions.add(StudySession(id = o.getString("id"), taskId = o.getString("taskId"), startTime = o.getLong("startTime"),
                    endTime = if (o.isNull("endTime")) null else o.getLong("endTime"), durationMinutes = o.optInt("durationMinutes", 0),
                    note = o.optString("note", ""), mood = if (o.isNull("mood")) null else o.getString("mood"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())))
            }
            if (sessions.isNotEmpty()) db.studySessionDao().upsert(sessions)

            val schedules = mutableListOf<ReviewSchedule>()
            val rsArr = root.optJSONArray("reviewSchedules") ?: JSONArray()
            for (i in 0 until rsArr.length()) {
                val o = rsArr.getJSONObject(i)
                schedules.add(ReviewSchedule(id = o.getString("id"), taskId = o.getString("taskId"),
                    reviewIntervals = o.optString("reviewIntervals", "[1,3,7,14,30]"), currentRound = o.optInt("currentRound", 0),
                    nextReviewDate = o.getLong("nextReviewDate"),
                    lastReviewDate = if (o.isNull("lastReviewDate")) null else o.getLong("lastReviewDate"),
                    totalRounds = o.optInt("totalRounds", 5), status = o.optString("status", "active"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())))
            }
            if (schedules.isNotEmpty()) db.reviewScheduleDao().upsert(schedules)

            val logs = mutableListOf<ReviewLog>()
            val rlArr = root.optJSONArray("reviewLogs") ?: JSONArray()
            for (i in 0 until rlArr.length()) {
                val o = rlArr.getJSONObject(i)
                logs.add(ReviewLog(id = o.getString("id"), scheduleId = o.getString("scheduleId"),
                    round = o.getInt("round"), reviewedAt = o.getLong("reviewedAt"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())))
            }
            if (logs.isNotEmpty()) db.reviewLogDao().upsert(logs)

            val stats = mutableListOf<DailyStats>()
            val dsArr = root.optJSONArray("dailyStats") ?: JSONArray()
            for (i in 0 until dsArr.length()) {
                val o = dsArr.getJSONObject(i)
                stats.add(DailyStats(date = o.getLong("date"), totalMinutes = o.optInt("totalMinutes", 0),
                    tasksCompleted = o.optInt("tasksCompleted", 0), reviewsDone = o.optInt("reviewsDone", 0),
                    streakDays = o.optInt("streakDays", 0), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())))
            }
            if (stats.isNotEmpty()) db.dailyStatsDao().upsert(stats)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().use(BufferedReader::readText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun writeToFile(context: Context, fileName: String, content: String): Uri? {
        return try {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Documents/FocusFlow")
            }
            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Files.getContentUri("external"), contentValues
            ) ?: return null
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return null
            outputStream.use { it.write(content.toByteArray()) }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun dateStr(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}
