package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class CreateSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(accountUid: String, name: String, amountMinorUnits: String): DomainSavingsGoal? {
    return repository.createSavingsGoal(
      accountUid = accountUid,
      name = name,
      amountMinorUnits = amountMinorUnits.toInt(),
      currency = "GBP"
    )
  }
}
