package com.focusflow.data.repository

import com.focusflow.data.db.dao.StudySessionDao
import com.focusflow.data.db.entity.StudySession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionRepository @Inject constructor(
    private val sessionDao: StudySessionDao
) {
    fun getSessionsByTaskId(taskId: String): Flow<List<StudySession>> = sessionDao.getByTaskId(taskId)
    fun getSessionsForDay(dayStart: Long, dayEnd: Long): Flow<List<StudySession>> = sessionDao.getByDateRange(dayStart, dayEnd)
    fun getTotalMinutesForDay(dayStart: Long, dayEnd: Long): Flow<Int> = sessionDao.getTotalMinutesForDay(dayStart, dayEnd)
    fun getTotalMinutesForWeek(weekStart: Long, weekEnd: Long): Flow<Int> = sessionDao.getTotalMinutesForWeek(weekStart, weekEnd)
    suspend fun createSession(session: StudySession) = sessionDao.upsert(session)
    suspend fun finishSession(sessionId: String, taskId: String, durationMinutes: Int) {
        sessionDao.finish(sessionId, System.currentTimeMillis(), durationMinutes, "", null)
    }
    suspend fun getAllSessions(): List<StudySession> = sessionDao.getAllSync()
}
