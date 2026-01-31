package com.example.roundupapp.domain.models.transaction

import com.example.roundupapp.data.network.models.feed.NetworkTransactionsWrapper
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class DomainTransaction(
  val amount: DomainAmount,
  val direction: String,
  val transactionTime: String,
  val counterPartyName: String
)

fun NetworkTransactionsWrapper.toListOfDomainTransactions(): List<DomainTransaction> {
  return feedItems.map { transaction ->
    val zonedDateTime = ZonedDateTime.parse(transaction.transactionTime)
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val formattedDate = zonedDateTime.format(formatter)

    DomainTransaction(
      amount = transaction.amount.toDomainAmount(),
      direction = transaction.direction,
      transactionTime = formattedDate,
      counterPartyName = transaction.counterPartyName
    )
  }
}
