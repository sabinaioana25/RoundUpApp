package com.example.roundupapp.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants
import org.junit.Rule
import org.junit.Test

class CreateGoalDialogTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun displaysDialogTitle() {
    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { _, _ -> },
          onDismiss = {}
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_DIALOG_TITLE)
      .assertIsDisplayed()
  }

  @Test
  fun displaysInputFields() {
    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { _, _ -> },
          onDismiss = {}
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_NAME_GOAL).assertIsDisplayed()
    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_TARGET).assertIsDisplayed()
  }

  @Test
  fun displaysActionButtons() {
    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { _, _ -> },
          onDismiss = {}
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE)
      .assertIsDisplayed()
    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CANCEL)
      .assertIsDisplayed()
  }

  @Test
  fun callsOnDismiss_whenCancelClicked() {
    var dismissCalled = false

    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { _, _ -> },
          onDismiss = { dismissCalled = true }
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CANCEL)
      .performClick()

    assert(dismissCalled)
  }

  @Test
  fun callsOnConfirm_whenValidInputProvided() {
    var confirmedName = ""
    var confirmedAmount = 0

    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { name, amount ->
            confirmedName = name
            confirmedAmount = amount
          },
          onDismiss = {}
        )
      }
    }

    // Enter goal name
    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_NAME_GOAL)
      .performTextInput("Holiday Fund")

    // Enter target amount
    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_TARGET)
      .performTextInput("500")

    // Click create
    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE)
      .performClick()

    assert(confirmedName == "Holiday Fund")
    assert(confirmedAmount == 500)
  }

  @Test
  fun showsError_whenNameIsEmpty() {
    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { _, _ -> },
          onDismiss = {}
        )
      }
    }

    // Enter only amount, leave name empty
    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_TARGET)
      .performTextInput("500")

    // Click create
    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE)
      .performClick()

    // Error should be displayed
    composeTestRule.onNodeWithText("Goal name cannot be empty").assertIsDisplayed()
  }

  @Test
  fun showsError_whenAmountIsEmpty() {
    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { _, _ -> },
          onDismiss = {}
        )
      }
    }

    // Enter only name, leave amount empty
    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_NAME_GOAL)
      .performTextInput("Holiday Fund")

    // Click create
    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE)
      .performClick()

    // Error should be displayed
    composeTestRule.onNodeWithText("Target amount cannot be empty").assertIsDisplayed()
  }

  @Test
  fun allowsDecimalInput_forAmount() {
    var confirmedAmount = 0

    composeTestRule.setContent {
      RoundUpAppTheme {
        CreateGoalDialog(
          onConfirm = { _, amount ->
            confirmedAmount = amount
          },
          onDismiss = {}
        )
      }
    }

    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_NAME_GOAL)
      .performTextInput("Holiday Fund")

    composeTestRule.onNodeWithText(Constants.GOALS_CARD_COMPOSABLE_TARGET)
      .performTextInput("500.50")

    composeTestRule.onNodeWithText(Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE)
      .performClick()

    // 500.50 pounds = 500 pounds (integer conversion)
    assert(confirmedAmount == 500)
  }
}
