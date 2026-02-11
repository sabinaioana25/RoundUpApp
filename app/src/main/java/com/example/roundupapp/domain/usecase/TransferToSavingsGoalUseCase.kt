package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.validation.Validator
import java.util.UUID
import javax.inject.Inject

/**
 * Transfers amount to a savings goal
 * Validates inputs, generates transfer UID, and executes transfer
 */
class TransferToSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository,
  private val connectivityChecker: NetworkConnectivityChecker
) {
  companion object {
    private val TAG = TransferToSavingsGoalUseCase::class.java.simpleName
  }

  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int
  ): Result<Boolean> {
    return try {
      // Check connectivity first
      if (!connectivityChecker.isNetworkAvailable()) {
        return Result.failure(OfflineException("Cannot transfer funds while offline"))
      }

      // Centralized validation
      Validator.requireAccountUid(accountUid)
      Validator.requireSavingsGoalUid(savingsGoalUid)
      Validator.requirePositiveAmount(amountMinorUnits, "Transfer amount")

      // Generate unique transfer UID
      val transferUid = UUID.randomUUID().toString()

      Log.d(TAG, "Initiating transfer: $amountMinorUnits pence to goal $savingsGoalUid")

      when (val result = repository.transferToSavingsGoalWithResult(
        accountUid = accountUid,
        savingsGoalUid = savingsGoalUid,
        amountMinorUnits = amountMinorUnits,
        transferUid = transferUid
      )) {
        is DataResult.Success -> {
          Log.d(TAG, "Transfer successful: $transferUid")
          Result.success(result.data)
        }
        is DataResult.Error -> {
          Log.e(TAG, "Transfer failed: $transferUid", result.exception)
          Result.failure(result.exception)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error during transfer", e)
      Result.failure(e)
    }
  }
}
