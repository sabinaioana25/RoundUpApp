package com.example.roundupapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.roundupapp.domain.models.DataSource
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.ui.components.Goals
import com.example.roundupapp.ui.components.Transactions
import com.example.roundupapp.ui.components.aListOfTransactions
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants

@Composable
fun HomeScreenHoist(
  viewModel: HomeViewModel,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsState()
  val onIntent: (Intent) -> Unit = viewModel::processIntent
  HomeScreen(
    state = state,
    onIntent = onIntent,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  state: ScreenState,
  onIntent: (Intent) -> Unit,
  modifier: Modifier = Modifier
) {
  val snackBarHostState = remember { SnackbarHostState() }

  // Show errors in snackbar
  LaunchedEffect(state.error) {
    state.error?.let { error ->
      snackBarHostState.showSnackbar(error.message)
      onIntent(Intent.DismissError)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets(0),
    topBar = {
      if (state.isOffline) {
        TopAppBar(
          title = { 
            Row(
              horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.small),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = Constants.HOME_SCREEN_OFFLINE_INDICATOR,
                tint = MaterialTheme.colorScheme.error
              )
              Text(
                text = Constants.HOME_SCREEN_OFFLINE_INDICATOR,
                style = MaterialTheme.typography.labelMedium
              )
            }
          },
          windowInsets = WindowInsets(0)
        )
      }
    },
    snackbarHost = { SnackbarHost(snackBarHostState) },
  ) { paddingValues ->
    when {
      // Initial loading state
      state.isInitialLoading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.default)
          ) {
            CircularProgressIndicator()
            Text(
              text = Constants.HOME_SCREEN_LOADING_MESSAGE,
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }

      // No data and not loading
      !state.hasData && !state.isLoading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.default)
          ) {
            Text(
              text = "⚠️",
              style = MaterialTheme.typography.displayMedium
            )
            Text(
              text = Constants.HOME_SCREEN_ERROR_EMPTY_STATE,
              style = MaterialTheme.typography.titleMedium
            )
            Text(
              text = Constants.HOME_SCREEN_PULL_TO_REFRESH,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = { onIntent(Intent.Refresh) }) {
              Text(Constants.HOME_SCREEN_BUTTON_RETRY)
            }
          }
        }
      }

      // Content with pull-to-refresh
      else -> {
        PullToRefreshBox(
          isRefreshing = state.isRefreshing,
          onRefresh = { onIntent(Intent.Refresh) },
          modifier = Modifier.padding(paddingValues)
        ) {
          HomeScreenContent(
            state = state,
            onIntent = onIntent
          )
        }
      }
    }
  }
}

@Composable
private fun HomeScreenContent(
  state: ScreenState,
  onIntent: (Intent) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(Dimens.Spacing.default),
    verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.default)
  ) {
    // Balance Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation.default),
      shape = RoundedCornerShape(Dimens.Corner.card)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(Dimens.Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = Constants.HOME_SCREEN_BALANCE,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
          text = state.balance,
          style = MaterialTheme.typography.displaySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }

    // Goals Section
    Goals(
      state = state,
      onIntent = onIntent,
      isInProgress = state.loadingState is LoadingState.InProgress
    )

    // Transactions Section
    Column(
      modifier = Modifier
        .weight(Dimens.Layout.DEFAULT)
        .fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(Dimens.Corner.card))
      ) {
        Transactions(
          transactions = state.transactions
        )
      }
    }
  }
}

@PreviewLightDark
@Composable
fun HomeScreenLoadingPreview() {
  RoundUpAppTheme {
    HomeScreen(
      state = ScreenState(
        loadingState = LoadingState.InitialLoading
      ),
      onIntent = {}
    )
  }
}

@PreviewLightDark
@Composable
fun HomeScreenErrorPreview() {
  RoundUpAppTheme {
    HomeScreen(
      state = ScreenState(
        loadingState = LoadingState.Idle
      ),
      onIntent = {}
    )
  }
}

@PreviewLightDark
@Composable
fun HomeScreenWithDataPreview() {
  RoundUpAppTheme {
    HomeScreen(
      state = ScreenState(
        loadingState = LoadingState.Idle,
        transactions = aListOfTransactions,
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
        roundedAmount = 44552,
        dataSource = DataSource.NETWORK
      ),
      onIntent = {}
    )
  }
}

@PreviewLightDark
@Composable
fun HomeScreenOfflinePreview() {
  RoundUpAppTheme {
    HomeScreen(
      state = ScreenState(
        loadingState = LoadingState.Idle,
        transactions = aListOfTransactions,
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
        roundedAmount = 44552,
        dataSource = DataSource.CACHE
      ),
      onIntent = {}
    )
  }
}
