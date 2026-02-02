package com.example.roundupapp.ui.home

import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction

/**
 * UI state model for the home screen containing account data, transactions, and savings goals.
 * Note: Proper Loading, Error, and Success states were not implemented due to time constraints.
 */
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
  val remainingAmount: String = ""
)
