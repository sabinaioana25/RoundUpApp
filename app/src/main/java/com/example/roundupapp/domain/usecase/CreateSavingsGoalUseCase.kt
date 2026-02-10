package com.example.roundupapp.domain.usecase

import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Creates a new savings goal with the specified name and target amount in GBP
 * Adds the created goal to the repository on success
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
    return try {
      // Create the SavingsGoal
      val createResult = repository.createSavingsGoalsRequest(accountUid, name, currency, amountMinorUnits * 100)

      if (createResult is DataResult.Error) {
        return Result.failure(createResult.exception)
      }

      // Fetch the created goal from the server to get full details
      when (val goalsResult = repository.getSavingsGoalsWithResult(accountUid)) {
        is DataResult.Success -> {
          val createdGoal = goalsResult.data.firstOrNull()
            ?: return Result.failure(Exception("Failed to fetch created goal"))

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
