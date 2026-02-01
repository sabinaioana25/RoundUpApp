package com.example.roundupapp.ui.home

import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.transaction.DomainTransaction

data class ScreenState(
  val isLoading: Boolean = false,
  val tasks: List<String> = emptyList(),
  val accounts: List<DomainAccount> = emptyList(),
  val balance: String = "",
  val transactions: List<DomainTransaction> = emptyList(),
  val savingsGoals: List<DomainSavingsGoal> = emptyList(),
  val savingsGoal: DomainSavingsGoal? = null,
  val deleteGoal: Boolean = false,
  val roundedAmount: Int = 0,
  val transferToSavingsGoal: Boolean = false,
  val error: String? = "",
  val accountUid: String = "",
  val defaultCategory: String = "",
)
