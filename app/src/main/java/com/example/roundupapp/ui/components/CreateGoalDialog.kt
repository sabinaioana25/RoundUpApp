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
import androidx.compose.ui.unit.dp
import com.example.roundupapp.ui.theme.RoundUpAppTheme

@Composable
fun CreateGoalDialog(
  onConfirm: (name: String, amount: Int) -> Unit,
  onDismiss: () -> Unit
) {
  var goalName by remember { mutableStateOf("") }
  var targetAmount by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create a new Savings Goal") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextField(
          value = goalName,
          onValueChange = { goalName = it },
          label = { Text("Goal Name") },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
          singleLine = true
        )
        TextField(
          value = targetAmount,
          onValueChange = {
            if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
              targetAmount = it
            }
          },
          label = { Text("Target Amount") },
          prefix = { Text("£") },
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
            onConfirm(goalName, targetAmount.toDoubleOrNull()?.toInt() ?: 0)
          }
        })
      {
        Text("Create")
      }
    },
    dismissButton = {
      Button(onClick = onDismiss) {
        Text("Cancel")
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
