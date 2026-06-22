package com.focusflow.util

import java.time.LocalDate

object ReviewScheduler {
    fun nextReviewDate(currentRound: Int, intervals: List<Int>): LocalDate {
        val days = intervals.getOrElse(currentRound) { intervals.last() }
        return LocalDate.now().plusDays(days.toLong())
    }

    fun isCompleted(currentRound: Int, totalRounds: Int): Boolean =
        currentRound >= totalRounds
}