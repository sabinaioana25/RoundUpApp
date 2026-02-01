package com.example.roundupapp.data.network.dto.savingsgoals

import com.example.roundupapp.data.network.dto.transactions.NetworkAmount

data class NetworkSavingsGoal(
  val savingsGoalUid: String?,
  val name: String?,
  val target: NetworkAmount,
  val totalSaved: NetworkAmount,
  val state: String?
)
