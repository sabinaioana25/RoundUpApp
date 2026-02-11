package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.models.CurrencyConverter
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.validation.Validator
import javax.inject.Inject

/**
 * Creates a new savings goal with validation and offline detection
 * Handles server creation, fetching full details, and cache update
 */
class CreateSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository,
  private val connectivityChecker: NetworkConnectivityChecker
) {
  companion object {
    private val TAG = CreateSavingsGoalUseCase::class.java.simpleName
  }

  suspend operator fun invoke(
    accountUid: String,
    name: String,
    amountInPounds: Int,
    currency: String
  ): Result<DomainSavingsGoal> {
    return try {
      // Check connectivity first
      if (!connectivityChecker.isNetworkAvailable()) {
        return Result.failure(OfflineException("Cannot create savings goal while offline"))
      }

      Validator.requireAccountUid(accountUid)
      Validator.requireNonBlankName(name, "Goal name")
      Validator.requirePositiveAmount(amountInPounds, "Target amount")

      val amountMinorUnits = CurrencyConverter.poundsToMinorUnits(amountInPounds)

      // Create the savings goal
      val createResult = repository.createSavingsGoalsRequest(
        accountUid = accountUid,
        name = name,
        currency = currency,
        amountMinorUnits = amountMinorUnits
      )

      when (createResult) {
        is DataResult.Error -> {
          Log.e(TAG, "Failed to create savings goal on server", createResult.exception)
          return Result.failure(createResult.exception)
        }
        is DataResult.Success -> {
          // Fetch the created goal to get full details
          return fetchAndCacheCreatedGoal(accountUid)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error creating savings goal", e)
      Result.failure(e)
    }
  }

  private suspend fun fetchAndCacheCreatedGoal(accountUid: String): Result<DomainSavingsGoal> {
    return when (val goalsResult = repository.getSavingsGoalsWithResult(accountUid)) {
      is DataResult.Success -> {
        val createdGoal = goalsResult.data.firstOrNull()
          ?: return Result.failure(Exception("Created goal not found in server response"))

        cacheGoalAsync(accountUid, createdGoal)

        Result.success(createdGoal)
      }
      is DataResult.Error -> {
        Log.e(TAG, "Failed to fetch created goal details", goalsResult.exception)
        Result.failure(goalsResult.exception)
      }
    }
  }

  private suspend fun cacheGoalAsync(accountUid: String, goal: DomainSavingsGoal) {
    when (val cacheResult = repository.cacheSavingsGoal(accountUid, goal)) {
      is DataResult.Error -> {
        Log.w(TAG, "Failed to cache created goal", cacheResult.exception)
      }
      is DataResult.Success<*> -> {
        Log.d(TAG, "Successfully cached created goal")
      }
    }
  }
}
