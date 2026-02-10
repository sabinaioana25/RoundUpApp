package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.repository.RoundUpRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Transfers amount to savings goal with validation and offline detection
 * Handles UID generation, transfer execution, and proper error logging
 */
class TransferToSavingsGoalUseCase @Inject constructor(
  private val repository: RoundUpRepository,
  private val connectivityChecker: NetworkConnectivityChecker
) {
  suspend operator fun invoke(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int
  ): Result<Boolean> {
    // Check connectivity first
    if (!connectivityChecker.isNetworkAvailable()) {
      return Result.failure(OfflineException("Cannot transfer funds while offline"))
    }

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
      
      Log.d(TAG, "Initiating transfer: $amountMinorUnits pence to goal $savingsGoalUid with transferUid $transferUid")
      
      val result = repository.transferToSavingsGoalWithResult(
        accountUid = accountUid,
        savingsGoalUid = savingsGoalUid,
        amountMinorUnits = amountMinorUnits,
        transferUid = transferUid
      )

      when (result) {
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

  companion object {
    private val TAG = TransferToSavingsGoalUseCase::class.java.simpleName
  }
}
