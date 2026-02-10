package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.ValidationException
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Deletes the first available savings goal from the account
 * Removes the goal from the repository on successful deletion
 */
class DeleteSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository,
  private val connectivityChecker: NetworkConnectivityChecker
) {
  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String
  ): Result<Unit> {
    // Check connectivity first
    if (!connectivityChecker.isNetworkAvailable()) {
      return Result.failure(OfflineException("Cannot delete savings goal while offline"))
    }

    if (accountUid.isBlank()) {
      return Result.failure(ValidationException("Account UID is required"))
    }

    if (savingsGoalUid.isBlank()) {
      return Result.failure(ValidationException("Savings goal UID is required"))
    }

    return try {
      // Delete from server first
      when (val deleteResult = repository.deleteSavingsGoalsWithResult(accountUid, savingsGoalUid)) {
        is DataResult.Success -> {
          // Server deletion succeeded, delete from cache
          val cacheDeleteResult = repository.cacheDeleteSavingsGoal(savingsGoalUid)

          if (cacheDeleteResult is DataResult.Error) {
            Log.w(TAG, "Failed to delete goal from cache, but server deletion succeeded", cacheDeleteResult.exception)
          }

          Result.success(Unit)
        }
        is DataResult.Error -> {
          Result.failure(deleteResult.exception)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error deleting savings goal", e)
      Result.failure(e)
    }
  }

  companion object {
    private val TAG = DeleteSavingsGoalUseCase::class.java.simpleName
  }
}
