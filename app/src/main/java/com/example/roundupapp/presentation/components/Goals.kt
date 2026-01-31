package com.example.roundupapp.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.roundupapp.presentation.home.ScreenState
import com.example.roundupapp.presentation.home.Intent

@Composable
fun Goals(
  modifier: Modifier = Modifier,
  state: ScreenState,
  onIntent: (Intent) -> Unit
) {

  var goalName by remember { mutableStateOf("") }
  var targetAmount by remember { mutableStateOf("") }

  TextField(value = goalName, onValueChange = {
    goalName = it
  })

  TextField(value = targetAmount, onValueChange = {
    targetAmount = it
  })

  Button(onClick = {
    if (state.accounts.isNotEmpty()) {
      val amountDecimal = targetAmount.toDoubleOrNull()
      val amountMinorUnits = amountDecimal?.let { (it * 100).toInt() }
      if (goalName.isNotBlank() && amountMinorUnits != null) {
        onIntent(Intent.CreateSavingsGoal(goalName, amountMinorUnits, "GBP"))
      } else {
        Log.d("HomeScreen", "Invalid input")
      }
    }
  }) {
    Text("Create Goal")
  }

  state.savingsGoals.forEach { savingsGoal ->
    Column(
      modifier = Modifier
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(savingsGoal.name)

      Button(onClick = {
        onIntent(Intent.DeleteSavingsGoal(savingsGoal.savingsGoalUid))
      }) {
        Text("Delete Goal")
      }
    }
  }
}
