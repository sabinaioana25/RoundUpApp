package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class CalculateRoundUpUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(
    accountUid: String,
    categoryUid: String
  ): Int {
    val transactions = repository.getTransactions(accountUid, categoryUid)
    return transactions
      .filter { it.direction == "OUT" }
      .sumOf { item ->
        val remainder = item.amount.minorUnits % 100
        if (remainder == 0) 0 else 100 - remainder
      }
  }
}
