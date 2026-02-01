package com.example.roundupapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.usecase.AccountDetailsUseCase
import com.example.roundupapp.domain.usecase.CalculateRoundUpUseCase
import com.example.roundupapp.domain.usecase.CreateSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.DeleteSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.TransferToSavingsGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val repository: RoundUpRepository,
  private val accountDetailsUseCase: AccountDetailsUseCase,
  private val createSavingsGoalUseCase: CreateSavingsGoalUseCase,
  private val deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase,
  private val transferToSavingsGoalUseCase: TransferToSavingsGoalUseCase,
  private val calculateRoundUpUseCase: CalculateRoundUpUseCase
) : ViewModel() {

  private val _state = MutableStateFlow(ScreenState())
  val state: StateFlow<ScreenState> = _state.asStateFlow()

  init {
    loadData()
  }

  private fun loadData() {
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
              roundedAmount = accountDetails.roundUpAmount,
              error = null
            )
          }
        } else {
          _state.update { it.copy(error = "No data found") }
        }
      }
      .launchIn(viewModelScope)

    viewModelScope.launch {
      accountDetailsUseCase()
      calculateRoundUpUseCase()
    }
  }

  fun processIntent(intent: Intent) {
    when (intent) {
      is Intent.CreateSavingsGoal -> createSavingsGoal(intent.name, intent.amountMinorUnits)
      is Intent.DeleteSavingsGoal -> deleteSavingsGoal()
      is Intent.TransferToSavingsGoal -> transferToSavingsGoal()
    }
  }

  fun createSavingsGoal(name: String, targetAmount: Int) = viewModelScope.launch {
    if (name.isBlank()) {
      _state.update { it.copy(error = "Name cannot be blank") }
      return@launch
    }

    if (targetAmount == 0) {
      _state.update { it.copy(error = "Amount cannot be blank") }
      return@launch
    }

    _state.update { it.copy(error = null) }

    createSavingsGoalUseCase(name, targetAmount)
  }

  fun deleteSavingsGoal() = viewModelScope.launch {
    deleteSavingsGoalUseCase()
  }

  fun transferToSavingsGoal() = viewModelScope.launch {
    transferToSavingsGoalUseCase()
  }
}
