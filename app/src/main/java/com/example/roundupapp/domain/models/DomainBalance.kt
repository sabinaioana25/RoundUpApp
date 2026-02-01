package com.example.roundupapp.domain.models

import com.example.roundupapp.data.network.dto.balance.NetworkBalance

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
