package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class TransferToSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(): Boolean {
    val details = repository.accountDetails.value ?: return false
    val accountUid = details.accounts.firstOrNull()?.accountUid ?: return false
    val goal = details.savingsGoals.firstOrNull() ?: return false

    val transferUid = "aaaaa880-aaaa-4aaa-aaaa-aaaaaaaaaaaa"

    return repository.transferToSavingsGoal(
      accountUid = accountUid,
      savingsGoalUid = goal.savingsGoalUid,
      transferUid = transferUid
    )
  }
}
