package com.example.roundupapp.domain.models.balance

import com.example.roundupapp.data.network.models.balance.NetworkBalance
import com.example.roundupapp.domain.models.transaction.DomainAmount
import com.example.roundupapp.domain.models.transaction.toDomainAmount

data class DomainBalance(
  val clearedBalance: DomainAmount,
  val effectiveBalance: DomainAmount,
  val amount: DomainAmount
)

fun NetworkBalance.toDomainBalance(): DomainBalance {
  return DomainBalance(
    clearedBalance = clearedBalance.toDomainAmount(),
    effectiveBalance = effectiveBalance.toDomainAmount(),
    amount = amount.toDomainAmount()
  )
}
