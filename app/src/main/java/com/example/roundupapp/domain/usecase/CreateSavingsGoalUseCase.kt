package com.example.roundupapp.domain.usecase

import com.example.roundupapp.data.NetworkResult
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
  ): Result<DomainSavingsGoal> =
    try {
      repository.createSavingsGoalsRequest(accountUid, name, currency, amountMinorUnits * 100)

      val goal = when (val goals = repository.getSavingsGoalsWithResult(accountUid)) {
        is NetworkResult.Success -> {
          val fetchedGoal = goals.data.first()
          repository.cacheSavingsGoal(accountUid, fetchedGoal)
          fetchedGoal
        }

        is NetworkResult.Error -> {
          throw goals.exception
        }
      }
      Result.success(goal)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
