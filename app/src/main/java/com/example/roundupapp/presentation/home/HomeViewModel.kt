package com.example.roundupapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {

  private val _testState = MutableStateFlow(ScreenState())
  val testState: StateFlow<ScreenState> = _testState.asStateFlow()

  fun processIntent(intent: TaskIntent) {
    when (intent) {
      is TaskIntent.LoadTasks -> ""
      is TaskIntent.AddTask -> updateName(intent.task)
      is TaskIntent.CompleteTask -> ""
    }
  }

  fun updateName(name: String) {
    viewModelScope.launch {
      try {
        Log.i("HomeViewModel", name)
      } catch (e: Exception) {
        _testState.update { it.copy(error = e.message) }
      }
    }
  }
}
