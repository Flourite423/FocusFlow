package com.focusflow.util

import com.focusflow.data.db.entity.DailyStats
import java.time.LocalDate

object StreakCalculator {
    const val MIN_MINUTES = 5

    fun calculate(
        dailyStats: List<DailyStats>,
        freezeUsed: Int,
        freezeLimit: Int
    ): Int {
        var streak = 0
        var freezesLeft = freezeLimit - freezeUsed
        for (stats in dailyStats) {
            if (stats.totalMinutes >= MIN_MINUTES) {
                streak++
            } else if (freezesLeft > 0) {
                freezesLeft--
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Check if the streak should reset at month boundary.
     * If the last active day was in a different month than today, reset streak.
     */
    fun shouldResetMonthly(lastActiveDate: Long?): Boolean {
        if (lastActiveDate == null) return false
        val lastMonth = lastActiveDate.toLocalDate().withDayOfMonth(1)
        val thisMonth = LocalDate.now().withDayOfMonth(1)
        return lastMonth.isBefore(thisMonth)
    }
}
