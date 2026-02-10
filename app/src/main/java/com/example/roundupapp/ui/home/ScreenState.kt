package com.example.roundupapp.ui.home

import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction

sealed interface LoadingState {
  data object Idle : LoadingState
  data object InitialLoading : LoadingState
  data object Refreshing : LoadingState
  data class InProgress(val operation: Operation) : LoadingState
  data class Error(val error: UiError) : LoadingState


  enum class Operation {
    CREATING_GOAL,
    DELETING_GOAL,
    TRANSFERRING
  }
}

/**
 * UI state model for the home screen containing account data, transactions, and savings goals.
 * Note: Proper Loading, Error, and Success states were not implemented due to time constraints.
 */
data class ScreenState(
  val loadingState: LoadingState = LoadingState.InitialLoading,
  val accounts: List<DomainAccount> = emptyList(),
  val balance: String = "",
  val transactions: List<DomainTransaction> = emptyList(),
  val savingsGoals: List<DomainSavingsGoal> = emptyList(),
  val roundedAmount: Int = 0,
  val error: UiError? = null,
  val accountUid: String = "",
  val defaultCategory: String = ""
) {
  val isLoading: Boolean
    get() = loadingState != LoadingState.Idle

  val isInitialLoading: Boolean
    get() = loadingState == LoadingState.InitialLoading

  val isRefreshing: Boolean
    get() = loadingState == LoadingState.Refreshing

  val hasData: Boolean
    get() = accounts.isNotEmpty()
}

/**
 * shows what types of errors can occur
 */
sealed interface UiError {
  val message: String

  data class NetworkError(override val message: String) : UiError
  data class ValidationError(override val message: String) : UiError
  data class OperationError(override val message: String, val operation: LoadingState.Operation) : UiError
  data class DataError(override val message: String) : UiError
}
