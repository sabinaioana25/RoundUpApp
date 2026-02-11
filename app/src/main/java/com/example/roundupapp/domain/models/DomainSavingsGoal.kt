package com.example.roundupapp.domain.models

import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalResponse
import com.example.roundupapp.data.network.dto.savingsgoals.NetworkSavingsGoal
import com.example.roundupapp.data.network.dto.savingsgoals.NetworkSavingsGoalsWrapper

data class DomainSavingsGoal(
  val savingsGoalUid: String,
  val name: String,
  val targetAmount: DomainAmount,
  val totalSaved: DomainAmount,
  val state: String
)
fun NetworkSavingsGoalsWrapper.toListOfDomainSavingsGoals(): List<DomainSavingsGoal> {
  return savingsGoalList?.filterNotNull()?.map {
    DomainSavingsGoal(
      savingsGoalUid = it.savingsGoalUid ?: "",
      name = it.name ?: "",
      targetAmount = it.target.toDomainAmount(),
      totalSaved = it.totalSaved.toDomainAmount(),
      state = it.state ?: ""
    )
  } ?: emptyList()
}
