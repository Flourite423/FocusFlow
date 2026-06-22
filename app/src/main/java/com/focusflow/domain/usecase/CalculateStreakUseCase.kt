package com.focusflow.domain.usecase

import com.focusflow.data.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {

    suspend operator fun invoke(): Int = streakRepository.calculateStreak()

    fun observe(): Flow<Int> = streakRepository.observeStreak()
}