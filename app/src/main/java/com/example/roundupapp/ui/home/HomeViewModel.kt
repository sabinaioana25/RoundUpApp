package com.example.roundupapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.usecase.AccountDetailsUseCase
import com.example.roundupapp.domain.usecase.CalculateRoundUpUseCase
import com.example.roundupapp.domain.usecase.CreateSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.DeleteSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.TransferToSavingsGoalUseCase
import com.example.roundupapp.ui.home.ErrorMapper.toUiError
import com.example.roundupapp.utils.Constants.ALERT_TRANSFER_FAILED
import com.example.roundupapp.utils.Constants.GOALS_CREATING_FAILURE
import com.example.roundupapp.utils.Constants.GOALS_FAILURE_DELETING
import com.example.roundupapp.utils.Constants.GOALS_NAME_BLANK_WARNING
import com.example.roundupapp.utils.Constants.GOALS_NO_SAVINGS_GOALS_TO_DELETE
import com.example.roundupapp.utils.Constants.GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ERROR_LOADING_INITIAL_DATA
import com.example.roundupapp.utils.Constants.HOME_SCREEN_NO_ROUND_UP_AVAILABLE
import com.example.roundupapp.utils.Constants.REPO_ACCOUNT_UID_MISSING
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen
 * Handles UI state and delegates business logic to use cases
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
  private val accountDetailsUseCase: AccountDetailsUseCase,
  private val createSavingsGoalUseCase: CreateSavingsGoalUseCase,
  private val deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase,
  private val transferToSavingsGoalUseCase: TransferToSavingsGoalUseCase,
  private val calculateRoundUpUseCase: CalculateRoundUpUseCase
) : ViewModel() {

  private val _state = MutableStateFlow(ScreenState())
  val state: StateFlow<ScreenState> = _state.asStateFlow()

  companion object {
    private val TAG = HomeViewModel::class.java.simpleName
  }

  init {
    loadAccountDetails(isInitialLoad = true)
  }

  // ================================================================================
  // Public API - Intent Processing
  // ================================================================================

  fun processIntent(intent: Intent) {
    when (intent) {
      is Intent.CreateSavingsGoal -> createSavingsGoal(
        name = intent.name,
        amountInPounds = intent.amountMinorUnits,
        currency = intent.currency
      )
      is Intent.DeleteSavingsGoal -> deleteSavingsGoal()
      is Intent.TransferToSavingsGoal -> transferToSavingsGoal()
      is Intent.Refresh -> loadAccountDetails(isInitialLoad = false)
      is Intent.DismissError -> dismissError()
    }
  }

  // ================================================================================
  // Private Methods - Loading
  // ================================================================================

  private fun loadAccountDetails(isInitialLoad: Boolean) {
    // Prevent concurrent loads
    if (_state.value.isLoading) {
      Log.d(TAG, "Already loading, skipping duplicate request")
      return
    }

    viewModelScope.launch {
      _state.update {
        it.copy(
          loadingState = if (isInitialLoad) {
            LoadingState.InitialLoading
          } else {
            LoadingState.Refreshing
          },
          error = null
        )
      }

      val result = accountDetailsUseCase()

      _state.update {
        if (result.isSuccess) {
          val details = result.getOrThrow()
          val firstAccount = details.accounts.firstOrNull()
          val roundUp = calculateRoundUpUseCase(details.transactions)

          it.copy(
            loadingState = LoadingState.Idle,
            accounts = details.accounts,
            balance = details.balance,
            transactions = details.transactions,
            savingsGoals = details.savingsGoals,
            roundedAmount = roundUp,
            accountUid = firstAccount?.accountUid.orEmpty(),
            defaultCategory = firstAccount?.defaultCategory.orEmpty(),
            dataSource = details.dataSource,
            error = null
          )
        } else {
          it.copy(
            loadingState = LoadingState.Idle,
            error = result.toUiError(HOME_SCREEN_ERROR_LOADING_INITIAL_DATA)
          )
        }
      }
    }
  }

  // ================================================================================
  // Private Methods - Goal Management
  // ================================================================================

  private fun createSavingsGoal(name: String, amountInPounds: Int, currency: String) {
    if (_state.value.isLoading) {
      Log.d(TAG, "Operation in progress, skipping create goal")
      return
    }

    if (name.isBlank()) {
      _state.update { it.copy(error = UiError.ValidationError(GOALS_NAME_BLANK_WARNING)) }
      return
    }

    if (amountInPounds <= 0) {
      _state.update {
        it.copy(error = UiError.ValidationError(GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO))
      }
      return
    }

    val accountUid = _state.value.accountUid
    if (accountUid.isBlank()) {
      _state.update { it.copy(error = UiError.DataError(REPO_ACCOUNT_UID_MISSING)) }
      return
    }

    viewModelScope.launch {
      _state.update {
        it.copy(
          loadingState = LoadingState.InProgress(LoadingState.Operation.CREATING_GOAL),
          error = null
        )
      }

      val result = createSavingsGoalUseCase(
        accountUid = accountUid,
        name = name,
        amountInPounds = amountInPounds,
        currency = currency
      )

      if (result.isSuccess) {
        Log.d(TAG, "Goal created successfully")
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        loadAccountDetails(isInitialLoad = false)
      } else {
        Log.e(TAG, "Failed to create goal", result.exceptionOrNull())
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = result.toUiError(
              defaultMessage = GOALS_CREATING_FAILURE,
              operation = LoadingState.Operation.CREATING_GOAL
            )
          )
        }
      }
    }
  }

  private fun deleteSavingsGoal() {
    if (_state.value.isLoading) {
      Log.d(TAG, "Operation in progress, skipping delete goal")
      return
    }

    val accountUid = _state.value.accountUid
    val savingsGoalUid = _state.value.savingsGoals.firstOrNull()?.savingsGoalUid

    if (accountUid.isBlank() || savingsGoalUid.isNullOrBlank()) {
      _state.update {
        it.copy(error = UiError.ValidationError(GOALS_NO_SAVINGS_GOALS_TO_DELETE))
      }
      return
    }

    viewModelScope.launch {
      _state.update {
        it.copy(
          loadingState = LoadingState.InProgress(LoadingState.Operation.DELETING_GOAL),
          error = null
        )
      }

      val result = deleteSavingsGoalUseCase(accountUid, savingsGoalUid)

      if (result.isSuccess) {
        Log.d(TAG, "Goal deleted successfully")
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        loadAccountDetails(isInitialLoad = false)
      } else {
        Log.e(TAG, "Failed to delete goal", result.exceptionOrNull())
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = result.toUiError(
              defaultMessage = GOALS_FAILURE_DELETING,
              operation = LoadingState.Operation.DELETING_GOAL
            )
          )
        }
      }
    }
  }

  private fun transferToSavingsGoal() {
    if (_state.value.isLoading) {
      Log.d(TAG, "Operation in progress, skipping transfer")
      return
    }

    val accountUid = _state.value.accountUid
    val goalUid = _state.value.savingsGoals.firstOrNull()?.savingsGoalUid
    val amount = _state.value.roundedAmount

    if (accountUid.isBlank() || goalUid.isNullOrBlank()) {
      _state.update { it.copy(error = UiError.DataError(REPO_ACCOUNT_UID_MISSING)) }
      return
    }

    if (amount <= 0) {
      _state.update { it.copy(error = UiError.ValidationError(HOME_SCREEN_NO_ROUND_UP_AVAILABLE)) }
      return
    }

    viewModelScope.launch {
      _state.update {
        it.copy(
          loadingState = LoadingState.InProgress(LoadingState.Operation.TRANSFERRING),
          error = null
        )
      }

      val result = transferToSavingsGoalUseCase(
        accountUid = accountUid,
        savingsGoalUid = goalUid,
        amountMinorUnits = amount
      )

      if (result.isSuccess) {
        Log.d(TAG, "Transfer completed successfully")
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        loadAccountDetails(isInitialLoad = false)
      } else {
        Log.e(TAG, "Failed to transfer", result.exceptionOrNull())
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = result.toUiError(
              defaultMessage = ALERT_TRANSFER_FAILED,
              operation = LoadingState.Operation.TRANSFERRING
            )
          )
        }
      }
    }
  }

  private fun dismissError() {
    _state.update { it.copy(error = null) }
  }
}
