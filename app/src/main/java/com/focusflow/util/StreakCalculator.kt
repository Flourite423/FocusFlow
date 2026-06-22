package com.focusflow.util

import com.focusflow.data.db.entity.DailyStats

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
}