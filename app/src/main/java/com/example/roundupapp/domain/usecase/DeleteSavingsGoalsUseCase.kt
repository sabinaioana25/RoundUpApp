package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Deletes the first available savings goal from the account
 * Removes the goal from the repository on successful deletion
 */
class DeleteSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String
  ): Result<Unit> =
    try {
      repository.deleteSavingsGoalsWithResult(accountUid, savingsGoalUid)
      repository.cacheDeleteSavingsGoal(savingsGoalUid)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
