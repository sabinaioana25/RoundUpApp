package com.example.roundupapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.ui.home.Intent
import com.example.roundupapp.ui.home.ScreenState
import com.example.roundupapp.ui.theme.RoundUpAppTheme
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
        onIntent(Intent.CreateSavingsGoal(name, amount, "GBP"))
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

  state.savingsGoals.firstOrNull()?.let { savingsGoal ->
    Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {

        Text(
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          text = "Goal",
          style = MaterialTheme.typography.labelMedium
        )

        Text(
          text = savingsGoal.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Total saved: ${savingsGoal.totalSaved.minorUnits.toGbp()}",
          style = MaterialTheme.typography.bodyMedium
        )

        Text(
          text = "Target: ${savingsGoal.targetAmount.minorUnits.toGbp()}",
          style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (savingsGoal.totalSaved.minorUnits == 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Round-up available",
                style = MaterialTheme.typography.bodySmall
              )
              Text(
                text = state.roundedAmount.toGbp(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
            }
            Button(onClick = {
              onIntent(Intent.TransferToSavingsGoal)
            }) {
              Text("Transfer")
            }
          }
        }

        OutlinedButton(
          onClick = {
            onIntent(Intent.DeleteSavingsGoal)
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Delete Goal")
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun GoalsEmptyPreview() {
  Goals(
    state = ScreenState(
      isLoading = false,
      tasks = emptyList(),
      error = null,
      transactions = emptyList(),
      balance = "£1000",
      savingsGoals = emptyList(),
      roundedAmount = 0
    ),
    onIntent = {}
  )
}

@PreviewLightDark
@Composable
fun GoalsWithGoalPreview() {
  RoundUpAppTheme {
    Goals(
      state = ScreenState(
        isLoading = false,
        tasks = emptyList(),
        error = null,
        transactions = emptyList(),
        balance = "£1000",
        savingsGoals = aListOfDomainSavingGoals,
        roundedAmount = 2450
      ),
      onIntent = {}
    )
  }
}

val aListOfDomainSavingGoals = listOf(
  DomainSavingsGoal(
    name = "Holiday Fund",
    targetAmount = DomainAmount("GBP", 50000, "£500.00"),
    savingsGoalUid = "",
    totalSaved = DomainAmount("GBP", 50000, "£500.00"),
    state = ""
  )
)
