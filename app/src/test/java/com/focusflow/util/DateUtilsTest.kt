package com.focusflow.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsTest {

    @Test
    fun `toEpochMillis and toLocalDate are inverse operations`() {
        val date = LocalDate.of(2024, 6, 15)
        val epoch = date.toEpochMillis()
        assertEquals(date, epoch.toLocalDate())
    }

    @Test
    fun `toEpochMillis returns midnight epoch`() {
        val date = LocalDate.of(2024, 1, 1)
        val epoch = date.toEpochMillis()
        val expected = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, epoch)
    }

    @Test
    fun `todayEpoch returns today date when converted back`() {
        val epoch = todayEpoch()
        assertEquals(LocalDate.now(), epoch.toLocalDate())
    }

    @Test
    fun `todayStart is before todayEnd`() {
        assertTrue(todayStart() < todayEnd())
    }

    @Test
    fun `weekStart is before weekEnd`() {
        assertTrue(weekStart() < weekEnd())
    }

    @Test
    fun `weekStart is Monday`() {
        val monday = weekStart().toLocalDate()
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
    }

    @Test
    fun `weekEnd is Sunday`() {
        val sunday = weekEnd().toLocalDate()
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)
    }

    @Test
    fun `monthStart is first day of month`() {
        val firstDay = monthStart().toLocalDate()
        assertEquals(1, firstDay.dayOfMonth)
    }

    @Test
    fun `monthEnd is last day of month`() {
        val lastDay = monthEnd().toLocalDate()
        assertEquals(lastDay.lengthOfMonth(), lastDay.dayOfMonth)
    }

    @Test
    fun `monthStart is before monthEnd`() {
        assertTrue(monthStart() < monthEnd())
    }

    @Test
    fun `toFormattedString returns correct format`() {
        val date = LocalDate.of(2024, 6, 15)
        val epoch = date.toEpochMillis()
        assertEquals("2024-06-15", epoch.toFormattedString())
    }

    @Test
    fun `toFormattedString pads single digits`() {
        val date = LocalDate.of(2024, 1, 5)
        val epoch = date.toEpochMillis()
        assertEquals("2024-01-05", epoch.toFormattedString())
    }
}
