package com.example.roundupapp.domain.models

import com.example.roundupapp.data.network.dto.account.NetworkAccountsWrapper

data class DomainAccount(
  val accountUid: String,
  val defaultCategory: String,
  val createdAt: String,
  val name: String,
)

fun NetworkAccountsWrapper.toListOfDomainAccounts(): List<DomainAccount> {
  return accounts.map {
    DomainAccount(
      accountUid = it.accountUid,
      defaultCategory = it.defaultCategory,
      createdAt = it.createdAt,
      name = it.name,
    )
  }
}
