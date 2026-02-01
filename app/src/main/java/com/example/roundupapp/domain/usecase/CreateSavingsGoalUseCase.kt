package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class CreateSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(name: String, amountMinorUnits: Int) {
    val accountUid = repository.accountDetails.value?.accounts?.firstOrNull()?.accountUid
      ?: return

    val newGoal =  repository.createSavingsGoal(
      accountUid = accountUid,
      name = name,
      amountMinorUnits = amountMinorUnits,
      currency = "GBP"
    ) ?: return

    repository.addSavingsGoal(newGoal)
  }
}
