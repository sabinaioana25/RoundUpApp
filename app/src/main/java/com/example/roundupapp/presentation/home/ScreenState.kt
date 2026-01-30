package com.example.roundupapp.presentation.home

import com.example.roundupapp.domain.models.account.DomainAccount

data class ScreenState(
  val isLoading: Boolean = false,
  val tasks: List<String> = emptyList(),
  val accounts: List<DomainAccount> = emptyList(),
  val error: String? = "",
)
