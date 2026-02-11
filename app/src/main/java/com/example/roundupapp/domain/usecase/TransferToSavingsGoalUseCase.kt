package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.validation.Validator
import com.example.roundupapp.utils.Constants.ERROR_UNEXPECTED_TRANSFER
import com.example.roundupapp.utils.Constants.LogMessages.TRANSFER_FAILED
import com.example.roundupapp.utils.Constants.LogMessages.TRANSFER_INITIATING
import com.example.roundupapp.utils.Constants.LogMessages.TRANSFER_SUCCESS
import com.example.roundupapp.utils.Constants.OFFLINE_TRANSFER_ERROR
import com.example.roundupapp.utils.Constants.VALIDATION_TRANSFER_AMOUNT
import com.example.roundupapp.utils.randomUuidV4
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
        return Result.failure(OfflineException(OFFLINE_TRANSFER_ERROR))
      }

      // Centralized validation
      Validator.requireAccountUid(accountUid)
      Validator.requireSavingsGoalUid(savingsGoalUid)
      Validator.requirePositiveAmount(amountMinorUnits, VALIDATION_TRANSFER_AMOUNT)

      // Generate unique transfer UID
      val transferUid = randomUuidV4()

      Log.d(TAG, String.format(TRANSFER_INITIATING, amountMinorUnits, savingsGoalUid))

      when (val result = repository.transferToSavingsGoalWithResult(
        accountUid = accountUid,
        savingsGoalUid = savingsGoalUid,
        amountMinorUnits = amountMinorUnits,
        transferUid = transferUid
      )) {
        is DataResult.Success -> {
          Log.d(TAG, String.format(TRANSFER_SUCCESS, transferUid))
          Result.success(result.data)
        }
        is DataResult.Error -> {
          Log.e(TAG, String.format(TRANSFER_FAILED, transferUid), result.exception)
          Result.failure(result.exception)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, ERROR_UNEXPECTED_TRANSFER, e)
      Result.failure(e)
    }
  }
}
