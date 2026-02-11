package com.example.roundupapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.example.roundupapp.domain.models.CurrencyConverter
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CANCEL
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_COMPOSABLE_DIALOG_TITLE
import com.example.roundupapp.utils.Constants.ALERT_DIALOG_CURRENCY_PREFIX
import com.example.roundupapp.utils.Constants.GOALS_CARD_COMPOSABLE_NAME_GOAL
import com.example.roundupapp.utils.Constants.GOALS_CARD_COMPOSABLE_TARGET

private val AMOUNT_REGEX = Regex("^\\d*\\.?\\d{0,2}$")

/**
 * Dialog for creating a new savings goal
 * Collects goal name and target amount with input validation
 */
@Composable
fun CreateGoalDialog(
  onConfirm: (name: String, amountInPounds: Int) -> Unit,
  onDismiss: () -> Unit
) {
  var goalName by remember { mutableStateOf("") }
  var targetAmount by remember { mutableStateOf("") }
  var nameError by remember { mutableStateOf<String?>(null) }
  var amountError by remember { mutableStateOf<String?>(null) }
  val focusManager = LocalFocusManager.current

  fun validateAmount(amount: String): Pair<String?, Int?> {
    if (amount.isBlank()) return "Target amount cannot be empty" to null
    val minorUnits = CurrencyConverter.parseToMinorUnits(amount)
      ?: return "Please enter a valid amount" to null
    if (minorUnits <= 0) return "Amount must be greater than zero" to null
    return null to minorUnits / 100
  }

  fun validateAndSubmit() {
    nameError = if (goalName.isBlank()) "Goal name cannot be empty" else null
    val (error, amountInPounds) = validateAmount(targetAmount)
    amountError = error

    if (nameError == null && amountInPounds != null) {
      onConfirm(goalName.trim(), amountInPounds)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(ALERT_DIALOG_COMPOSABLE_DIALOG_TITLE) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.default)) {
        TextField(
          value = goalName,
          onValueChange = {
            goalName = it
            nameError = null
          },
          label = { Text(GOALS_CARD_COMPOSABLE_NAME_GOAL) },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) }
          ),
          singleLine = true,
          isError = nameError != null,
          supportingText = nameError?.let { message ->
            { Text(text = message, color = MaterialTheme.colorScheme.error) }
          }
        )

        TextField(
          value = targetAmount,
          onValueChange = {
            if (it.isEmpty() || it.matches(AMOUNT_REGEX)) {
              targetAmount = it
              amountError = null
            }
          },
          label = { Text(GOALS_CARD_COMPOSABLE_TARGET) },
          prefix = { Text(ALERT_DIALOG_CURRENCY_PREFIX) },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              focusManager.clearFocus()
              validateAndSubmit()
            }
          ),
          singleLine = true,
          isError = amountError != null,
          supportingText = amountError?.let { message ->
            { Text(text = message, color = MaterialTheme.colorScheme.error) }
          }
        )
      }
    },
    confirmButton = {
      Button(onClick = { validateAndSubmit() }) {
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
