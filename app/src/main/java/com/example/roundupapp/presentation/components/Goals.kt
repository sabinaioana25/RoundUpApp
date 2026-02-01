package com.example.roundupapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.roundupapp.presentation.home.Intent
import com.example.roundupapp.presentation.home.ScreenState
import com.example.roundupapp.utils.toGbp

@Composable
fun Goals(
  modifier: Modifier = Modifier,
  state: ScreenState,
  onIntent: (Intent) -> Unit
) {

  var showCreateGoalDialog by rememberSaveable { mutableStateOf(false) }

  if (state.savingsGoals.isEmpty()) {
    Button(onClick = { showCreateGoalDialog = true }) {
      Text("Create Goal")
    }
  }

  if (showCreateGoalDialog) {
    CreateGoalDialog(
      onConfirm = { name, amount ->
        onIntent(Intent.CreateSavingsGoal(name, amount, "GPB"))
        showCreateGoalDialog = false
      },
      onDismiss = { showCreateGoalDialog = false }
    )
  }

  CreatedGoal(state, onIntent)
}

@Composable
private fun CreatedGoal(
  state: ScreenState,
  onIntent: (Intent) -> Unit
) {
  var isTransferComplete by rememberSaveable { mutableStateOf(false) }

  state.savingsGoals.firstOrNull()?.let { savingsGoal ->
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row {
        Text(savingsGoal.name)
        Text(savingsGoal.targetAmount.minorUnits.toGbp())
      }

      if (!isTransferComplete) {
        Text("available round up value ${state.roundedAmount.toGbp()}")
        Button(onClick = {
          onIntent(Intent.TransferToSavingsGoal)
          isTransferComplete = true
        }) {
          Text("Transfer now!")
        }
      }

      Button(onClick = {
        onIntent(Intent.DeleteSavingsGoal)
        isTransferComplete = false
      }) {
        Text("Delete Goal")
      }
    }
  }
}
