package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Use case for creating a new savings goal
 *
 * It retrieves the account UID, creates the goal with the specified name and target amount,
 * and then adds the new goal to the repository
 */
class CreateSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(name: String, amountMinorUnits: Int) {
    val accountUid = repository.accountDetails.value?.accounts?.firstOrNull()?.accountUid
      ?: return

    val newGoal =  repository.createSavingsGoal(
      accountUid = accountUid,
      name = name,
      amountMinorUnits = amountMinorUnits * 100,
      currency = "GBP"
    ) ?: return

    repository.addSavingsGoal(newGoal, accountUid)
  }
}
