package com.example.roundupapp.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

  @get:Rule
  val composeTestRule =  createAndroidComposeRule<ComponentActivity>()

  private fun emptyState(balance: String = "£0.00"): ScreenState = ScreenState(
    isLoading = false,
    tasks = emptyList(),
    error = null,
    transactions = emptyList(),
    balance = balance,
    savingsGoals = emptyList(),
    roundedAmount = 0
  )

  private fun stateWithGoalAndTransactions(): ScreenState = ScreenState(
    isLoading = false,
    tasks = emptyList(),
    error = null,
    transactions = listOf(
      DomainTransaction(
        amount = DomainAmount("GBP", 500, "£5.00"),
        direction = "OUT",
        transactionTime = "2026-01-31",
        counterPartyName = "Coffee Shop"
      )
    ),
    balance = "£1,234.56",
    savingsGoals = listOf(
      DomainSavingsGoal(
        name = "Vacation",
        targetAmount = DomainAmount("GBP", 100000, "£1,000.00"),
        savingsGoalUid = "uid-1",
        totalSaved = DomainAmount("GBP", 25000, "£250.00"),
        state = "OPEN"
      )
    ),
    roundedAmount = 1350
  )

  @Test
  fun balance_card_displays_balance_from_state() {
    composeTestRule.setContent {
      HomeScreen(state = emptyState("£9,876.54"), onIntent = {})
    }

    composeTestRule
      .onNodeWithText("Balance")
      .assertExists()

    composeTestRule
      .onNodeWithText("£9,876.54")
      .assertExists()
  }

  @Test
  fun balance_card_displays_zero_balance() {
    composeTestRule.setContent {
      HomeScreen(state = emptyState("£0.00"), onIntent = {})
    }

    composeTestRule
      .onNodeWithText("£0.00")
      .assertExists()
  }

  @Test
  fun shows_create_goal_button_when_no_goals_exist() {
    composeTestRule.setContent {
      HomeScreen(state = emptyState(), onIntent = {})
    }

    composeTestRule
      .onNodeWithText("Create Goal")
      .assertExists()
  }

  @Test
  fun shows_goal_card_when_goals_exist() {
    composeTestRule.setContent {
      HomeScreen(state = stateWithGoalAndTransactions(), onIntent = {})
    }

    composeTestRule
      .onNodeWithText("Vacation")
      .assertExists()

    composeTestRule
      .onNodeWithText("Create Goal")
      .assertDoesNotExist()
  }

  @Test
  fun clicking_transfer_fires_intent() {
    val intents = mutableListOf<Intent>()

    composeTestRule.setContent {
      HomeScreen(state = stateWithGoalAndTransactions(), onIntent = { intents.add(it) })
    }

    composeTestRule
      .onNodeWithText("Transfer")
      .performClick()

    assert(intents.any { it is Intent.TransferToSavingsGoal })
  }

  @Test
  fun clicking_delete_goal_fires_intent() {
    val intents = mutableListOf<Intent>()

    composeTestRule.setContent {
      HomeScreen(state = stateWithGoalAndTransactions(), onIntent = { intents.add(it) })
    }

    composeTestRule
      .onNodeWithText("Delete Goal")
      .performClick()

    assert(intents.any { it is Intent.DeleteSavingsGoal })
  }
}
