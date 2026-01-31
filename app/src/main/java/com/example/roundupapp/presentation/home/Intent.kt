package com.example.roundupapp.presentation.home

sealed class Intent {
  data class CreateSavingsGoal(
    val name: String,
    val amountMinorUnits: Int,
    val currency: String
  ) : Intent()
  data class DeleteSavingsGoal(val savingsGoalUid: String) : Intent()
  data class TransferToSavingsGoal(val savingsGoalUid: String) : Intent()
}
