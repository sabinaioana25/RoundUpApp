package com.example.roundupapp.ui.home

/**
 * A sealed representation of user actions forwarded to
 * the ViewModel for handling
 */
sealed class Intent {
  data class CreateSavingsGoal(
    val name: String,
    val amountMinorUnits: Int,
    val currency: String
  ) : Intent()
  object DeleteSavingsGoal : Intent()
  object TransferToSavingsGoal : Intent()
}
