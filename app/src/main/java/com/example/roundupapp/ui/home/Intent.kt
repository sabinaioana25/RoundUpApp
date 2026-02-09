package com.example.roundupapp.ui.home

/**
 * A sealed representation of user actions forwarded to
 * the ViewModel for handling
 */
sealed interface Intent {
  data class CreateSavingsGoal(
    val name: String,
    val amountMinorUnits: Int,
    val currency: String = "GBP"
  ) : Intent

  data object DeleteSavingsGoal : Intent
  data object TransferToSavingsGoal : Intent
  data object Refresh : Intent
  data object DismissError : Intent
}
