package com.example.roundupapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants.GOALS_CARD_COMPOSABLE_TEXT_TRANSACTIONS

@Composable
fun Transactions(
  modifier: Modifier = Modifier,
  transactions: List<DomainTransaction>
) {
  val groups: Map<String, List<DomainTransaction>> = transactions
    .groupBy { it.transactionTime }
    .toSortedMap(compareByDescending { it })

  Card(
    modifier = modifier,
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {

    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      textAlign = TextAlign.Center,
      text = GOALS_CARD_COMPOSABLE_TEXT_TRANSACTIONS,
      style = MaterialTheme.typography.labelMedium
    )

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      groups.forEach { (date, itemsForDate) ->
        stickyHeader {
          Text(
            text = formatDate(date),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(12.dp)
          )
        }

        items(items = itemsForDate) { transaction ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = transaction.counterPartyName)
            Text(
              text = (if (transaction.direction == "IN") "+" else "") + transaction.amount.gbpUnits,
              color = if (transaction.direction == "IN") Color(0xFF059669) else MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

private fun formatDate(dateString: String): String {
  val today = java.time.LocalDate.now()
  val yesterday = today.minusDays(1)
  return when (val date = java.time.LocalDate.parse(dateString)) {
    today -> "Today"
    yesterday -> "Yesterday"
    else -> {
      val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
      date.format(formatter)
    }
  }
}

@PreviewLightDark
@Composable
fun TransactionsPreview() {
  RoundUpAppTheme {
    Transactions(
      modifier = Modifier.padding(16.dp),
      transactions = aListOfTransactions
    )
  }
}

val aListOfTransactions = listOf(
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 236, gbpUnits = "£2.36"),
    direction = "OUT",
    transactionTime = "2026-01-31",
    counterPartyName = "John Doe"
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 811, gbpUnits = "£8.11"),
    direction = "IN",
    transactionTime = "2026-01-31",
    counterPartyName = "Gary Doe"
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 744, gbpUnits = "£7.44"),
    direction = "OUT",
    transactionTime = "2023-06-01",
    counterPartyName = "Jamie Oliver"
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 912, gbpUnits = "£9.12"),
    direction = "OUT",
    transactionTime = "2023-06-01",
    counterPartyName = "John Oliver"
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 234, gbpUnits = "£2.34"),
    direction = "OUT",
    transactionTime = "2023-06-01",
    counterPartyName = "John Bishop"
  ),
)
