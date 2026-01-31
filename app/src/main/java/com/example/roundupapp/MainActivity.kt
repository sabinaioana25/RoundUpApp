package com.example.roundupapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.roundupapp.presentation.home.HomeScreenHoist
import com.example.roundupapp.presentation.home.MainViewModel
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RoundUpAppTheme {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
          color = MaterialTheme.colorScheme.background
        ) {
          Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeScreenHoist(
              modifier = Modifier.padding(innerPadding),
              viewModel = hiltViewModel<MainViewModel>()
            )
          }
        }
      }
    }
  }
}
