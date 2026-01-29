package com.example.roundupapp.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

  var text by remember { mutableStateOf("") }

  Surface(
    modifier = Modifier
      .fillMaxSize(), color = MaterialTheme.colorScheme.background
  )
  {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.Start
    )
    {
      Text(
        text = text,
        modifier = modifier
      )
      TextField(
        value = text,
        onValueChange = {
          text = it
        },
        label = {
          Text(text = "Goal Name")
        }
      )
    }
  }
}
