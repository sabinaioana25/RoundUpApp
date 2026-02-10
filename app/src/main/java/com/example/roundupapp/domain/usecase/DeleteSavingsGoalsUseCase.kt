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
    if (accountUid.isBlank()) {
      return Result.failure(ValidationException("Account UID is required"))
    }

    if (savingsGoalUid.isBlank()) {
      return Result.failure(ValidationException("Savings goal UID is required"))
    }

    return try {
      // Delete from server
      when (val deleteResult = repository.deleteSavingsGoalsWithResult(accountUid, savingsGoalUid)) {
        is DataResult.Success -> {
          // Delete from cache
          repository.cacheDeleteSavingsGoal(savingsGoalUid)
          Result.success(Unit)
        }
        is DataResult.Error -> {
          Result.failure(deleteResult.exception)
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
