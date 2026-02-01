package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.toGbp
import jakarta.inject.Inject

data class RefreshedData(
  val balance: String,
  val savingsGoals: List<DomainSavingsGoal>
)

class RefreshDataUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(accountUid: String): RefreshedData {
    val balance = repository.getBalance(accountUid)?.effectiveBalance?.minorUnits.toGbp()
    val savingsGoals = repository.getSavingsGoals(accountUid)

    return RefreshedData(
      balance = balance,
      savingsGoals = savingsGoals
    )
  }
}
