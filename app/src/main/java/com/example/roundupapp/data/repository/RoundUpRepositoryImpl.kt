package com.example.roundupapp.data.repository

import com.example.roundupapp.BuildConfig
import com.example.roundupapp.data.network.RoundUpApi
import com.example.roundupapp.domain.models.account.Account
import com.example.roundupapp.domain.repository.RoundUpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoundUpRepositoryImpl: RoundUpRepository {
  override suspend fun getAccounts(): List<Account> = withContext(Dispatchers.IO) {
    try {
      RoundUpApi.retrofitService.getAccounts(
        BuildConfig.API_KEY
      )
    } catch (e: Exception) {
      emptyList()
    }
  }
}
