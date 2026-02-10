package com.example.roundupapp.domain.usecase

import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Creates a new savings goal with validation and business logic
 * Handles all business rules for goal creation
 */
class CreateSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(
    accountUid: String,
    name: String,
    amountMinorUnits: Int,
    currency: String
  ): Result<DomainSavingsGoal> {
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
        return Result.failure(createResult.exception)
      }

      // Fetch the created goal to get full details
      when (val goalsResult = repository.getSavingsGoalsWithResult(accountUid)) {
        is DataResult.Success -> {
          val createdGoal = goalsResult.data.firstOrNull()
            ?: return Result.failure(Exception("Created goal not found"))
          
          // Cache the new goal
          repository.cacheSavingsGoal(accountUid, createdGoal)
          
          Result.success(createdGoal)
        }
        is DataResult.Error -> {
          Result.failure(goalsResult.exception)
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}

/**
 * Exception for validation errors
 */
class ValidationException(message: String) : Exception(message)
