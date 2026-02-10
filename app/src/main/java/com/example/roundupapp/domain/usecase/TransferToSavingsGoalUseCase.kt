package com.example.roundupapp.domain.usecase

import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.repository.RoundUpRepository
import java.util.UUID
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
    amountMinorUnits: Int
  ): Result<Boolean> {
    if (accountUid.isBlank()) {
      return Result.failure(ValidationException("Account UID is required"))
    }

    if (savingsGoalUid.isBlank()) {
      return Result.failure(ValidationException("Savings goal UID is required"))
    }

    if (amountMinorUnits <= 0) {
      return Result.failure(ValidationException("Transfer amount must be greater than zero"))
    }

    return try {
      // Generate transfer UID
      val transferUid = UUID.randomUUID().toString()

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
