package com.example.roundupapp.presentation.home

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
  val savingGoal: DomainSavingsGoal? = null,
  val deleteGoal: Boolean = false,
  val roundedAmount: String = "",
  val transferToSavingsGoal: Boolean = false,
  val error: String? = "",
  val accountUid: String = "",
  val defaultCategory: String = "",
)
