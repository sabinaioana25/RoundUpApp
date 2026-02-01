package com.example.roundupapp.domain.models.transaction

import com.example.roundupapp.data.network.models.transactions.NetworkAmount

data class DomainAmount(
  val currency: String,
  val minorUnits: Int,
)

fun NetworkAmount?.toDomainAmount(): DomainAmount {
  return DomainAmount(
    currency = this?.currency ?: "GBP",
    minorUnits = this?.minorUnits ?: 0
  )
}
