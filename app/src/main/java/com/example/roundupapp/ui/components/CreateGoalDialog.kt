package com.example.roundupapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CANCEL
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_COMPOSABLE_DIALOG_TITLE
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_CURRENCY_PREFIX
import com.example.roundupapp.utils.Constants.GOALS_CARD_COMPOSABLE_NAME_GOAL
import com.example.roundupapp.utils.Constants.GOALS_CARD_COMPOSABLE_TARGET

/**
 * Dialog for creating a new savings goal
 * Collects goal name and target amount with input validation
 */
@Composable
fun CreateGoalDialog(
  onConfirm: (name: String, amountMinorUnits: Int) -> Unit,
  onDismiss: () -> Unit
) {
  var goalName by remember { mutableStateOf("") }
  var targetAmount by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(ALERT_DIALOG_COMPOSABLE_DIALOG_TITLE) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.default)) {
        TextField(
          value = goalName,
          onValueChange = { goalName = it },
          label = { Text(GOALS_CARD_COMPOSABLE_NAME_GOAL) },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
          singleLine = true
        )
        TextField(
          value = targetAmount,
          onValueChange = {
            // Allow only valid decimal numbers with up to 2 decimal places
            if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
              targetAmount = it
            }
          },
          label = { Text(GOALS_CARD_COMPOSABLE_TARGET) },
          prefix = { Text(ALERT_DIALOG_CURRENCY_PREFIX) },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
          }),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (goalName.isNotBlank() && targetAmount.isNotBlank()) {
            val amountInMinorUnits = convertToMinorUnits(targetAmount)
            onConfirm(goalName, amountInMinorUnits)
          }
        })
      {
        Text(ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE)
      }
    },
    dismissButton = {
      Button(onClick = onDismiss) {
        Text(ALERT_DIALOG_COMPOSABLE_BUTTON_CANCEL)
      }
    }
  )
}

private fun convertToMinorUnits(decimalString: String): Int {
  if (decimalString.isBlank()) return 0
  
  return try {
    val pounds = decimalString.toDouble()
    (pounds * 100).toInt()
  } catch (e: NumberFormatException) {
    0
  }
}

@Preview
@Composable
fun CreateGoalsDialogPreview() {
  RoundUpAppTheme {
    CreateGoalDialog(
      onConfirm = { _, _ -> },
      onDismiss = { }
    )
  }
}
