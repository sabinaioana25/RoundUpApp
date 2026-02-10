package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Creates a new savings goal with validation and offline detection
 * Handles server creation, fetching full details, and cache update with proper error recovery
 */
class CreateSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository,
  private val connectivityChecker: NetworkConnectivityChecker
) {
  suspend operator fun invoke(
    accountUid: String,
    name: String,
    amountMinorUnits: Int,
    currency: String
  ): Result<DomainSavingsGoal> {
    // Check connectivity first
    if (!connectivityChecker.isNetworkAvailable()) {
      return Result.failure(OfflineException("Cannot create savings goal while offline"))
    }

    if (accountUid.isBlank()) {
      return Result.failure(ValidationException("Account UID is required"))
    }
    
    if (name.isBlank()) {
      return Result.failure(ValidationException("Goal name cannot be blank"))
    }
    
    if (amountMinorUnits <= 0) {
      return Result.failure(ValidationException("Target amount must be greater than zero"))
    }

    return try {
      // Create the savings goal
      val createResult = repository.createSavingsGoalsRequest(
        accountUid = accountUid,
        name = name,
        currency = currency,
        amountMinorUnits = amountMinorUnits
      )

      if (createResult is DataResult.Error) {
        Log.e(TAG, "Failed to create savings goal on server", createResult.exception)
        return Result.failure(createResult.exception)
      }

      // Fetch the created goal to get full details
      when (val goalsResult = repository.getSavingsGoalsWithResult(accountUid)) {
        is DataResult.Success -> {
          val createdGoal = goalsResult.data.firstOrNull()
            ?: return Result.failure(Exception("Created goal not found in server response"))
          
          // Cache the new goal
          val cacheResult = repository.cacheSavingsGoal(accountUid, createdGoal)

          if (cacheResult is DataResult.Error) {
            Log.e(TAG, "Failed to cache created goal, but creation succeeded", cacheResult.exception)
          }
          
          Result.success(createdGoal)
        }
        is DataResult.Error -> {
          Log.e(TAG, "Failed to fetch created goal details", goalsResult.exception)
          Result.failure(goalsResult.exception)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error creating savings goal", e)
      Result.failure(e)
    }
  }

  companion object {
    private val TAG = CreateSavingsGoalUseCase::class.java.simpleName
  }
}

/**
 * Exception for validation errors
 */
class ValidationException(message: String) : Exception(message)
