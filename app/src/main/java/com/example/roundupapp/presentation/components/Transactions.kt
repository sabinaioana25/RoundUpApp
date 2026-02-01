package com.example.roundupapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.roundupapp.domain.models.transaction.DomainAmount
import com.example.roundupapp.domain.models.transaction.DomainTransaction
import com.example.roundupapp.ui.theme.RoundUpAppTheme

@Composable
fun Transactions(
  modifier: Modifier = Modifier,
  transactions: List<DomainTransaction>
) {
  val groups: Map<String, List<DomainTransaction>> = transactions
    .groupBy { it.transactionTime }
    .toSortedMap(compareByDescending { it })

  LazyColumn(modifier = modifier) {
    groups.forEach { (date, itemsForDate) ->
      stickyHeader {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
        ) {
          Text(text = date)
        }
        HorizontalDivider()
      }
      items(items = itemsForDate) { transaction ->
        Row(
          modifier = modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = transaction.counterPartyName)
          Text(
            text = (if (transaction.direction == "IN") "+" else "") + transaction.amount.gbpUnits,
            color = if (transaction.direction == "IN") Color.Blue else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun TransactionsPreview() {
  RoundUpAppTheme {
    Transactions(
      transactions = aListOfTransactions
    )
  }
}

val aListOfTransactions = listOf(
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 100, gbpUnits = "£1.00"),
    direction = "OUT",
    transactionTime = "2026-01-31",
    counterPartyName = "John Doe"
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 100, gbpUnits = "£1.00"),
    direction = "IN",
    transactionTime = "2026-01-31",
    counterPartyName = "Gary Doe"
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 100, gbpUnits = "£1.00"),
    direction = "OUT",
    transactionTime = "2023-06-01",
    counterPartyName = "Jamie Oliver"
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 100, gbpUnits = "£1.00"),
    direction = "OUT",
    transactionTime = "2023-06-01",
    counterPartyName = "John Oliver"
  ),
)
