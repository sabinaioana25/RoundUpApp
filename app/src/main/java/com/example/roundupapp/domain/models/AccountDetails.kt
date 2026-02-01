package com.example.roundupapp.domain.models

import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.transaction.DomainTransaction

data class AccountDetails(
  val accounts: List<DomainAccount>,
  val transactions: List<DomainTransaction>,
  val savingsGoals: List<DomainSavingsGoal>,
  val balance: String,
  val roundUpAmount: Int = 0
)
