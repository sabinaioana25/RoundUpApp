package com.example.roundupapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.ui.home.Intent
import com.example.roundupapp.ui.home.LoadingState
import com.example.roundupapp.ui.home.ScreenState
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants
import com.example.roundupapp.utils.toGbp

@Composable
fun Goals(
  modifier: Modifier = Modifier,
  state: ScreenState,
  onIntent: (Intent) -> Unit,
  isInProgress: Boolean = false
) {
  var showCreateGoalDialog by rememberSaveable { mutableStateOf(false) }

  Column(modifier = modifier) {
    if (state.savingsGoals.isEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation.default),
        shape = RoundedCornerShape(Dimens.Corner.card)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.Spacing.large),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.medium)
        ) {
          Text(
            text = Constants.GOALS_NO_GOALS_YET,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = Constants.GOALS_CREATE_TO_START,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
          
          val isCreating = isInProgress &&
            state.loadingState is LoadingState.InProgress &&
            state.loadingState.operation == LoadingState.Operation.CREATING_GOAL

          Button(
            onClick = { showCreateGoalDialog = true },
            enabled = !isInProgress && !state.isOffline,
            shape = RoundedCornerShape(Dimens.Corner.button)
          ) {
            if (isCreating) {
              CircularProgressIndicator(
                modifier = Modifier.size(Dimens.Spacing.default),
                strokeWidth = Dimens.Stroke.default,
                color = MaterialTheme.colorScheme.onPrimary
              )
            } else {
              Text(Constants.GOALS_CREATE_FIRST_GOAL_BUTTON)
            }
          }
          
          if (state.isOffline && !isInProgress) {
            Text(
              text = Constants.GOALS_OFFLINE_CREATE_DISABLED,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    if (showCreateGoalDialog) {
      CreateGoalDialog(
        onConfirm = { name, amount ->
          onIntent(Intent.CreateSavingsGoal(name, amount, Constants.REPO_CURRENCY_GBP))
          showCreateGoalDialog = false
        },
        onDismiss = { showCreateGoalDialog = false }
      )
    }

    CreatedGoal(state, onIntent, isInProgress)
  }
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
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation.default),
      shape = RoundedCornerShape(Dimens.Corner.card)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(Dimens.Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.default)
      ) {
        // Goal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = savingsGoal.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = Constants.GOALS_TARGET_PREFIX + savingsGoal.targetAmount.minorUnits.toGbp(),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
          }
          
          // Goal Icon
          Box(
            modifier = Modifier
              .size(Dimens.Size.iconBadge)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = Constants.GOALS_ICON_EMOJI,
              fontSize = Dimens.Font.emojiSize
            )
          }
        }

        // Progress indicator
        val progress = if (savingsGoal.targetAmount.minorUnits > 0) {
          (savingsGoal.totalSaved.minorUnits.toFloat() / savingsGoal.targetAmount.minorUnits.toFloat()).coerceIn(0f, 1f)
        } else 0f
        
        val progressPercentage = (progress * 100).toInt()

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.extraSmall)) {
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(Dimens.Size.progressBar)
              .clip(RoundedCornerShape(Dimens.Corner.progressBar)),
          )
          Text(
            text = "$progressPercentage% ${Constants.GOALS_PROGRESS_TEXT}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
          )
        }

        // Round-up transfer section
        if (savingsGoal.totalSaved.minorUnits == 0) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(Dimens.Corner.button))
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
              .padding(Dimens.Spacing.default),
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.small)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = Constants.GOALS_ROUND_UP_AVAILABLE,
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                  text = state.roundedAmount.toGbp(),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              
              val isTransferring = isInProgress &&
                state.loadingState is LoadingState.InProgress &&
                state.loadingState.operation == LoadingState.Operation.TRANSFERRING

              Button(
                onClick = { onIntent(Intent.TransferToSavingsGoal) },
                enabled = !isInProgress && !state.isOffline && state.roundedAmount > 0,
                shape = RoundedCornerShape(Dimens.Corner.button),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                )
              ) {
                if (isTransferring) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(Dimens.Spacing.default),
                      strokeWidth = Dimens.Stroke.default,
                      color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(Constants.GOALS_TRANSFERRING_BUTTON)
                  }
                } else {
                  Text(Constants.GOALS_TRANSFER_BUTTON)
                }
              }
            }
            
            if (state.isOffline && !isInProgress && state.roundedAmount > 0) {
              Text(
                text = Constants.GOALS_OFFLINE_LABEL,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
              )
            }
          }
        }

        // Delete button
        val isDeleting = isInProgress &&
          state.loadingState is LoadingState.InProgress &&
          state.loadingState.operation == LoadingState.Operation.DELETING_GOAL

        OutlinedButton(
          onClick = { onIntent(Intent.DeleteSavingsGoal) },
          enabled = !isInProgress && !state.isOffline,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(Dimens.Corner.button),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
          )
        ) {
          if (isDeleting) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.small),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(Dimens.Spacing.default),
                strokeWidth = Dimens.Stroke.default
              )
              Text(Constants.GOALS_DELETING_BUTTON)
            }
          } else {
            Text(Constants.GOALS_DELETE_GOAL_BUTTON)
          }
        }
        
        // Show helper text when disabled due to offline
        if (state.isOffline && !isInProgress) {
          Text(
            text = Constants.GOALS_OFFLINE_DELETE_DISABLED,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

@PreviewLightDark
@Composable
fun GoalsEmptyPreview() {
  RoundUpAppTheme {
    Goals(
      state = ScreenState(
        loadingState = LoadingState.Idle,
        transactions = emptyList(),
        balance = Constants.HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = emptyList(),
        roundedAmount = 0
      ),
      onIntent = {}
    )
  }
}

@PreviewLightDark
@Composable
fun GoalsWithGoalPreview() {
  RoundUpAppTheme {
    Goals(
      state = ScreenState(
        loadingState = LoadingState.Idle,
        transactions = emptyList(),
        balance = Constants.HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = listOf(
          DomainSavingsGoal(
            name = Constants.ALERT_SAMPLE_GOAL_NAME,
            targetAmount = DomainAmount(Constants.REPO_CURRENCY_GBP, 50000, "£500.00"),
            savingsGoalUid = "",
            totalSaved = DomainAmount(Constants.REPO_CURRENCY_GBP, 15000, "£150.00"),
            state = ""
          )
        ),
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
        balance = Constants.HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = listOf(
          DomainSavingsGoal(
            name = Constants.ALERT_SAMPLE_GOAL_NAME,
            targetAmount = DomainAmount(Constants.REPO_CURRENCY_GBP, 50000, "£500.00"),
            savingsGoalUid = "",
            totalSaved = DomainAmount(Constants.REPO_CURRENCY_GBP, 0, "£0.00"),
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
