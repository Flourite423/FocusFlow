package com.focusflow.util

import com.focusflow.data.db.entity.DailyStats
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StreakCalculatorTest {

    @Test
    fun `calculate returns 0 for empty stats`() {
        val stats = emptyList<DailyStats>()
        assertEquals(0, StreakCalculator.calculate(stats, freezeUsed = 0, freezeLimit = 2))
    }

    @Test
    fun `calculate counts consecutive active days`() {
        val stats = listOf(
            DailyStats(date = 1L, totalMinutes = 30),
            DailyStats(date = 2L, totalMinutes = 20),
            DailyStats(date = 3L, totalMinutes = 10)
        )
        assertEquals(3, StreakCalculator.calculate(stats, freezeUsed = 0, freezeLimit = 2))
    }

    @Test
    fun `calculate breaks on inactive day without freezes`() {
        val stats = listOf(
            DailyStats(date = 1L, totalMinutes = 30),
            DailyStats(date = 2L, totalMinutes = 0),
            DailyStats(date = 3L, totalMinutes = 20)
        )
        assertEquals(1, StreakCalculator.calculate(stats, freezeUsed = 0, freezeLimit = 0))
    }

    @Test
    fun `calculate uses freeze day for inactive day`() {
        val stats = listOf(
            DailyStats(date = 1L, totalMinutes = 30),
            DailyStats(date = 2L, totalMinutes = 0),
            DailyStats(date = 3L, totalMinutes = 20)
        )
        assertEquals(2, StreakCalculator.calculate(stats, freezeUsed = 0, freezeLimit = 2))
    }

    @Test
    fun `calculate breaks when all freezes exhausted`() {
        val stats = listOf(
            DailyStats(date = 1L, totalMinutes = 30),
            DailyStats(date = 2L, totalMinutes = 0),
            DailyStats(date = 3L, totalMinutes = 0),
            DailyStats(date = 4L, totalMinutes = 20)
        )
        assertEquals(2, StreakCalculator.calculate(stats, freezeUsed = 0, freezeLimit = 2))
    }

    @Test
    fun `calculate respects freezeUsed count`() {
        val stats = listOf(
            DailyStats(date = 1L, totalMinutes = 30),
            DailyStats(date = 2L, totalMinutes = 0),
            DailyStats(date = 3L, totalMinutes = 20)
        )
        assertEquals(1, StreakCalculator.calculate(stats, freezeUsed = 2, freezeLimit = 2))
    }

    @Test
    fun `calculate counts minimum minutes threshold`() {
        val stats = listOf(
            DailyStats(date = 1L, totalMinutes = 4),
            DailyStats(date = 2L, totalMinutes = 5)
        )
        assertEquals(0, StreakCalculator.calculate(stats, freezeUsed = 0, freezeLimit = 0))
    }

    @Test
    fun `shouldResetMonthly returns false for null date`() {
        assertEquals(false, StreakCalculator.shouldResetMonthly(null))
    }
}
