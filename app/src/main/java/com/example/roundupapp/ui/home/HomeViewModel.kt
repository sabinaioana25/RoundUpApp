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
import com.example.roundupapp.utils.Constants.GOALS_ERROR_CREATING
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
 * ViewModel for the home screen managing account details, savings goals, and round-up transfers
 * Handles user intents and updates UI state based on repository data
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
    load(initial = true)
  }

  // ================================================================================
  // Public API - Intent Processing
  // ================================================================================
  fun processIntent(intent: Intent) {
    when (intent) {
      is Intent.CreateSavingsGoal -> handleCreateSavingsGoal(intent.name, intent.amountMinorUnits, intent.currency)
      is Intent.DeleteSavingsGoal -> handleDeleteSavingsGoal()
      is Intent.TransferToSavingsGoal -> handleTransferToSavingsGoal()
      is Intent.Refresh -> load(initial = false)
      is Intent.DismissError -> handleDismissError()
    }
  }


  // ================================================================================
  // Initial Load
  // ================================================================================
  private fun load(initial: Boolean) {
    viewModelScope.launch {
      _state.update {
        it.copy(
          loadingState = if (initial) LoadingState.InitialLoading else LoadingState.Refreshing,
          error = null
        )
      }

      val result = accountDetailsUseCase()

      if (result.isSuccess) {
        val details = result.getOrThrow()
        val firstAccount = details.accounts.firstOrNull()
        val transactions = details.transactions
        val roundUp = calculateRoundUpUseCase(transactions)

        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            accounts = details.accounts,
            balance = details.balance,
            transactions = transactions,
            savingsGoals = details.savingsGoals,
            roundedAmount = roundUp,
            accountUid = firstAccount?.accountUid.orEmpty(),
            defaultCategory = firstAccount?.defaultCategory.orEmpty(),
            error = null
          )
        }
      } else {
        val error = result.exceptionOrNull()
        _state.update {
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


  // ================================================================================
  // Intent Handlers
  // ================================================================================
  private fun handleCreateSavingsGoal(name: String, targetAmount: Int, currency: String) {
    if (_state.value.loadingState is LoadingState.InProgress) return

    if (name.isBlank()) {
      _state.update { it.copy(error = UiError.ValidationError(GOALS_NAME_BLANK_WARNING)) }
      return
    }

    if (targetAmount <= 0) {
      _state.update { it.copy(error = UiError.ValidationError(GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO)) }
      return
    }

    val accountUid = _state.value.accountUid
    if (accountUid.isBlank()) {
      _state.update { it.copy(error = UiError.DataError(GOALS_ERROR_CREATING)) }
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
        amountMinorUnits = targetAmount,
        currency = currency
      )


      if (result.isSuccess) {
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        load(initial = false)
      } else {
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.OperationError(GOALS_CREATING_FAILURE, LoadingState.Operation.CREATING_GOAL)
          )
        }
      }
    }
  }

  private fun handleDeleteSavingsGoal() {
    if (_state.value.loadingState is LoadingState.InProgress) return

    val accountUid = _state.value.accountUid
    val savingsGoal = _state.value.savingsGoals.firstOrNull()

    if (savingsGoal == null || accountUid.isBlank()) {
      _state.update {
        it.copy(
          error = UiError.ValidationError(GOALS_NO_SAVINGS_GOALS_TO_DELETE),
        )
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
        load(initial = false)
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

  private fun handleTransferToSavingsGoal() {
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
      _state.update { it.copy(error = UiError.DataError(REPO_FETCHING_GOAL_NOT_FOUND)) }
      return
    }

    val amount = _state.value.roundedAmount
    if (amount <= 0) {
      _state.update { it.copy(error = UiError.ValidationError(HOME_SCREEN_NO_ROUND_UP_AVAILABLE)) }
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
        roundUpAmount = amount,
        transferUid = transferUid
      )

      if (result.isSuccess) {
        _state.update { it.copy(loadingState = LoadingState.Idle) }
        load(initial = false)
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

  private fun handleDismissError() {
    _state.update { it.copy(error = null) }
  }
}
