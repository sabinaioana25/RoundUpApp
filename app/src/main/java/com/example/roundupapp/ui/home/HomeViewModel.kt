package com.example.roundupapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.usecase.AccountDetailsUseCase
import com.example.roundupapp.domain.usecase.CalculateRoundUpUseCase
import com.example.roundupapp.domain.usecase.CreateSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.DeleteSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.TransferToSavingsGoalUseCase
import com.example.roundupapp.utils.Constants.ALERT_TRANSFERRING_ERROR
import com.example.roundupapp.utils.Constants.ALERT_TRANSFERRING_FAILURE
import com.example.roundupapp.utils.Constants.ALERT_TRANSFER_FAILED
import com.example.roundupapp.utils.Constants.ALERT_WAIT_FOR_ACTION_TO_COMPLETE
import com.example.roundupapp.utils.Constants.GOALS_ERROR_CREATING
import com.example.roundupapp.utils.Constants.GOALS_ERROR_DELETING
import com.example.roundupapp.utils.Constants.GOALS_FAILURE_CREATING
import com.example.roundupapp.utils.Constants.GOALS_FAILURE_DELETING
import com.example.roundupapp.utils.Constants.GOALS_NAME_BLANK_WARNING
import com.example.roundupapp.utils.Constants.GOALS_PLEASE_WAIT_DELETE_ACTION
import com.example.roundupapp.utils.Constants.GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ALREADY_LOADING
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ERROR_EMPTY_STATE
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ERROR_LOADING_INITIAL_DATA
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ERROR_REFRESHING
import com.example.roundupapp.utils.Constants.HOME_SCREEN_NO_ROUND_UP_AVAILABLE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen managing account details, savings goals, and round-up transfers
 * Handles user intents and updates UI state based on repository data
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
  private val repository: RoundUpRepository,
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
    observeAccountDetails()
    loadInitialData()
  }

  private fun observeAccountDetails() {
    repository.accountDetails
      .onEach { accountDetails ->
        if (accountDetails != null) {
          _state.update {
            it.copy(
              accounts = accountDetails.accounts,
              transactions = accountDetails.transactions,
              savingsGoals = accountDetails.savingsGoals,
              balance = accountDetails.balance,
              accountUid = accountDetails.accounts.firstOrNull()?.accountUid ?: "",
              defaultCategory = accountDetails.accounts.firstOrNull()?.defaultCategory ?: "",
              roundedAmount = accountDetails.roundUpAmount,
              loadingState = if (it.isInitialLoading) LoadingState.Idle else it.loadingState,
              error = null
            )
          }
        } else if (!_state.value.isInitialLoading) {
          _state.update {
            it.copy(
              error = UiError.DataError(HOME_SCREEN_ERROR_EMPTY_STATE),
              loadingState = LoadingState.Idle
            )
          }
        }
      }
      .launchIn(viewModelScope)
  }

  private fun loadInitialData() {
    viewModelScope.launch {
      try {
        _state.update { it.copy(loadingState = LoadingState.InitialLoading, error = null) }
        accountDetailsUseCase()
        calculateRoundUpUseCase()
      } catch (e: Exception) {
        Log.e(TAG, HOME_SCREEN_ERROR_LOADING_INITIAL_DATA, e)
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.NetworkError(HOME_SCREEN_ERROR_LOADING_INITIAL_DATA)
          )
        }
      }
    }
  }

  fun processIntent(intent: Intent) {
    when (intent) {
      is Intent.CreateSavingsGoal -> createSavingsGoal(intent.name, intent.amountMinorUnits)
      is Intent.DeleteSavingsGoal -> deleteSavingsGoal()
      is Intent.TransferToSavingsGoal -> transferToSavingsGoal()
      is Intent.Refresh -> refresh()
      is Intent.DismissError -> dismissError()
    }
  }

  private fun refresh() {
    if (_state.value.isLoading) {
      Log.i(TAG, HOME_SCREEN_ALREADY_LOADING)
      return
    }

    viewModelScope.launch {
      try {
        _state.update { it.copy(loadingState = LoadingState.Refreshing, error = null) }
        accountDetailsUseCase()
        calculateRoundUpUseCase()
        _state.update { it.copy(loadingState = LoadingState.Idle) }
      } catch (e: Exception) {
        Log.e(TAG, HOME_SCREEN_ERROR_REFRESHING, e)
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.NetworkError(HOME_SCREEN_ERROR_REFRESHING)
          )
        }
      }
    }
  }

  private fun createSavingsGoal(name: String, targetAmount: Int) {
    if (_state.value.isLoading) {
      _state.update {
        it.copy(
          error = UiError.OperationError(
            ALERT_WAIT_FOR_ACTION_TO_COMPLETE,
            LoadingState.Operation.CREATING_GOAL
          )
        )
      }
      return
    }

    viewModelScope.launch {
      try {
        // validation
        if (name.isBlank()) {
          _state.update { it.copy(error = UiError.ValidationError(GOALS_NAME_BLANK_WARNING)) }
          return@launch
        }

        if (targetAmount <= 0) {
          _state.update { it.copy(error = UiError.ValidationError(GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO)) }
          return@launch
        }

        _state.update {
          it.copy(
            loadingState = LoadingState.InProgress(LoadingState.Operation.CREATING_GOAL),
            error = null
          )
        }

        createSavingsGoalUseCase(name, targetAmount)

        _state.update { it.copy(loadingState = LoadingState.Idle) }
      } catch (e: Exception) {
        Log.e(TAG, GOALS_ERROR_CREATING, e)
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.OperationError(
              GOALS_FAILURE_CREATING,
              LoadingState.Operation.CREATING_GOAL
            )
          )
        }
      }
    }
  }

  private fun deleteSavingsGoal() {
    if (_state.value.isLoading) {
      _state.update {
        it.copy(
          error = UiError.OperationError(
            GOALS_PLEASE_WAIT_DELETE_ACTION,
            LoadingState.Operation.DELETING_GOAL
          )
        )
      }
      return
    }
    viewModelScope.launch {
      try {
        _state.update {
          it.copy(
            loadingState = LoadingState.InProgress(LoadingState.Operation.DELETING_GOAL),
            error = null
          )
        }
        deleteSavingsGoalUseCase()
        _state.update { it.copy(loadingState = LoadingState.Idle) }
      } catch (e: Exception) {
        Log.e(TAG, GOALS_ERROR_DELETING, e)
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
    if (_state.value.isLoading) {
      _state.update {
        it.copy(
          error = UiError.OperationError(
            ALERT_WAIT_FOR_ACTION_TO_COMPLETE,
            LoadingState.Operation.TRANSFERRING
          )
        )
      }
      return
    }

    if (_state.value.roundedAmount <= 0) {
      _state.update { it.copy(error = UiError.ValidationError(HOME_SCREEN_NO_ROUND_UP_AVAILABLE)) }
      return
    }

    viewModelScope.launch {
      try {
        _state.update {
          it.copy(
            loadingState = LoadingState.InProgress(LoadingState.Operation.TRANSFERRING),
            error = null
          )
        }

        val success = transferToSavingsGoalUseCase()

        if (success) {
          accountDetailsUseCase()
          calculateRoundUpUseCase()
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
          return@launch
        }

        _state.update { it.copy(loadingState = LoadingState.Idle) }
      } catch (e: Exception) {
        Log.e(TAG, ALERT_TRANSFERRING_ERROR, e)
        _state.update {
          it.copy(
            loadingState = LoadingState.Idle,
            error = UiError.OperationError(
              ALERT_TRANSFERRING_FAILURE,
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
