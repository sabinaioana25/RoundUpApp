package com.example.roundupapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roundupapp.domain.repository.RoundUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
  private val repository: RoundUpRepository
) : ViewModel() {

  private val _testState = MutableStateFlow(ScreenState())
  val testState: StateFlow<ScreenState> = _testState.asStateFlow()

  fun processIntent(intent: TaskIntent) {
    when (intent) {
      is TaskIntent.LoadTasks -> ""
      is TaskIntent.AddTask -> getAccounts()
      is TaskIntent.CompleteTask -> ""
    }
  }

  fun getAccounts() = viewModelScope.launch {
    val accounts = repository.getAccounts()
    Log.i("HomeViewModel", accounts.toString())
  }
}
