package com.example.roundupapp.presentation.home

import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.balance.DomainBalance
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.transaction.DomainTransaction

data class ScreenState(
  val isLoading: Boolean = false,
  val tasks: List<String> = emptyList(),
  val accounts: List<DomainAccount> = emptyList(),
  val balance: List<DomainBalance> = emptyList(),
  val transactions: List<DomainTransaction> = emptyList(),
  val savingsGoals: List<DomainSavingsGoal> = emptyList(),
  val savingGoal: DomainSavingsGoal? = null,
  val deleteGoal: Boolean = false,
  val roundUpAmount: Int = 0,
  val transferToSavingsGoal: Boolean = false,
  val error: String? = "",
)
