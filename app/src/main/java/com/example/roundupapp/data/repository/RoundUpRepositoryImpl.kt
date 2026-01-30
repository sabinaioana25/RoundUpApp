package com.example.roundupapp.data.repository

import android.util.Log
import com.example.roundupapp.BuildConfig
import com.example.roundupapp.data.network.RoundUpApi
import com.example.roundupapp.data.network.models.NetworkAccountsWrapper
import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.account.toListOfDomainAccounts
import com.example.roundupapp.domain.repository.RoundUpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoundUpRepositoryImpl : RoundUpRepository {
  override suspend fun getAccounts(): List<DomainAccount> = withContext(Dispatchers.IO) {
    try {
      RoundUpApi.retrofitService.getAccounts(BuildConfig.API_KEY)
    } catch (e: Exception) {
      Log.e("RoundUpRepository", e.message.toString())
      NetworkAccountsWrapper(accounts = emptyList())
    }.toListOfDomainAccounts()
  }
}
