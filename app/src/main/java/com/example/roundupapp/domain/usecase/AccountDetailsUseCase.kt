package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.repository.RoundUpRepository
import javax.inject.Inject

/**
 * Loads account details from cache and refreshes from network
 * Used to initialize and update account data in the application
 */
class AccountDetailsUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke() {
    repository.loadFromCache()
    repository.refreshFromNetwork()
  }
}
