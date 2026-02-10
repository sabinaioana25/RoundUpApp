package com.example.roundupapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.roundupapp.domain.models.DataSource
import com.example.roundupapp.ui.components.Goals
import com.example.roundupapp.ui.components.Transactions
import com.example.roundupapp.ui.components.aListOfDomainSavingGoals
import com.example.roundupapp.ui.components.aListOfTransactions
import com.example.roundupapp.ui.theme.Dimens
import com.example.roundupapp.ui.theme.RoundUpAppTheme
import com.example.roundupapp.utils.Constants.HOME_SCREEN_BALANCE
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ERROR_EMPTY_STATE
import com.example.roundupapp.utils.Constants.HOME_SCREEN_LOADING_MESSAGE
import com.example.roundupapp.utils.Constants.HOME_SCREEN_OFFLINE_INDICATOR
import com.example.roundupapp.utils.Constants.HOME_SCREEN_PULL_TO_REFRESH
import com.example.roundupapp.utils.Constants.HOME_SCREEN_ROUND_UP_BUTTON
import com.example.roundupapp.utils.Constants.HOME_SCREEN_SAMPLE_BALANCE

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
  val snackbarHostState = remember { SnackbarHostState() }

  // Show errors in snackbar
  LaunchedEffect(state.error) {
    state.error?.let { error ->
      snackbarHostState.showSnackbar(error.message)
      onIntent(Intent.DismissError)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets(0),
    topBar = {
      TopAppBar(
        title = { 
          Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.small),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(HOME_SCREEN_ROUND_UP_BUTTON)
            
            // Offline indicator
            if (state.isOffline) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = HOME_SCREEN_OFFLINE_INDICATOR,
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        },
        windowInsets = WindowInsets(0)
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
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
              text = HOME_SCREEN_LOADING_MESSAGE,
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
              text = HOME_SCREEN_ERROR_EMPTY_STATE,
              style = MaterialTheme.typography.titleMedium
            )
            Text(
              text = HOME_SCREEN_PULL_TO_REFRESH,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimens.Spacing.default),
    verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.default)
  ) {
    // Balance Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation.default)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(Dimens.Spacing.default),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = HOME_SCREEN_BALANCE,
          style = MaterialTheme.typography.labelMedium
        )
        Text(
          text = state.balance,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold
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
    Transactions(
      modifier = Modifier
        .weight(Dimens.Layout.defaultWeight)
        .fillMaxWidth(),
      transactions = state.transactions
    )
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
fun HomeScreenWithDataPreview() {
  RoundUpAppTheme {
    HomeScreen(
      state = ScreenState(
        loadingState = LoadingState.Idle,
        transactions = aListOfTransactions,
        balance = HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = aListOfDomainSavingGoals,
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
        balance = HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = aListOfDomainSavingGoals,
        roundedAmount = 44552,
        dataSource = DataSource.CACHE
      ),
      onIntent = {}
    )
  }
}

@PreviewLightDark
@Composable
fun HomeScreenRefreshingPreview() {
  RoundUpAppTheme {
    HomeScreen(
      state = ScreenState(
        loadingState = LoadingState.Refreshing,
        transactions = aListOfTransactions,
        balance = HOME_SCREEN_SAMPLE_BALANCE,
        savingsGoals = aListOfDomainSavingGoals,
        roundedAmount = 44552
      ),
      onIntent = {}
    )
  }
}
