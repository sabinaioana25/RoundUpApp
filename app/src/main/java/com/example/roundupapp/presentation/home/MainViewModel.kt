package com.example.roundupapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.usecase.LoadInitialDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.io.path.Path

@HiltViewModel
class MainViewModel @Inject constructor(
  private val repository: RoundUpRepository,
  private val loadInitialDataUseCase: LoadInitialDataUseCase
) : ViewModel() {

  private val _state = MutableStateFlow(ScreenState())
  val state: StateFlow<ScreenState> = _state.asStateFlow()

  init {
    loadData()
  }

  private fun loadData() = viewModelScope.launch {
    val initial = loadInitialDataUseCase()
    if (initial != null) {
      _state.value = _state.value.copy(
        accounts = initial.accounts,
        transactions = initial.transactions,
        savingsGoals = initial.savingsGoals,
        balance = initial.balance
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
      transferUid = "aaaaa880-aaaa-4aaa-aaaa-aaaaaaaaaaaa"
    )
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
