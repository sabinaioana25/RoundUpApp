package com.example.roundupapp.domain.models.balance

import com.example.roundupapp.data.network.models.balance.NetworkBalanceWrapper
import com.example.roundupapp.domain.models.transaction.DomainAmount
import com.example.roundupapp.domain.models.transaction.toDomainAmount

data class DomainBalance(
  val clearedBalance: DomainAmount,
  val effectiveBalance: DomainAmount
)

fun NetworkBalanceWrapper.toListOfDomainBalances(): List<DomainBalance> {
  return balanceList?.map { balance ->
    DomainBalance(
      clearedBalance = balance.clearedBalance.toDomainAmount(),
      effectiveBalance = balance.effectiveBalance.toDomainAmount()
    )
  } ?: emptyList()
}
