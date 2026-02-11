package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.validation.Validator
import com.example.roundupapp.utils.Constants.ERROR_UNEXPECTED_DELETE_GOAL
import com.example.roundupapp.utils.Constants.LogMessages.DELETE_CACHE_FAILED
import com.example.roundupapp.utils.Constants.LogMessages.DELETE_CACHE_SUCCESS
import com.example.roundupapp.utils.Constants.LogMessages.DELETE_GOAL_FAILED
import com.example.roundupapp.utils.Constants.OFFLINE_DELETE_GOAL_ERROR
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
        return Result.failure(OfflineException(OFFLINE_DELETE_GOAL_ERROR))
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
          Log.e(TAG, DELETE_GOAL_FAILED, deleteResult.exception)
          Result.failure(deleteResult.exception)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, ERROR_UNEXPECTED_DELETE_GOAL, e)
      Result.failure(e)
    }
  }

  private suspend fun deleteCacheAsync(savingsGoalUid: String) {
    when (val cacheDeleteResult = repository.cacheDeleteSavingsGoal(savingsGoalUid)) {
      is DataResult.Error -> {
        Log.w(TAG, DELETE_CACHE_FAILED, cacheDeleteResult.exception)
      }
      is DataResult.Success<*> -> {
        Log.d(TAG, DELETE_CACHE_SUCCESS)
      }
    }
  }
}
