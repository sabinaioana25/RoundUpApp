package com.example.roundupapp.presentation.home

sealed class TaskIntent {
  object LoadTasks : TaskIntent()
  data class AddTask(val task: String) : TaskIntent()
  data class CreateSavingsGoal(
    val name: String,
    val amountMinorUnits: Int,
    val currency: String
  ) : TaskIntent()
  data class DeleteSavingsGoal(val savingsGoalUid: String) : TaskIntent()
  data class CompleteTask(val id: String) : TaskIntent()
}
