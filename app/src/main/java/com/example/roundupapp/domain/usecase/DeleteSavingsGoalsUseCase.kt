package com.example.roundupapp.domain.usecase

import com.example.roundupapp.data.DataResult
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
  ): Result<Unit> {
    return try {
      when (val deleteResult = repository.deleteSavingsGoalsWithResult(accountUid, savingsGoalUid)) {
        is DataResult.Success -> {
          repository.cacheDeleteSavingsGoal(savingsGoalUid)
          Result.success(Unit)
        }

        is DataResult.Error -> Result.failure(deleteResult.exception)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
