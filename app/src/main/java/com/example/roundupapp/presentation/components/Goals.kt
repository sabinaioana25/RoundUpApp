package com.example.roundupapp.presentation.components

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.roundupapp.presentation.home.Intent
import com.example.roundupapp.presentation.home.ScreenState

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
        onIntent(Intent.CreateSavingsGoal(name, amount.toString(), "GPB"))
        showCreateGoalDialog = false
      },
      onDismiss = { showCreateGoalDialog = false }
    )
  }

  state.savingsGoals.firstOrNull()?.let { savingsGoal ->
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(savingsGoal.name)

      Button(onClick = {
        onIntent(Intent.DeleteSavingsGoal)
      }) {
        Text("Delete Goal")
      }
    }
  }
}
