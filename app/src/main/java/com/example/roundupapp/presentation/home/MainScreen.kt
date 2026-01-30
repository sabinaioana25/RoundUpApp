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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  var text by remember { mutableStateOf("Just") }
  val sampleUiState by viewModel.testState.collectAsState()

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
      sampleUiState.error?.let { errorMessage ->
        Log.e("HomeScreen", errorMessage)
        viewModel.processIntent(TaskIntent.LoadTasks)
      }
      Button(onClick = { viewModel.processIntent(TaskIntent.AddTask(text)) }) {
        Text("Click me")
      }
    }
  }
}
