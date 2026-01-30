package com.example.roundupapp.domain.models.account

import com.example.roundupapp.data.network.models.NetworkAccountsWrapper

data class DomainAccount(
  val name: String
)

fun NetworkAccountsWrapper.toListOfDomainAccounts(): List<DomainAccount> {
  return accounts.map {
    DomainAccount(
      name = it.name
    )
  }
}
