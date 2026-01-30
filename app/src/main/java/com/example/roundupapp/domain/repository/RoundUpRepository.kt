package com.example.roundupapp.domain.repository

import com.example.roundupapp.domain.models.account.Account

interface RoundUpRepository {
  suspend fun getAccounts(): List<Account>
}
