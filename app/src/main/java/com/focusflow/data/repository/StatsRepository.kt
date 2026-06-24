package com.focusflow.data.repository

import com.focusflow.data.db.dao.DailyStatsDao
import com.focusflow.data.db.entity.DailyStats
import com.focusflow.util.toEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class StatsRepository @Inject constructor(
    private val statsDao: DailyStatsDao
) {

    fun getStatsForDate(date: Long): Flow<DailyStats?> = statsDao.getByDate(date)

    suspend fun getStatsForDateSync(date: Long): DailyStats? = statsDao.getByDateSync(date)

    fun getStatsRange(from: Long, to: Long): Flow<List<DailyStats>> =
        statsDao.getDateRange(from, to)

    fun getHeatmapData(weeks: Int = 16): Flow<Map<Long, Int>> {
        val from = LocalDate.now().minusWeeks(weeks.toLong()).toEpochMillis()
        val to = System.currentTimeMillis()
        return statsDao.getDateRange(from, to).map { statsList ->
            statsList.associate { it.date to it.totalMinutes }
        }
    }

    fun getRecentStats(limit: Int = 30): Flow<List<DailyStats>> = statsDao.getRecent(limit)

    suspend fun upsertDailyStats(stats: DailyStats) = statsDao.upsert(stats)
    suspend fun addStudyMinutes(date: Long, minutes: Int) = statsDao.addStudyMinutes(date, minutes)

    suspend fun recordTaskCompleted(date: Long) = statsDao.incrementTasksCompleted(date)

    suspend fun recordReviewDone(date: Long) = statsDao.incrementReviewsDone(date)
}
