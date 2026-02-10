package com.example.roundupapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.usecase.AccountDetailsUseCase
import com.example.roundupapp.domain.usecase.CalculateRoundUpUseCase
import com.example.roundupapp.domain.usecase.CreateSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.DeleteSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.TransferToSavingsGoalUseCase
import com.example.roundupapp.utils.Constants.ALERT_TRANSFER_FAILED
import com.example.roundupapp.utils.Constants.GOALS_CREATING_FAILURE
import com.example.roundupapp.utils.Constants.GOALS_FAILURE_DELETING
import com.example.roundupapp.utils.Constants.GOALS_NAME_BLANK_WARNING
import com.example.roundupapp.utils.Constants.GOALS_NO_SAVINGS_GOALS_TO_DELETE
import com.example.roundupapp.utils.Constants.GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO
import com.example.roundupapp.utils.Constants.GOALS_WAIT_FOR_TRANSFER
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ERROR_LOADING_INITIAL_DATA
import com.example.roundupapp.utils.Constants.HOME_SCREEN_NETWORK_ERROR_LOADING_INITIAL_DATA
import com.example.roundupapp.utils.Constants.HOME_SCREEN_NO_ROUND_UP_AVAILABLE
import com.example.roundupapp.utils.Constants.REPO_ACCOUNT_UID_MISSING
import com.example.roundupapp.utils.Constants.REPO_FETCHING_GOAL_NOT_FOUND
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
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

  private var pendingTransferUid: String? = null

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
        amountMinorUnits = intent.amountMinorUnits,
        currency = intent.currency
      )
      is Intent.DeleteSavingsGoal -> deleteSavingsGoal()
      is Intent.TransferToSavingsGoal -> transferToSavingsGoal()
      is Intent.Refresh -> loadAccountDetails(isInitialLoad = false)
      is Intent.DismissError -> dismissError()
    }
  }

  // ================================================================================
  // Private Methods
  // ================================================================================
  private fun loadAccountDetails(isInitialLoad: Boolean) {
    viewModelScope.launch {
      _state.update {
        it.copy(
          loadingState = if (isInitialLoad) LoadingState.InitialLoading else LoadingState.Refreshing,
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
            error = null
          )
        } else {
          val error = result.exceptionOrNull()
          it.copy(
            loadingState = LoadingState.Idle,
            error = when (error) {
              is IOException -> UiError.NetworkError(HOME_SCREEN_NETWORK_ERROR_LOADING_INITIAL_DATA)
              else -> UiError.DataError(HOME_SCREEN_ERROR_LOADING_INITIAL_DATA)
            }
          )
        }
      }
    }
  }

  private fun createSavingsGoal(name: String, amountMinorUnits: Int, currency: String) {
    // Validation
    if (_state.value.loadingState is LoadingState.InProgress) return

    if (name.isBlank()) {
      _state.update { it.copy(error = UiError.ValidationError(GOALS_NAME_BLANK_WARNING)) }
      return
    }

    if (amountMinorUnits <= 0) {
      _state.update { it.copy(error = UiError.ValidationError(GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO)) }
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
        amountMinorUnits = amountMinorUnits,
        currency = currency
      )

      if (result.isSuccess) {
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        loadAccountDetails(isInitialLoad = false)
      } else {
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.OperationError(
              GOALS_CREATING_FAILURE,
              LoadingState.Operation.CREATING_GOAL
            )
          )
        }
      }
    }
  }

  private fun deleteSavingsGoal() {
    // Validation
    if (_state.value.loadingState is LoadingState.InProgress) return

    val accountUid = _state.value.accountUid
    val savingsGoal = _state.value.savingsGoals.firstOrNull()

    if (savingsGoal == null || accountUid.isBlank()) {
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

      val result = deleteSavingsGoalUseCase(accountUid, savingsGoal.savingsGoalUid)

      if (result.isSuccess) {
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        loadAccountDetails(isInitialLoad = false)
      } else {
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.OperationError(
              GOALS_FAILURE_DELETING,
              LoadingState.Operation.DELETING_GOAL
            )
          )
        }
      }
    }
  }

  private fun transferToSavingsGoal() {
    // Validation
    if (_state.value.isLoading) {
      _state.update {
        it.copy(
          error = UiError.OperationError(
            GOALS_WAIT_FOR_TRANSFER,
            LoadingState.Operation.TRANSFERRING
          )
        )
      }
      return
    }

    val accountUid = _state.value.accountUid
    if (accountUid.isBlank()) {
      _state.update {
        it.copy(error = UiError.ValidationError(REPO_ACCOUNT_UID_MISSING))
      }
      return
    }

    val goalUid = _state.value.savingsGoals.firstOrNull()?.savingsGoalUid
    if (goalUid.isNullOrBlank()) {
      _state.update {
        it.copy(error = UiError.DataError(REPO_FETCHING_GOAL_NOT_FOUND))
      }
      return
    }

    val amount = _state.value.roundedAmount
    if (amount <= 0) {
      _state.update {
        it.copy(error = UiError.ValidationError(HOME_SCREEN_NO_ROUND_UP_AVAILABLE))
      }
      return
    }

    viewModelScope.launch {
      val transferUid = pendingTransferUid ?: UUID.randomUUID().toString().also {
        pendingTransferUid = it
      }

      _state.update {
        it.copy(
          loadingState = LoadingState.InProgress(LoadingState.Operation.TRANSFERRING),
          error = null
        )
      }

      val result = transferToSavingsGoalUseCase(
        accountUid = accountUid,
        savingsGoalUid = goalUid,
        amountMinorUnits = amount,
        transferUid = transferUid
      )

      if (result.isSuccess) {
        pendingTransferUid = null
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        loadAccountDetails(isInitialLoad = false)
      } else {
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.OperationError(
              ALERT_TRANSFER_FAILED,
              LoadingState.Operation.TRANSFERRING
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
