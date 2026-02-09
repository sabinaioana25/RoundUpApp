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
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants.GOALS_CARD_COMPOSABLE_TEXT_TRANSACTIONS
import com.example.roundupapp.utils.Constants.PreviewData
import com.example.roundupapp.utils.Constants.TRANSACTIONS_DATE_FORMAT_TODAY
import com.example.roundupapp.utils.Constants.TRANSACTIONS_DATE_FORMAT_YESTERDAY
import com.example.roundupapp.utils.Constants.TRANSACTION_DATE_FORMAT_DD_MM_YYYY

/**
 * Transaction list component displaying transactions grouped by date
 * Shows incoming transactions in green and outgoing in default color
 * Dates are formatted as "Today", "Yesterday", or "MMM dd, yyyy"
 */
@Composable
fun Transactions(
  modifier: Modifier = Modifier,
  transactions: List<DomainTransaction>
) {

  // group transactions by date and sort in descending order
  val groups: Map<String, List<DomainTransaction>> = transactions
    .groupBy { it.transactionTime }
    .toSortedMap(compareByDescending { it })

  Card(
    modifier = modifier,
    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation.default)
  ) {

    Text(
      modifier = Modifier
          .fillMaxWidth()
          .padding(Dimens.Spacing.default),
      textAlign = TextAlign.Center,
      text = GOALS_CARD_COMPOSABLE_TEXT_TRANSACTIONS,
      style = MaterialTheme.typography.labelMedium
    )

    LazyColumn(modifier = Modifier.padding(horizontal = Dimens.Spacing.default, vertical = Dimens.Spacing.small)) {
      groups.forEach { (date, itemsForDate) ->
        // sticky date header for each group
        stickyHeader {
          Text(
            text = formatDate(date),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(Dimens.Spacing.medium)
          )
        }

        items(items = itemsForDate) { transaction ->
          Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.Spacing.small),
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

/**
 * Formats date string to friendly labels: "Today", "Yesterday", or "MMM dd, yyyy"
 */
private fun formatDate(dateString: String): String {
  val today = java.time.LocalDate.now()
  val yesterday = today.minusDays(1)
  return when (val date = java.time.LocalDate.parse(dateString)) {
    today -> TRANSACTIONS_DATE_FORMAT_TODAY
    yesterday -> TRANSACTIONS_DATE_FORMAT_YESTERDAY
    else -> {
      val formatter = java.time.format.DateTimeFormatter.ofPattern(TRANSACTION_DATE_FORMAT_DD_MM_YYYY)
      date.format(formatter)
    }
  }
}

@PreviewLightDark
@Composable
fun TransactionsPreview() {
  RoundUpAppTheme {
    Transactions(
      modifier = Modifier.padding(Dimens.Spacing.default),
      transactions = aListOfTransactions
    )
  }
}

val aListOfTransactions = listOf(
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 236, gbpUnits = PreviewData.TRANSACTION_1_AMOUNT),
    direction = PreviewData.TRANSACTION_1_DIRECTION,
    transactionTime = PreviewData.TRANSACTION_1_TIME,
    counterPartyName = PreviewData.TRANSACTION_1_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 811, gbpUnits = PreviewData.TRANSACTION_2_AMOUNT),
    direction = PreviewData.TRANSACTION_2_DIRECTION,
    transactionTime = PreviewData.TRANSACTION_2_TIME,
    counterPartyName = PreviewData.TRANSACTION_2_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 744, gbpUnits = PreviewData.TRANSACTION_3_AMOUNT),
    direction = PreviewData.TRANSACTION_3_DIRECTION,
    transactionTime = PreviewData.TRANSACTION_3_TIME,
    counterPartyName = PreviewData.TRANSACTION_3_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 912, gbpUnits = PreviewData.TRANSACTION_4_AMOUNT),
    direction = PreviewData.TRANSACTION_4_DIRECTION,
    transactionTime = PreviewData.TRANSACTION_4_TIME,
    counterPartyName = PreviewData.TRANSACTION_4_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 234, gbpUnits = PreviewData.TRANSACTION_5_AMOUNT),
    direction = PreviewData.TRANSACTION_5_DIRECTION,
    transactionTime = PreviewData.TRANSACTION_5_TIME,
    counterPartyName = PreviewData.TRANSACTION_5_NAME
  ),
)
