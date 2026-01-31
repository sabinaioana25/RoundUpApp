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

@HiltViewModel
class MainViewModel @Inject constructor(
  private val repository: RoundUpRepository
) : ViewModel() {

  private val _testState = MutableStateFlow(ScreenState())
  val testState: StateFlow<ScreenState> = _testState.asStateFlow()

  fun processIntent(intent: TaskIntent) {
    when (intent) {
      is TaskIntent.LoadTasks -> ""
      is TaskIntent.AddTask -> getAccounts()
      is TaskIntent.CreateSavingsGoal -> createSavingsGoal(intent.name, intent.amountMinorUnits)
      is TaskIntent.DeleteSavingsGoal -> deleteSavingsGoal(intent.savingsGoalUid)
      is TaskIntent.CompleteTask -> ""
    }
  }

  fun getAccounts() = viewModelScope.launch {
    val accounts = repository.getAccounts()
    if (accounts.isNotEmpty()) {
      val transactions = repository.getTransactions(
        accounts[0].accountUid,
        accounts[0].defaultCategory
      )
      val savingsGoals = repository.getSavingsGoals(accounts[0].accountUid)
      _testState.value = _testState.value.copy(
        accounts = accounts,
        transactions = transactions,
        savingsGoals = savingsGoals
      )
    } else {
      _testState.value = _testState.value.copy(error = "No accounts found")
    }
  }

  fun createSavingsGoal(name: String, targetAmount: Int) = viewModelScope.launch {
    val accounts = _testState.value.accounts
    if (accounts.isNotEmpty()) {
      val newGoal = repository.createSavingsGoal(
        accounts[0].accountUid,
        name = name,
        amountMinorUnits = targetAmount,
        currency = "GBP"
      )
      if (newGoal != null) {
        val updatedGoals = _testState.value.savingsGoals + newGoal
        _testState.value = _testState.value.copy(savingsGoals = updatedGoals)
      }
    }
  }

  fun deleteSavingsGoal(savingsGoalUid: String) = viewModelScope.launch {
    val accounts = _testState.value.accounts
    if (accounts.isEmpty()) return@launch

    val wasDeleted = repository.deleteSavingsGoal(
      accountUid = accounts[0].accountUid,
      savingsGoalUid = savingsGoalUid
    )

    if (wasDeleted) {
      val updatedGoals = _testState.value.savingsGoals -
        _testState.value.savingsGoals.first {
        it.savingsGoalUid == savingsGoalUid
      }

      _testState.value = _testState.value.copy(
        savingsGoals = updatedGoals
      )
    }
  }
}
