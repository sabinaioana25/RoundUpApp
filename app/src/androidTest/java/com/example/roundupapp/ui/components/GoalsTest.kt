package com.example.roundupapp.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.ui.home.Intent
import com.example.roundupapp.ui.home.LoadingState
import com.example.roundupapp.ui.home.ScreenState
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants
import org.junit.Rule
import org.junit.Test

class GoalsTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun showsCreateGoalButton_whenNoGoals() {
    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.Idle,
            savingsGoals = emptyList()
          ),
          onIntent = {}
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_NO_GOALS_YET).assertIsDisplayed()
    composeTestRule.onNodeWithText(Constants.GOALS_CREATE_FIRST_GOAL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun showsGoalDetails_whenGoalExists() {
    val goal = DomainSavingsGoal(
      name = "Holiday Fund",
      targetAmount = DomainAmount("GBP", 50000, "£500.00"),
      savingsGoalUid = "123",
      totalSaved = DomainAmount("GBP", 15000, "£150.00"),
      state = "ACTIVE"
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.Idle,
            savingsGoals = listOf(goal),
            roundedAmount = 2450
          ),
          onIntent = {}
        )
      }
    }

    composeTestRule.onNodeWithText("Holiday Fund").assertIsDisplayed()
    composeTestRule.onNodeWithText(Constants.GOALS_TARGET_PREFIX + "£500.00").assertIsDisplayed()
  }

  @Test
  fun showsProgressBar_whenGoalExists() {
    val goal = DomainSavingsGoal(
      name = "Holiday Fund",
      targetAmount = DomainAmount("GBP", 50000, "£500.00"),
      savingsGoalUid = "123",
      totalSaved = DomainAmount("GBP", 15000, "£150.00"),
      state = "ACTIVE"
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.Idle,
            savingsGoals = listOf(goal)
          ),
          onIntent = {}
        )
      }
    }

    composeTestRule.onNodeWithText("30% ${Constants.GOALS_PROGRESS_TEXT}").assertIsDisplayed()
  }

  @Test
  fun showsTransferButton_whenRoundUpAvailable() {
    val goal = DomainSavingsGoal(
      name = "Holiday Fund",
      targetAmount = DomainAmount("GBP", 50000, "£500.00"),
      savingsGoalUid = "123",
      totalSaved = DomainAmount("GBP", 0, "£0.00"),
      state = "ACTIVE"
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.Idle,
            savingsGoals = listOf(goal),
            roundedAmount = 2450
          ),
          onIntent = {}
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_ROUND_UP_AVAILABLE).assertIsDisplayed()
    composeTestRule.onNodeWithText("£24.50").assertIsDisplayed()
    composeTestRule.onNodeWithText(Constants.GOALS_TRANSFER_BUTTON).assertIsDisplayed()
  }

  @Test
  fun callsTransferIntent_whenTransferButtonClicked() {
    val goal = DomainSavingsGoal(
      name = "Holiday Fund",
      targetAmount = DomainAmount("GBP", 50000, "£500.00"),
      savingsGoalUid = "123",
      totalSaved = DomainAmount("GBP", 0, "£0.00"),
      state = "ACTIVE"
    )

    var transferCalled = false

    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.Idle,
            savingsGoals = listOf(goal),
            roundedAmount = 2450
          ),
          onIntent = { intent ->
            if (intent is Intent.TransferToSavingsGoal) transferCalled = true
          }
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_TRANSFER_BUTTON).performClick()
    assert(transferCalled)
  }

  @Test
  fun callsDeleteIntent_whenDeleteButtonClicked() {
    val goal = DomainSavingsGoal(
      name = "Holiday Fund",
      targetAmount = DomainAmount("GBP", 50000, "£500.00"),
      savingsGoalUid = "123",
      totalSaved = DomainAmount("GBP", 15000, "£150.00"),
      state = "ACTIVE"
    )

    var deleteCalled = false

    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.Idle,
            savingsGoals = listOf(goal)
          ),
          onIntent = { intent ->
            if (intent is Intent.DeleteSavingsGoal) deleteCalled = true
          }
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_DELETE_GOAL_BUTTON).performClick()
    assert(deleteCalled)
  }

  @Test
  fun disablesButtons_whenOffline() {
    val goal = DomainSavingsGoal(
      name = "Holiday Fund",
      targetAmount = DomainAmount("GBP", 50000, "£500.00"),
      savingsGoalUid = "123",
      totalSaved = DomainAmount("GBP", 0, "£0.00"),
      state = "ACTIVE"
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.Idle,
            savingsGoals = listOf(goal),
            roundedAmount = 2450,
            dataSource = com.example.roundupapp.domain.models.DataSource.CACHE
          ),
          onIntent = {}
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_TRANSFER_BUTTON).assertIsNotEnabled()
    composeTestRule.onNodeWithText(Constants.GOALS_DELETE_GOAL_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun showsLoadingIndicator_whenTransferring() {
    val goal = DomainSavingsGoal(
      name = "Holiday Fund",
      targetAmount = DomainAmount("GBP", 50000, "£500.00"),
      savingsGoalUid = "123",
      totalSaved = DomainAmount("GBP", 0, "£0.00"),
      state = "ACTIVE"
    )

    composeTestRule.setContent {
      RoundUpAppTheme {
        Goals(
          state = ScreenState(
            loadingState = LoadingState.InProgress(LoadingState.Operation.TRANSFERRING),
            savingsGoals = listOf(goal),
            roundedAmount = 2450
          ),
          onIntent = {},
          isInProgress = true
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_TRANSFERRING_BUTTON).assertIsDisplayed()
  }
}
