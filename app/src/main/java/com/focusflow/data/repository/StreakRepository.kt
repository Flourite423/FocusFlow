package com.focusflow.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.focusflow.data.db.dao.DailyStatsDao
import com.focusflow.util.toLocalDate
import com.focusflow.util.toEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class StreakRepository @Inject constructor(
    private val statsDao: DailyStatsDao,
    private val prefs: DataStore<Preferences>
) {

    companion object {
        const val MIN_STREAK_MINUTES = 5
        private const val FREEZE_USED_KEY = "freeze_used"
        private const val FREEZE_LIMIT_KEY = "freeze_limit"
        val freezeUsedKey = intPreferencesKey(FREEZE_USED_KEY)
        val freezeLimitKey = intPreferencesKey(FREEZE_LIMIT_KEY)
    }

    suspend fun calculateStreak(): Int {
        val freezeUsedThisMonth = prefs.data.first()[freezeUsedKey] ?: 0
        val freezeLimit = prefs.data.first()[freezeLimitKey] ?: 2
        var streak = 0
        var freezesRemaining = freezeLimit - freezeUsedThisMonth
        var date = LocalDate.now()

        while (true) {
            val stats = statsDao.getByDateSync(date.toEpochMillis())
            when {
                stats != null && stats.totalMinutes >= MIN_STREAK_MINUTES -> streak++
                freezesRemaining > 0 -> freezesRemaining--
                else -> break
            }
            date = date.minusDays(1)
        }
        return streak
    }

    fun observeStreak(): Flow<Int> = combine(
        statsDao.getRecent(365),
        prefs.data
    ) { statsList, preferences ->
        val freezeUsedThisMonth = preferences[freezeUsedKey] ?: 0
        val freezeLimit = preferences[freezeLimitKey] ?: 2
        var streak = 0
        var freezesRemaining = freezeLimit - freezeUsedThisMonth
        var expectedDate = LocalDate.now()

        for (stats in statsList) {
            val statsDate = stats.date.toLocalDate()
            // Gap between expected and actual date — use freezes to bridge
            val gap = java.time.temporal.ChronoUnit.DAYS.between(statsDate, expectedDate).toInt()
            if (gap > 0) {
                if (gap <= freezesRemaining) {
                    freezesRemaining -= gap
                } else {
                    break
                }
            }
            when {
                stats.totalMinutes >= MIN_STREAK_MINUTES -> streak++
                freezesRemaining > 0 -> freezesRemaining--
                else -> break
            }
            expectedDate = statsDate.minusDays(1)
        }
        streak
    }
}
