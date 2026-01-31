package com.example.roundupapp.presentation.home

sealed class TaskIntent {
  data class GetAccounts(val task: String) : TaskIntent()
  data class CreateSavingsGoal(
    val name: String,
    val amountMinorUnits: Int,
    val currency: String
  ) : TaskIntent()
  data class DeleteSavingsGoal(val savingsGoalUid: String) : TaskIntent()
  data class TransferToSavingsGoal(val savingsGoalUid: String) : TaskIntent()
}
