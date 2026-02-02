package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Calculates total round-up amount from all outgoing transactions
 * Rounds each transaction up to the nearest pound and stores the sum
 *
 */
class CalculateRoundUpUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke() {
    val details = repository.accountDetails.value ?: return

    val roundUpTotal = details.transactions
      .filter { it.direction == "OUT" }
      .sumOf { item ->
        val remainder = item.amount.minorUnits % 100
        if (remainder == 0) 0 else 100 - remainder
      }
    repository.setRoundUpAmount(roundUpTotal)
  }
}
