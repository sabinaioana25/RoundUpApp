package com.example.roundupapp.domain.models.transaction

import com.example.roundupapp.data.network.models.feed.NetworkAmount

data class DomainAmount(
  val amount: Int,
)

fun NetworkAmount?.toDomainAmount(): DomainAmount {
  return DomainAmount(
    amount = this?.minorUnits ?: 0,
  )
}
