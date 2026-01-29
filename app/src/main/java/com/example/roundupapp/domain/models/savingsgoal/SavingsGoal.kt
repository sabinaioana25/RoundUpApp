package com.example.roundupapp.domain.models.savingsgoal

data class SavingsGoal(
  val goalUid: String,
  val name: String,
  val targetAmount: Amount,
  val savedPercentage: Int,
  val state: String
)
