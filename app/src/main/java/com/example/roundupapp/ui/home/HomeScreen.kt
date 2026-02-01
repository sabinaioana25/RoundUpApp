package com.example.roundupapp.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.roundupapp.ui.components.Goals
import com.example.roundupapp.ui.components.Transactions
import com.example.roundupapp.ui.components.aListOfTransactions
import com.example.roundupapp.ui.theme.RoundUpAppTheme

@Composable
fun HomeScreenHoist(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsState()
  val onIntent: (Intent) -> Unit = viewModel::processIntent
  HomeScreen(
    state = state,
    onIntent = onIntent
  )
}

@Composable
fun HomeScreen(
  state: ScreenState,
  onIntent: (Intent) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = Modifier
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
  )
  {
    Text(state.balance)

    Transactions(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      transactions = state.transactions
    )

    Goals(
      state = state,
      onIntent = onIntent
    )
  }
}

@Preview
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
  tasks = listOf("text1234"),
  error = null,
  transactions = aListOfTransactions,
  balance = "£1000"
)
