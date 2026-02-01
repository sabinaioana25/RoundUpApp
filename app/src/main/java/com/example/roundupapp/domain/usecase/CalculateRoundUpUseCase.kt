package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class CalculateRoundUpUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke() {
    val details = repository.accountDetails.value ?: return
    val account = details.accounts.firstOrNull() ?: return

    val transactions = repository.getTransactions(account.accountUid, account.defaultCategory)
    val roundUpTotal =  transactions
      .filter { it.direction == "OUT" }
      .sumOf { item ->
        val remainder = item.amount.minorUnits % 100
        if (remainder == 0) 0 else 100 - remainder
      }

    repository.setRoundUpAmount(roundUpTotal)
  }
}
