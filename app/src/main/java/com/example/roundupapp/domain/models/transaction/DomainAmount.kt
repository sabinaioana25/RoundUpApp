package com.example.roundupapp.domain.models.transaction

import com.example.roundupapp.data.network.models.transactions.NetworkAmount

data class DomainAmount(
  val minorUnits: Int,
)

fun NetworkAmount?.toDomainAmount(): DomainAmount {
  return DomainAmount(
    minorUnits = this?.minorUnits ?: 0,
  )
}
