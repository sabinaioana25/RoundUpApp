package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class DeleteSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(accountUid: String, savingsGoalUid: String): Boolean {
    return repository.deleteSavingsGoal(
      accountUid = accountUid,
      savingsGoalUid = savingsGoalUid
    )
  }
}
