package com.example.roundupapp.domain.models

data class AccountDetails(
  val accounts: List<DomainAccount>,
  val transactions: List<DomainTransaction>,
  val savingsGoals: List<DomainSavingsGoal>,
  val balance: String,
  val roundUpAmount: Int = 0
)
