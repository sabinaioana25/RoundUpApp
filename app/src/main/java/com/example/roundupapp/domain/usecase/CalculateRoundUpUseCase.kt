package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Use case for calculating the total round-up amount from all outgoing transactions
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
