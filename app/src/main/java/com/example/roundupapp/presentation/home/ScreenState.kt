package com.example.roundupapp.presentation.home

data class ScreenState(
  val isLoading: Boolean = false,
  val tasks: List<String> = emptyList(),
  val error: String? = "",
)
