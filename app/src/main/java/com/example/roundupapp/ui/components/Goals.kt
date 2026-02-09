package com.example.roundupapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.ui.home.Intent
import com.example.roundupapp.ui.home.LoadingState
import com.example.roundupapp.ui.home.ScreenState
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants.ALERT_SAMPLE_GOAL_NAME
import com.example.roundupapp.utils.Constants.GOALS_CARD_COMPOSABLE_NAME_GOAL
import com.example.roundupapp.utils.Constants.GOALS_CREATE_GOAL_BUTTON
import com.example.roundupapp.utils.Constants.GOALS_DELETING_BUTTON
import com.example.roundupapp.utils.Constants.GOALS_DELETE_GOAL_BUTTON
import com.example.roundupapp.utils.Constants.GOALS_ROUND_UP_AVAILABLE
import com.example.roundupapp.utils.Constants.GOALS_TARGET
import com.example.roundupapp.utils.Constants.GOALS_TOTAL_SAVED
import com.example.roundupapp.utils.Constants.GOALS_TRANSFERRING_BUTTON
import com.example.roundupapp.utils.Constants.GOALS_TRANSFER_BUTTON
import com.example.roundupapp.utils.Constants.HOME_SCREEN_SAMPLE_BALANCE
import com.example.roundupapp.utils.Constants.REPO_CURRENCY_GBP
import com.example.roundupapp.utils.toGbp

@Composable
fun Goals(
  modifier: Modifier = Modifier,
  state: ScreenState,
  onIntent: (Intent) -> Unit,
  isInProgress: Boolean = false
) {
  var showCreateGoalDialog by rememberSaveable { mutableStateOf(false) }

  if (state.savingsGoals.isEmpty()) {
    Button(
      onClick = { showCreateGoalDialog = true },
      enabled = !isInProgress
    ) {
      if (isInProgress && state.loadingState is LoadingState.InProgress
        && state.loadingState.operation == LoadingState.Operation.CREATING_GOAL
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(Dimens.Spacing.default),
          strokeWidth = Dimens.Stroke.default,
          color = MaterialTheme.colorScheme.onPrimary
        )
      } else {
        Text(GOALS_CREATE_GOAL_BUTTON)
      }
    }
  }

  if (showCreateGoalDialog) {
    CreateGoalDialog(
      onConfirm = { name, amount ->
        onIntent(Intent.CreateSavingsGoal(name, amount, REPO_CURRENCY_GBP))
        showCreateGoalDialog = false
      },
      onDismiss = { showCreateGoalDialog = false }
    )
  }

  CreatedGoal(state, onIntent, isInProgress)
}

@Composable
private fun CreatedGoal(
  state: ScreenState,
  onIntent: (Intent) -> Unit,
  isInProgress: Boolean
) {
  state.savingsGoals.firstOrNull()?.let { savingsGoal ->
    Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation.default)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(Dimens.Spacing.default),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.medium)
      ) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Center,
          text = GOALS_CARD_COMPOSABLE_NAME_GOAL,
          style = MaterialTheme.typography.labelMedium
        )

        Text(
          text = savingsGoal.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "$GOALS_TOTAL_SAVED${savingsGoal.totalSaved.minorUnits.toGbp()}",
          style = MaterialTheme.typography.bodyMedium
        )

        Text(
          text = "$GOALS_TARGET${savingsGoal.targetAmount.minorUnits.toGbp()}",
          style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(Dimens.Spacing.small))

        if (savingsGoal.totalSaved.minorUnits == 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = GOALS_ROUND_UP_AVAILABLE,
                style = MaterialTheme.typography.bodySmall
              )
              Text(
                text = state.roundedAmount.toGbp(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
            }

            Button(
              onClick = { onIntent(Intent.TransferToSavingsGoal) },
              enabled = !isInProgress && state.roundedAmount > 0
            ) {
              if (isInProgress && state.loadingState is LoadingState.InProgress
                && state.loadingState.operation == LoadingState.Operation.TRANSFERRING
              ) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.small),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.Spacing.default),
                    strokeWidth = Dimens.Stroke.default,
                    color = MaterialTheme.colorScheme.onPrimary
                  )
                  Text(GOALS_TRANSFERRING_BUTTON)
                }
              } else {
                Text(GOALS_TRANSFER_BUTTON)
              }
            }
          }
        }

        OutlinedButton(
          onClick = { onIntent(Intent.DeleteSavingsGoal) },
          enabled = !isInProgress,
          modifier = Modifier.fillMaxWidth()
        ) {
          if (isInProgress && state.loadingState is LoadingState.InProgress
            && state.loadingState.operation == LoadingState.Operation.DELETING_GOAL
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.small),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(Dimens.Spacing.default),
                strokeWidth = Dimens.Stroke.default
              )
              Text(GOALS_DELETING_BUTTON)
            }
          } else {
            Text(GOALS_DELETE_GOAL_BUTTON)
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun GoalsEmptyPreview() {
  Goals(
    state = ScreenState(
      loadingState = LoadingState.InitialLoading,
      transactions = emptyList(),
      balance = HOME_SCREEN_SAMPLE_BALANCE,
      savingsGoals = emptyList(),
      roundedAmount = 0
    ),
    onIntent = {}
  )
}

@PreviewLightDark
@Composable
fun GoalsWithGoalPreview() {
  RoundUpAppTheme {
    Goals(
      state = ScreenState(
        loadingState = LoadingState.InitialLoading,
        transactions = emptyList(),
        balance = HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = aListOfDomainSavingGoals,
        roundedAmount = 2450
      ),
      onIntent = {}
    )
  }
}

@PreviewLightDark
@Composable
fun GoalsTransferringPreview() {
  RoundUpAppTheme {
    Goals(
      state = ScreenState(
        loadingState = LoadingState.InProgress(LoadingState.Operation.TRANSFERRING),
        transactions = emptyList(),
        balance = HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = listOf(
          DomainSavingsGoal(
            name = ALERT_SAMPLE_GOAL_NAME,
            targetAmount = DomainAmount(REPO_CURRENCY_GBP, 50000, "£500.00"),
            savingsGoalUid = "",
            totalSaved = DomainAmount(REPO_CURRENCY_GBP, 0, "£0.00"),
            state = ""
          )
        ),
        roundedAmount = 2450
      ),
      onIntent = {},
      isInProgress = true
    )
  }
}

val aListOfDomainSavingGoals = listOf(
  DomainSavingsGoal(
    name = ALERT_SAMPLE_GOAL_NAME,
    targetAmount = DomainAmount(REPO_CURRENCY_GBP, 50000, "£500.00"),
    savingsGoalUid = "",
    totalSaved = DomainAmount(REPO_CURRENCY_GBP, 0, "£0.00"),
    state = ""
  )
)
