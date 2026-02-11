package com.example.roundupapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.sp
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.Dimens.Image.thumbnailSmall
import com.example.roundupapp.ui.theme.IncomeGreen
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants

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
    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation.default),
    shape = RoundedCornerShape(Dimens.Corner.card)
  ) {
    LazyColumn(modifier = Modifier.padding(Dimens.Spacing.default)) {
      groups.forEach { (date, itemsForDate) ->
        stickyHeader {
          Text(
            text = formatDate(date),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface)
              .padding(vertical = Dimens.Spacing.default, horizontal = Dimens.Spacing.small)
          )
        }
        items(items = itemsForDate) { transaction ->
          TransactionItem(transaction = transaction)
        }
      }
    }
  }
}

@Composable
private fun TransactionItem(transaction: DomainTransaction) {
  val isIncoming = transaction.direction == "IN"
  
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = Dimens.Spacing.small),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      // Transaction Icon
      Box(
        modifier = Modifier
          .size(thumbnailSmall)
          .clip(CircleShape)
          .background(
            if (isIncoming) 
              MaterialTheme.colorScheme.primaryContainer 
            else 
              MaterialTheme.colorScheme.surfaceVariant
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (isIncoming) "↓" else "↑",
          fontSize = Dimens.Spacing.large.value.toInt().sp,
          color = if (isIncoming) 
            MaterialTheme.colorScheme.primary 
          else 
            MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      
      Spacer(modifier = Modifier.width(Dimens.Spacing.medium))
      
      Column {
        Text(
          text = transaction.counterPartyName,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onBackground
        )
      }
    }
    
    // Amount
    Text(
      text = (if (isIncoming) "+" else "-") + transaction.amount.gbpUnits.removePrefix("£"),
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.SemiBold,
      color = if (isIncoming) IncomeGreen else MaterialTheme.colorScheme.onBackground
    )
  }
}

private fun formatDate(dateString: String): String {
  val today = java.time.LocalDate.now()
  val yesterday = today.minusDays(1)
  return when (val date = java.time.LocalDate.parse(dateString)) {
    today -> Constants.TRANSACTIONS_DATE_FORMAT_TODAY
    yesterday -> Constants.TRANSACTIONS_DATE_FORMAT_YESTERDAY
    else -> {
      val formatter = java.time.format.DateTimeFormatter.ofPattern(Constants.TRANSACTION_DATE_FORMAT_DD_MM_YYYY)
      date.format(formatter)
    }
  }
}

@PreviewLightDark
@Composable
fun TransactionsPreview() {
  RoundUpAppTheme {
    Surface {
      Transactions(
        modifier = Modifier.padding(Dimens.Spacing.default),
        transactions = aListOfTransactions
      )
    }
  }
}

val aListOfTransactions = listOf(
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 236, gbpUnits = Constants.PreviewData.TRANSACTION_1_AMOUNT),
    direction = Constants.PreviewData.TRANSACTION_1_DIRECTION,
    transactionTime = Constants.PreviewData.TRANSACTION_1_TIME,
    counterPartyName = Constants.PreviewData.TRANSACTION_1_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 811, gbpUnits = Constants.PreviewData.TRANSACTION_2_AMOUNT),
    direction = Constants.PreviewData.TRANSACTION_2_DIRECTION,
    transactionTime = Constants.PreviewData.TRANSACTION_2_TIME,
    counterPartyName = Constants.PreviewData.TRANSACTION_2_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 744, gbpUnits = Constants.PreviewData.TRANSACTION_3_AMOUNT),
    direction = Constants.PreviewData.TRANSACTION_3_DIRECTION,
    transactionTime = Constants.PreviewData.TRANSACTION_3_TIME,
    counterPartyName = Constants.PreviewData.TRANSACTION_3_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 912, gbpUnits = Constants.PreviewData.TRANSACTION_4_AMOUNT),
    direction = Constants.PreviewData.TRANSACTION_4_DIRECTION,
    transactionTime = Constants.PreviewData.TRANSACTION_4_TIME,
    counterPartyName = Constants.PreviewData.TRANSACTION_4_NAME
  ),
  DomainTransaction(
    amount = DomainAmount(currency = "GBP", minorUnits = 234, gbpUnits = Constants.PreviewData.TRANSACTION_5_AMOUNT),
    direction = Constants.PreviewData.TRANSACTION_5_DIRECTION,
    transactionTime = Constants.PreviewData.TRANSACTION_5_TIME,
    counterPartyName = Constants.PreviewData.TRANSACTION_5_NAME
  ),
)
