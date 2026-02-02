package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.randomHex
import javax.inject.Inject

/**
 * Transfers the calculated round-up amount to the first available savings goal
 * Generates a unique transfer UID and refreshes account data on success
 */
class TransferToSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(): Boolean {
    val details = repository.accountDetails.value ?: return false
    val accountUid = details.accounts.firstOrNull()?.accountUid ?: return false
    val goal = details.savingsGoals.firstOrNull() ?: return false

    // Random-generated transfer UID for the transfer
    val transferUidBase = "aaaaa880-aaaa-4aaa-aaaa-aaaaaaaaaaaa"

    val success = repository.transferToSavingsGoal(
      accountUid = accountUid,
      savingsGoalUid = goal.savingsGoalUid,
      transferUid = transferUidBase.dropLast(4) + randomHex(4)
    )

    if (success) {
      repository.refreshFromNetwork()
    }
    return success
  }
}
