package com.example.roundupapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.repository.RoundUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@HiltViewModel
class MainViewModel @Inject constructor(
  private val repository: RoundUpRepository
) : ViewModel() {

  private val _state = MutableStateFlow(ScreenState())
  val state: StateFlow<ScreenState> = _state.asStateFlow()

  fun processIntent(intent: TaskIntent) {
    when (intent) {
      is TaskIntent.GetAccounts -> getAccounts()
      is TaskIntent.CreateSavingsGoal -> createSavingsGoal(intent.name, intent.amountMinorUnits)
      is TaskIntent.DeleteSavingsGoal -> deleteSavingsGoal(intent.savingsGoalUid)
      is TaskIntent.TransferToSavingsGoal -> transferToSavingsGoal(intent.savingsGoalUid)
    }
  }

  fun getAccounts() = viewModelScope.launch {
    val accounts = repository.getAccounts()
    if (accounts.isNotEmpty()) {
      val balance = repository.getBalanceList(
        accounts[0].accountUid,
      )
      _state.value = _state.value.copy(
        accounts = accounts,
      )
      val transactions = repository.getTransactions(
        accounts[0].accountUid,
        accounts[0].defaultCategory
      )
      val savingsGoals = repository.getSavingsGoals(accounts[0].accountUid)
      _state.value = _state.value.copy(
        accounts = accounts,
        transactions = transactions,
        savingsGoals = savingsGoals
      )
    } else {
      _state.value = _state.value.copy(error = "No accounts found")
    }
  }

  fun createSavingsGoal(name: String, targetAmount: Int) = viewModelScope.launch {
    val accounts = _state.value.accounts
    if (accounts.isNotEmpty()) {
      val newGoal = repository.createSavingsGoal(
        accounts[0].accountUid,
        name = name,
        amountMinorUnits = targetAmount,
        currency = "GBP"
      )
      if (newGoal != null) {
        val updatedGoals = _state.value.savingsGoals + newGoal
        _state.value = _state.value.copy(savingsGoals = updatedGoals)
      }
    }
  }

  fun deleteSavingsGoal(savingsGoalUid: String) = viewModelScope.launch {
    val accounts = _state.value.accounts
    if (accounts.isEmpty()) return@launch

    val wasDeleted = repository.deleteSavingsGoal(
      accountUid = accounts[0].accountUid,
      savingsGoalUid = savingsGoalUid
    )

    if (wasDeleted) {
      val updatedGoals = _state.value.savingsGoals -
        _state.value.savingsGoals.first {
          it.savingsGoalUid == savingsGoalUid
        }

      _state.value = _state.value.copy(
        savingsGoals = updatedGoals
      )
    }
  }

  fun transferToSavingsGoal(savingsGoalUid: String) = viewModelScope.launch {
    repository.transferToSavingsGoal(
      accountUid = _state.value.accounts[0].accountUid,
      savingsGoalUid = savingsGoalUid,
      transferUid = "aaaaa880-aaaa-4aaa-aaaa-aaaaaaaaaaaa")
  }

  suspend fun calculateSavings(roundedAmount: Int) {
    val accounts = _state.value.accounts
    val transactions = repository.getTransactions(
      accounts[0].accountUid,
      accounts[0].defaultCategory
    )

    val total = transactions
      .filter { it.direction == "OUT" }
      .sumOf { item ->
        val pence = item.amount.minorUnits
        val remainder = pence % 100
        if (remainder == 0) 0 else 100 - remainder
      }

    val displayAmount = BigDecimal(total)
      .divide(BigDecimal(100))
      .setScale(2, RoundingMode.CEILING)
  }
}
