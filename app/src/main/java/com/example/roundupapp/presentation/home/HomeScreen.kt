package com.example.roundupapp.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreenHoist(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.testState.collectAsState()
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
