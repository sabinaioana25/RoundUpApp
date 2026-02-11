package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.validation.Validator
import javax.inject.Inject

/**
 * Deletes the first available savings goal from the account
 * Removes the goal from server first, then updates local cache
 */
class DeleteSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository,
  private val connectivityChecker: NetworkConnectivityChecker
) {
  companion object {
    private val TAG = DeleteSavingsGoalUseCase::class.java.simpleName
  }

  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String
  ): Result<Unit> {
    return try {
      // Check connectivity first
      if (!connectivityChecker.isNetworkAvailable()) {
        return Result.failure(OfflineException("Cannot delete savings goal while offline"))
      }

      // Centralized validation
      Validator.requireAccountUid(accountUid)
      Validator.requireSavingsGoalUid(savingsGoalUid)

      // Delete from server first
      when (val deleteResult = repository.deleteSavingsGoalsWithResult(accountUid, savingsGoalUid)) {
        is DataResult.Success -> {
          // Server deletion succeeded, delete from cache
          deleteCacheAsync(savingsGoalUid)
          Result.success(Unit)
        }
        is DataResult.Error -> {
          Log.e(TAG, "Failed to delete savings goal", deleteResult.exception)
          Result.failure(deleteResult.exception)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error deleting savings goal", e)
      Result.failure(e)
    }
  }

  private suspend fun deleteCacheAsync(savingsGoalUid: String) {
    when (val cacheDeleteResult = repository.cacheDeleteSavingsGoal(savingsGoalUid)) {
      is DataResult.Error -> {
        Log.w(TAG, "Failed to delete goal from cache (non-fatal)", cacheDeleteResult.exception)
      }
      is DataResult.Success<*> -> {
        Log.d(TAG, "Successfully deleted goal from cache")
      }
    }
  }
}
