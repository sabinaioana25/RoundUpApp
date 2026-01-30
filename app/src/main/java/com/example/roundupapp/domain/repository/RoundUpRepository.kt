package com.example.roundupapp.domain.repository

import com.example.roundupapp.domain.models.account.DomainAccount

interface RoundUpRepository {
  suspend fun getAccounts(): List<DomainAccount>
}
