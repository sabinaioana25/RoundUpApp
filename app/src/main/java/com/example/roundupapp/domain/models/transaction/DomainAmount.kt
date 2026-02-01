package com.example.roundupapp.domain.models.transaction

import com.example.roundupapp.data.network.models.transactions.NetworkAmount
import com.example.roundupapp.utils.toGbp

data class DomainAmount(
  val currency: String,
  val minorUnits: Int,
  val gbpUnits: String
)

fun NetworkAmount?.toDomainAmount(): DomainAmount {
  return DomainAmount(
    currency = this?.currency ?: "GBP",
    minorUnits = this?.minorUnits ?: 0,
    gbpUnits = this?.minorUnits.toGbp()
  )
}
