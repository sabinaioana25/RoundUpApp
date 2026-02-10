package com.example.roundupapp.domain.usecase

import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Transfers the specified amount to a savings goal
 */
class TransferToSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int,
    transferUid: String
  ): Result<Boolean> {
    return try {
      val result = repository.transferToSavingsGoalWithResult(
        accountUid = accountUid,
        savingsGoalUid = savingsGoalUid,
        amountMinorUnits = amountMinorUnits,
        transferUid = transferUid
      )

      when (result) {
        is DataResult.Success -> Result.success(result.data)
        is DataResult.Error -> Result.failure(result.exception)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
