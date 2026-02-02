package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

class DeleteSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke() {
    val details = repository.accountDetails.value ?: return
    val accountUid = details.accounts.firstOrNull()?.accountUid ?: return
    val savingsGoalUid = details.savingsGoals.firstOrNull()?.savingsGoalUid ?: return

    val wasDeleted = repository.deleteSavingsGoal(
      accountUid = accountUid,
      savingsGoalUid = savingsGoalUid
    )

    if (wasDeleted) {
      repository.removeSavingsGoal(savingsGoalUid)
    }
  }
}
