package com.example.roundupapp.presentation.home

data class ScreenState(
  val tasks: List<String> = emptyList(),
  val error: String? = "",
  val isLoading: Boolean = false,
)
