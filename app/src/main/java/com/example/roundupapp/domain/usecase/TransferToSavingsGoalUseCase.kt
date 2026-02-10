package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.randomUuidV4
import javax.inject.Inject

/**
 * Transfers the calculated round-up amount to the first available savings goal
 * Generates a unique transfer UID and refreshes account data on success
 */
class TransferToSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String,
    roundUpAmount: Int,
    transferUid: String = randomUuidV4()
  ): Result<Boolean> =
    try {
      repository.transferToSavingsGoalWithResult(
        accountUid = accountUid,
        savingsGoalUid = savingsGoalUid,
        amountMinorUnits = roundUpAmount,
        transferUid = transferUid
      )
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
