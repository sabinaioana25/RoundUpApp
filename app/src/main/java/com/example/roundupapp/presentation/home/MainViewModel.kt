package com.example.roundupapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.usecase.AccountDetailsUseCase
import com.example.roundupapp.domain.usecase.CalculateRoundUpUseCase
import com.example.roundupapp.domain.usecase.CreateSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.DeleteSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.TransferToSavingsGoalUseCase
import com.example.roundupapp.utils.toDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

  private fun loadData() = viewModelScope.launch {
    val initial = accountDetailsUseCase()
    if (initial != null && initial.accounts.isNotEmpty()) {
      val initialAccountUid = initial.accounts[0].accountUid

      _state.value = _state.value.copy(
        accounts = initial.accounts,
        transactions = initial.transactions,
        savingsGoals = initial.savingsGoals,
        balance = initial.balance,
        accountUid = initialAccountUid
      )
    } else {
      _state.value = _state.value.copy(error = "No data found")
    }
  }

  fun processIntent(intent: Intent) {
    when (intent) {
      is Intent.CreateSavingsGoal -> createSavingsGoal(intent.name, intent.amountMinorUnits)
      is Intent.DeleteSavingsGoal -> deleteSavingsGoal(intent.savingsGoalUid)
      is Intent.TransferToSavingsGoal -> transferToSavingsGoal(intent.savingsGoalUid)
    }
  }

  fun createSavingsGoal(name: String, targetAmount: String) = viewModelScope.launch {
    if (name.isBlank()) {
      _state.update { it.copy(error = "Name cannot be blank") }
    }

    if(targetAmount.isBlank()) {
      _state.update { it.copy(error = "Amount cannot be blank") }
    }

    _state.update { it.copy(error = null) }

    val accountUid = _state.value.accountUid

    if (accountUid.isBlank()) return@launch
    val newGoal = createSavingsGoalUseCase(
      accountUid,
      name,
      targetAmount
    )

    val updatedGoals =
      _state.value.savingsGoals + listOfNotNull(newGoal)
    _state.value = _state.value.copy(savingsGoals = updatedGoals)
  }

  fun deleteSavingsGoal(savingsGoalUid: String) = viewModelScope.launch {
    val accountUid = _state.value.accountUid

    val wasDeleted = deleteSavingsGoalUseCase(
      accountUid = accountUid,
      savingsGoalUid = savingsGoalUid
    )

    if (wasDeleted) {
      _state.update { current ->
        current.copy(
          savingsGoals = current.savingsGoals.filter { it.savingsGoalUid != savingsGoalUid }
        )
      }
    }
  }

  fun transferToSavingsGoal(savingsGoalUid: String) = viewModelScope.launch {
    val goal =
      _state.value.savingsGoals.find { it.savingsGoalUid == savingsGoalUid } ?: return@launch

    val amountMinorUnits = goal.targetAmount.minorUnits
    transferToSavingsGoalUseCase(
      accountUid = _state.value.accountUid,
      savingsGoalUid = savingsGoalUid,
      amountMinorUnits = amountMinorUnits
    )
    _state.update { it.copy(transferToSavingsGoal = true) }
  }

  fun calculateSavings(roundedAmount: Int) {
    val accountUid = _state.value.accountUid
    val defaultCategoryUid = _state.value.defaultCategory

    viewModelScope.launch {
      val total = calculateRoundUpUseCase(
        accountUid,
        defaultCategoryUid,
      )
      val roundedAmount = total.toDecimal()
      _state.update { it.copy(roundedAmount = roundedAmount.toPlainString()) }
    }
  }
}
