package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

class AccountDetailsUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke() {
    repository.loadFromCache()
    repository.refresh()
  }
}
