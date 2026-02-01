package com.example.roundupapp.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun homeScreen_displaysBalance() {
    val state = ScreenState(balance = "£1,234.56")
    composeTestRule.setContent {
      RoundUpAppTheme {
        HomeScreen(state = state, onIntent = {})
      }
    }
    composeTestRule.onNodeWithText("£1,234.56").assertIsDisplayed()
  }

  @Test
  fun homeScreen_showsCreateGoalButton_whenNoGoals() {
    val state = ScreenState(savingsGoals = emptyList())
    composeTestRule.setContent {
      RoundUpAppTheme {
        HomeScreen(state = state, onIntent = {})
      }
    }
    composeTestRule.onNodeWithText("Create Goal").assertIsDisplayed()
  }

  @Test
  fun homeScreen_clickingTransferNow_triggersIntent() {
    var intentSent: Intent? = null
    val goal = DomainSavingsGoal(
      savingsGoalUid = "123",
      name = "Trip to Paris",
      targetAmount = DomainAmount("GBP", 100000, "£1000.00"),
      totalSaved = DomainAmount("GBP", 5000, "£50.00"),
      state = "ACTIVE"
    )
    val state = ScreenState(savingsGoals = listOf(goal))
    composeTestRule.setContent {
      RoundUpAppTheme {
        HomeScreen(state = state, onIntent = { intentSent = it })
      }
    }

    composeTestRule.onNodeWithText("Transfer").performClick()
    assert(intentSent is Intent.TransferToSavingsGoal)
  }
}
