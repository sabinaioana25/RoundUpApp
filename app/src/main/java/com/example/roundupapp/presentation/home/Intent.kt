package com.example.roundupapp.presentation.home

sealed class Intent {
  data class CreateSavingsGoal(
    val name: String,
    val amountMinorUnits: Int,
    val currency: String
  ) : Intent()
  object DeleteSavingsGoal : Intent()
  object TransferToSavingsGoal : Intent()
}
