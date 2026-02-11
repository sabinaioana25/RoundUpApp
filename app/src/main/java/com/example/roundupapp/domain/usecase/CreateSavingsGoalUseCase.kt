package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.models.CurrencyConverter
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.validation.Validator
import com.example.roundupapp.utils.Constants.ERROR_GOAL_NOT_FOUND
import com.example.roundupapp.utils.Constants.ERROR_UNEXPECTED_CREATE_GOAL
import com.example.roundupapp.utils.Constants.LogMessages.CREATE_GOAL_CACHE_FAILED
import com.example.roundupapp.utils.Constants.LogMessages.CREATE_GOAL_CACHE_SUCCESS
import com.example.roundupapp.utils.Constants.LogMessages.CREATE_GOAL_FETCH_FAILED
import com.example.roundupapp.utils.Constants.LogMessages.CREATE_GOAL_SERVER_FAILED
import com.example.roundupapp.utils.Constants.OFFLINE_CREATE_GOAL_ERROR
import com.example.roundupapp.utils.Constants.VALIDATION_GOAL_NAME
import com.example.roundupapp.utils.Constants.VALIDATION_TARGET_AMOUNT
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
        return Result.failure(OfflineException(OFFLINE_CREATE_GOAL_ERROR))
      }

      Validator.requireAccountUid(accountUid)
      Validator.requireNonBlankName(name, VALIDATION_GOAL_NAME)
      Validator.requirePositiveAmount(amountInPounds, VALIDATION_TARGET_AMOUNT)

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
          Log.e(TAG, CREATE_GOAL_SERVER_FAILED, createResult.exception)
          return Result.failure(createResult.exception)
        }
        is DataResult.Success -> {
          // Fetch the created goal to get full details
          return fetchAndCacheCreatedGoal(accountUid)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, ERROR_UNEXPECTED_CREATE_GOAL, e)
      Result.failure(e)
    }
  }

  private suspend fun fetchAndCacheCreatedGoal(accountUid: String): Result<DomainSavingsGoal> {
    return when (val goalsResult = repository.getSavingsGoalsWithResult(accountUid)) {
      is DataResult.Success -> {
        val createdGoal = goalsResult.data.firstOrNull()
          ?: return Result.failure(Exception(ERROR_GOAL_NOT_FOUND))

        cacheGoalAsync(accountUid, createdGoal)

        Result.success(createdGoal)
      }
      is DataResult.Error -> {
        Log.e(TAG, CREATE_GOAL_FETCH_FAILED, goalsResult.exception)
        Result.failure(goalsResult.exception)
      }
    }
  }

  private suspend fun cacheGoalAsync(accountUid: String, goal: DomainSavingsGoal) {
    when (val cacheResult = repository.cacheSavingsGoal(accountUid, goal)) {
      is DataResult.Error -> {
        Log.w(TAG, CREATE_GOAL_CACHE_FAILED, cacheResult.exception)
      }
      is DataResult.Success<*> -> {
        Log.d(TAG, CREATE_GOAL_CACHE_SUCCESS)
      }
    }
  }
}
