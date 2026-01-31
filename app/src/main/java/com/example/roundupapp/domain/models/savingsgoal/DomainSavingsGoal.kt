package com.example.roundupapp.domain.models.savingsgoal

import com.example.roundupapp.data.network.models.savingsgoals.NetworkCreateSavingsGoalResponse
import com.example.roundupapp.data.network.models.savingsgoals.NetworkSavingsGoal
import com.example.roundupapp.data.network.models.savingsgoals.NetworkSavingsGoalsWrapper
import com.example.roundupapp.domain.models.transaction.DomainAmount
import com.example.roundupapp.domain.models.transaction.toDomainAmount

data class DomainSavingsGoal(
  val savingsGoalUid: String,
  val name: String,
  val targetAmount: DomainAmount,
  val createdAt: String,
  val totalSaved: DomainAmount,
  val state: String
)
fun NetworkSavingsGoalsWrapper.toListOfDomainSavingsGoals(): List<DomainSavingsGoal> {
  return savingsGoalList?.filterNotNull()?.map {
    DomainSavingsGoal(
      savingsGoalUid = it.savingsGoalUid ?: "",
      name = it.name ?: "",
      targetAmount = it.targetAmount.toDomainAmount(),
      createdAt = it.createdAt ?: "",
      totalSaved = it.totalSaved.toDomainAmount(),
      state = it.state ?: ""
    )
  } ?: emptyList()
}

fun NetworkCreateSavingsGoalResponse.toDomainSavingsGoal(originalGoal: NetworkSavingsGoal): DomainSavingsGoal {
    return DomainSavingsGoal(
        savingsGoalUid = this.savingsGoalUid,
        name = originalGoal.name ?: "",
        targetAmount = originalGoal.targetAmount.toDomainAmount(),
        createdAt = originalGoal.createdAt ?: "",
        totalSaved = originalGoal.totalSaved.toDomainAmount(),
        state = originalGoal.state ?: ""
    )
}
