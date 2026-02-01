package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class TransferToSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int
  ): Boolean {
    val transferUid = "aaaaa880-aaaa-4aaa-aaaa-aaaaaaaaaaaa"
    return repository.transferToSavingsGoal(
      accountUid = accountUid,
      savingsGoalUid = savingsGoalUid,
      transferUid = transferUid
    )
  }
}
