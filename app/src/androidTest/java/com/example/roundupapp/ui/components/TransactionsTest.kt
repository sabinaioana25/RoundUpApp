package com.example.roundupapp.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants
import org.junit.Rule
import org.junit.Test

class TransactionsTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun displaysTransactions_whenProvided() {
    val transactions = listOf(
      DomainTransaction(
        amount = DomainAmount("GBP", 236, "£2.36"),
        direction = "OUT",
        transactionTime = "2026-01-31",
        counterPartyName = "Coffee Shop"
      ),
      DomainTransaction(
        amount = DomainAmount("GBP", 1500, "£15.00"),
        direction = "IN",
        transactionTime = "2026-01-31",
        counterPartyName = "Salary"
      )
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Transactions(transactions = transactions)
      }
    }

    composeTestRule.onNodeWithText("Coffee Shop").assertIsDisplayed()
    composeTestRule.onNodeWithText("Salary").assertIsDisplayed()
  }

  @Test
  fun showsDateHeader_forTransactionGroups() {
    val transactions = listOf(
      DomainTransaction(
        amount = DomainAmount("GBP", 236, "£2.36"),
        direction = "OUT",
        transactionTime = java.time.LocalDate.now().toString(),
        counterPartyName = "Coffee Shop"
      )
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Transactions(transactions = transactions)
      }
    }

    composeTestRule.onNodeWithText(Constants.TRANSACTIONS_DATE_FORMAT_TODAY).assertIsDisplayed()
  }

  @Test
  fun showsYesterdayLabel_forYesterdayTransactions() {
    val yesterday = java.time.LocalDate.now().minusDays(1).toString()
    val transactions = listOf(
      DomainTransaction(
        amount = DomainAmount("GBP", 236, "£2.36"),
        direction = "OUT",
        transactionTime = yesterday,
        counterPartyName = "Coffee Shop"
      )
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Transactions(transactions = transactions)
      }
    }

    composeTestRule.onNodeWithText(Constants.TRANSACTIONS_DATE_FORMAT_YESTERDAY).assertIsDisplayed()
  }

  @Test
  fun showsPlusSign_forIncomingTransactions() {
    val transactions = listOf(
      DomainTransaction(
        amount = DomainAmount("GBP", 1500, "£15.00"),
        direction = "IN",
        transactionTime = "2026-01-31",
        counterPartyName = "Salary"
      )
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Transactions(transactions = transactions)
      }
    }

    composeTestRule.onNodeWithText("+15.00").assertIsDisplayed()
  }

  @Test
  fun showsMinusSign_forOutgoingTransactions() {
    val transactions = listOf(
      DomainTransaction(
        amount = DomainAmount("GBP", 236, "£2.36"),
        direction = "OUT",
        transactionTime = "2026-01-31",
        counterPartyName = "Coffee Shop"
      )
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Transactions(transactions = transactions)
      }
    }

    composeTestRule.onNodeWithText("-2.36").assertIsDisplayed()
  }

  @Test
  fun groupsTransactionsByDate() {
    val transactions = listOf(
      DomainTransaction(
        amount = DomainAmount("GBP", 236, "£2.36"),
        direction = "OUT",
        transactionTime = "2026-01-31",
        counterPartyName = "Coffee Shop"
      ),
      DomainTransaction(
        amount = DomainAmount("GBP", 500, "£5.00"),
        direction = "OUT",
        transactionTime = "2026-01-31",
        counterPartyName = "Lunch"
      ),
      DomainTransaction(
        amount = DomainAmount("GBP", 1000, "£10.00"),
        direction = "OUT",
        transactionTime = "2026-01-30",
        counterPartyName = "Groceries"
      )
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Transactions(transactions = transactions)
      }
    }

    // All three transactions should be visible
    composeTestRule.onNodeWithText("Coffee Shop").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lunch").assertIsDisplayed()
    composeTestRule.onNodeWithText("Groceries").assertIsDisplayed()
  }

  @Test
  fun displaysEmptyList_whenNoTransactions() {
    composeTestRule.setContent {
      RoundUpAppTheme {
        Transactions(transactions = emptyList())
      }
    }

    // Should not crash and card should still be visible
    composeTestRule.onRoot().assertIsDisplayed()
  }
}
