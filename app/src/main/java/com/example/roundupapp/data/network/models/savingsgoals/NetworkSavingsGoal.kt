package com.example.roundupapp.data.network.models.savingsgoals

import com.example.roundupapp.data.network.models.transactions.NetworkAmount

data class NetworkSavingsGoal(
  val savingsGoalUid: String?,
  val name: String?,
  val targetAmount: NetworkAmount?,
  val createdAt: String?,
  val totalSaved: NetworkAmount?,
  val state: String?
)
