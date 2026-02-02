package com.example.roundupapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.roundupapp.ui.components.Goals
import com.example.roundupapp.ui.components.Transactions
import com.example.roundupapp.ui.components.aListOfDomainSavingGoals
import com.example.roundupapp.ui.components.aListOfTransactions
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants

/**
 * Main home screen displaying account balance, savings goals, and transaction history
 * Uses a hoisted pattern to separate state management from UI composition
 */
@Composable
fun HomeScreenHoist(
  viewModel: HomeViewModel,
  modifier: Modifier = Modifier,
) {
  // lifecycle-aware collection
  val state by viewModel.state.collectAsState()
  val onIntent: (Intent) -> Unit = viewModel::processIntent
  HomeScreen(
    state = state,
    onIntent = onIntent
  )
}

/**
 * Displays the user's balance, savings goals, and recent transactions
 */
@Composable
fun HomeScreen(
  state: ScreenState,
  onIntent: (Intent) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Balance",
          style = MaterialTheme.typography.labelMedium
        )
        Text(
          text = state.balance,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Goals(
      state = state,
      onIntent = onIntent
    )

    Transactions(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      transactions = state.transactions
    )
  }
}

@PreviewLightDark
@Composable
fun HomeScreenHoistPreview() {
  RoundUpAppTheme {
    HomeScreen(
      state = aScreenState,
      onIntent = {}
    )
  }
}

val aScreenState = ScreenState(
  isLoading = false,
  tasks = listOf(Constants.HOME_SCREEN_EMPTY_STATE_PREVIEW_TASK),
  error = null,
  transactions = aListOfTransactions,
  balance = "£1000",
  savingsGoals = aListOfDomainSavingGoals,
  roundedAmount = 44552
)
