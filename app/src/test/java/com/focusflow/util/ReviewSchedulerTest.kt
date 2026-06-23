package com.focusflow.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ReviewSchedulerTest {

    @Test
    fun `nextReviewDate returns correct date for first round`() {
        val intervals = listOf(1, 3, 7, 14, 30)
        val result = ReviewScheduler.nextReviewDate(0, intervals)
        assertEquals(LocalDate.now().plusDays(1), result)
    }

    @Test
    fun `nextReviewDate returns correct date for second round`() {
        val intervals = listOf(1, 3, 7, 14, 30)
        val result = ReviewScheduler.nextReviewDate(1, intervals)
        assertEquals(LocalDate.now().plusDays(3), result)
    }

    @Test
    fun `nextReviewDate returns correct date for third round`() {
        val intervals = listOf(1, 3, 7, 14, 30)
        val result = ReviewScheduler.nextReviewDate(2, intervals)
        assertEquals(LocalDate.now().plusDays(7), result)
    }

    @Test
    fun `nextReviewDate uses last interval for out of bounds round`() {
        val intervals = listOf(1, 3, 7, 14, 30)
        val result = ReviewScheduler.nextReviewDate(10, intervals)
        assertEquals(LocalDate.now().plusDays(30), result)
    }

    @Test
    fun `nextReviewDate uses last interval for round equal to size`() {
        val intervals = listOf(1, 3, 7, 14, 30)
        val result = ReviewScheduler.nextReviewDate(5, intervals)
        assertEquals(LocalDate.now().plusDays(30), result)
    }

    @Test
    fun `isCompleted returns true when currentRound equals totalRounds`() {
        assertTrue(ReviewScheduler.isCompleted(5, 5))
    }

    @Test
    fun `isCompleted returns true when currentRound exceeds totalRounds`() {
        assertTrue(ReviewScheduler.isCompleted(6, 5))
    }

    @Test
    fun `isCompleted returns false when currentRound less than totalRounds`() {
        assertFalse(ReviewScheduler.isCompleted(3, 5))
    }

    @Test
    fun `isCompleted returns false for first round`() {
        assertFalse(ReviewScheduler.isCompleted(0, 5))
    }
}
