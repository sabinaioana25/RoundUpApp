package com.example.roundupapp.domain.models

import com.example.roundupapp.data.network.dto.balance.NetworkBalance

data class DomainBalance(
  val effectiveBalance: DomainAmount,
)

fun NetworkBalance.toDomainBalance(): DomainBalance {
  return DomainBalance(
    effectiveBalance = effectiveBalance.toDomainAmount(),
  )
}
