package com.example.roundupapp.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.roundupapp.presentation.components.Transactions

@Composable
fun HomeScreenHoist(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsState()
  val onIntent: (TaskIntent) -> Unit = viewModel::processIntent
  HomeScreen(
    state = state,
    onIntent = onIntent
  )
}

@Composable
fun HomeScreen(
  state: ScreenState,
  onIntent: (TaskIntent) -> Unit,
  modifier: Modifier = Modifier
) {

  var goalName by remember { mutableStateOf("") }
  var targetAmount by remember { mutableStateOf("") }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .padding(48.dp),
    color = MaterialTheme.colorScheme.background
  )
  {
    Column(
      modifier = Modifier
        .fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    )
    {
      state.error?.let { errorMessage ->
        Log.e("HomeScreen", errorMessage)
        onIntent(TaskIntent.LoadTasks)
      }
      Button(onClick = { onIntent(TaskIntent.AddTask("text")) }) {
        Text("Click me")
      }
      state.accounts.firstOrNull()?.name?.let { Text(it) }
      state.transactions.firstOrNull()?.direction?.let { Text(it) }
      state.transactions.firstOrNull()?.transactionTime?.let { Text(it) }
      state.savingsGoals.firstOrNull()?.name?.let { Text(it) }

      Transactions(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        transactions = state.transactions
      )

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
            onIntent(TaskIntent.CreateSavingsGoal(goalName, amountMinorUnits, "GBP"))
          } else {
            onIntent(TaskIntent.LoadTasks)
          }
        }
      }) {
        Text("Create Goal")
      }

      Button(
        onClick = {
        onIntent(TaskIntent.TransferToSavingsGoal(state.savingsGoals.first().savingsGoalUid))
      }) {
        Text("Transfer")
      }

      state.savingsGoals.forEach { savingsGoal ->
        Column(
          modifier = Modifier
            .fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(savingsGoal.name)

          Button(onClick = {
            onIntent(TaskIntent.DeleteSavingsGoal(savingsGoal.savingsGoalUid))
          }) {
            Text("Delete Goal")
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun HomeScreenHoistPreview() {
  HomeScreen(
    state = aScreenState,
    onIntent = {}
  )
}

val aScreenState = ScreenState(
  isLoading = false,
  tasks = listOf("text1234"),
  error = null
)
