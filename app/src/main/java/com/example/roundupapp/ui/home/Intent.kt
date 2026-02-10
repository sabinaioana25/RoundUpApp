package com.example.roundupapp.ui.home

/**
 * User intents for the home screen
 */
sealed interface Intent {
  data class CreateSavingsGoal(
    val name: String,
    val amountMinorUnits: Int,
    val currency: String
  ) : Intent
  
  data object DeleteSavingsGoal : Intent
  data object TransferToSavingsGoal : Intent
  data object Refresh : Intent
  data object DismissError : Intent
}
