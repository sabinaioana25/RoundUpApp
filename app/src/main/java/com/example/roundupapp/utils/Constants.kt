package com.example.roundupapp.utils

object Constants {

  // HomeScreen & VM
  const val HOME_SCREEN_ERROR_EMPTY_STATE = "No data to display"
  const val HOME_SCREEN_LOADING_MESSAGE = "Loading your account..."
  const val HOME_SCREEN_ERROR_LOADING_INITIAL_DATA = "Error loading data. Please try again"
  const val HOME_SCREEN_PULL_TO_REFRESH = "Pull down to refresh"
  const val HOME_SCREEN_BUTTON_RETRY = "Retry"
  const val HOME_SCREEN_BALANCE = "Current Balance"
  const val HOME_SCREEN_SAMPLE_BALANCE = "£1000.00"
  const val HOME_SCREEN_NO_ROUND_UP_AVAILABLE = "No round up available to transfer"

  // Goals card
  const val GOALS_CARD_COMPOSABLE_NAME_GOAL = "Goal"
  const val GOALS_CARD_COMPOSABLE_TARGET = "Target"
  const val GOALS_NAME_BLANK_WARNING = "Name cannot be blank"
  const val GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO = "Value must be greater than zero"
  const val GOALS_CREATING_FAILURE = "Failed to create goal"
  const val GOALS_NO_SAVINGS_GOALS_TO_DELETE = "No savings goals to delete"
  const val GOALS_FAILURE_DELETING = "Failed to delete goal"
  const val GOALS_CREATE_FIRST_GOAL_BUTTON = "Create Your First Goal"
  const val GOALS_NO_GOALS_YET = "No Savings Goals Yet"
  const val GOALS_CREATE_TO_START = "Create a goal to start saving"
  const val GOALS_TARGET_PREFIX = "Target: "
  const val GOALS_PROGRESS_TEXT = "of goal reached"
  const val GOALS_ROUND_UP_AVAILABLE = "Available Round-up"
  const val GOALS_TRANSFERRING_BUTTON = "Transferring"
  const val GOALS_TRANSFER_BUTTON = "Transfer Now"
  const val GOALS_DELETING_BUTTON = "Deleting"
  const val GOALS_DELETE_GOAL_BUTTON = "Delete Goal"
  const val GOALS_OFFLINE_CREATE_DISABLED = "Connect to network to create goals"
  const val GOALS_OFFLINE_DELETE_DISABLED = "Connect to network to delete goal"
  const val GOALS_OFFLINE_LABEL = "Offline"
  const val GOALS_ICON_EMOJI = "🎯"

  // Alert dialog
  const val ALERT_DIALOG_COMPOSABLE_DIALOG_TITLE = "Create a new Savings Goal"
  const val ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE = "Create"
  const val ALERT_DIALOG_COMPOSABLE_BUTTON_CANCEL = "Cancel"
  const val ALERT_TRANSFER_FAILED = "Transfer failed. Please try again"
  const val ALERT_TRANSFER_FAILURE_EXCEPTION = "Transfer failed: unexpected transferUid"
  const val ALERT_DIALOG_CURRENCY_PREFIX = "£"
  const val ALERT_SAMPLE_GOAL_NAME = "Holiday Fund"

  // RoundUpRepositoryImpl
  const val REPO_CURRENCY_GBP = "GBP"
  const val REPO_ROUND_UP_TRANSFER_REFERENCE = "Round-up transfer"
  const val REPO_ACCOUNT_UID_MISSING = "Account UID missing"

  // Transactions
  const val TRANSACTIONS_DATE_FORMAT_TODAY = "Today"
  const val TRANSACTIONS_DATE_FORMAT_YESTERDAY = "Yesterday"
  const val TRANSACTION_DATE_FORMAT_DD_MM_YYYY = "MMM dd, yyyy"

  // Offline handling
  const val HOME_SCREEN_OFFLINE_ERROR = "No internet connection. Showing cached data."
  const val HOME_SCREEN_OFFLINE_INDICATOR = "Offline mode"

  // Offline error messages
  const val OFFLINE_DELETE_GOAL_ERROR = "Cannot delete savings goal while offline"
  const val OFFLINE_CREATE_GOAL_ERROR = "Cannot create savings goal while offline"
  const val OFFLINE_TRANSFER_ERROR = "Cannot transfer funds while offline"
  const val OFFLINE_NO_NETWORK = "No network connectivity"

  // Validation field names
  const val VALIDATION_GOAL_NAME = "Goal name"
  const val VALIDATION_TARGET_AMOUNT = "Target amount"
  const val VALIDATION_TRANSFER_AMOUNT = "Transfer amount"

  // Error messages
  const val ERROR_GOAL_NOT_FOUND = "Created goal not found in server response"
  const val ERROR_NO_ACCOUNTS_FOUND = "No accounts found"
  const val ERROR_CACHE_EMPTY_FIRST_RUN = "Cache is empty (first run)"
  const val ERROR_UNEXPECTED_ACCOUNT_DETAILS = "Unexpected error loading account details"
  const val ERROR_UNEXPECTED_TRANSFER = "Unexpected error during transfer"
  const val ERROR_UNEXPECTED_CREATE_GOAL = "Created goal not found in server response"
  const val ERROR_UNEXPECTED_DELETE_GOAL = "Unexpected error deleting savings goal"

  // Default values
  const val DEFAULT_BALANCE = "0.00"

  // Log messages
  object LogMessages {
    // Create Goal
    const val CREATE_GOAL_SERVER_FAILED = "Failed to create savings goal on server"
    const val CREATE_GOAL_FETCH_FAILED = "Failed to fetch created goal details"
    const val CREATE_GOAL_CACHE_FAILED = "Failed to cache created goal"
    const val CREATE_GOAL_CACHE_SUCCESS = "Successfully cached created goal"
    const val CREATE_GOAL_SUCCESS = "Goal created successfully"
    const val CREATE_GOAL_FAILED = "Failed to create goal"

    // Delete Goal
    const val DELETE_GOAL_FAILED = "Failed to delete savings goal"
    const val DELETE_CACHE_FAILED = "Failed to delete goal from cache (non-fatal)"
    const val DELETE_CACHE_SUCCESS = "Successfully deleted goal from cache"
    const val DELETE_GOAL_SUCCESS = "Goal deleted successfully"

    // Transfer
    const val TRANSFER_INITIATING = "Initiating transfer: %d pence to goal %s"
    const val TRANSFER_SUCCESS = "Transfer successful: %s"
    const val TRANSFER_FAILED = "Transfer failed: %s"
    const val TRANSFER_COMPLETED_SUCCESS = "Transfer completed successfully"

    // Account Details
    const val NETWORK_FETCH_FAILED = "Network fetch failed, falling back to cache"
    const val CACHE_READ_FAILED = "Cache read failed"
    const val DEVICE_OFFLINE = "Device offline, using cached data"
    const val CACHE_EMPTY_EXPECTED = "Cache is empty (expected on first run)"

    // ViewModel
    const val ALREADY_LOADING = "Already loading, skipping duplicate request"
    const val OPERATION_IN_PROGRESS_CREATE = "Operation in progress, skipping create goal"
    const val OPERATION_IN_PROGRESS_DELETE = "Operation in progress, skipping delete goal"
    const val OPERATION_IN_PROGRESS_TRANSFER = "Operation in progress, skipping transfer"

    // Repository - Network Errors
    const val ERROR_FETCHING_ACCOUNTS = "Error fetching accounts"
    const val ERROR_FETCHING_BALANCE = "Error fetching balance"
    const val ERROR_FETCHING_TRANSACTIONS = "Error fetching transactions"
    const val ERROR_FETCHING_SAVINGS_GOALS = "Error fetching savings goals"
    const val ERROR_CREATING_SAVINGS_GOAL = "Error creating savings goal"
    const val ERROR_TRANSFERRING_TO_SAVINGS_GOAL = "Error transferring to savings goal"
    const val ERROR_DELETING_SAVINGS_GOAL = "Error deleting savings goal"

    // Repository - Database Errors
    const val ERROR_READING_CACHED_ACCOUNTS = "Error reading cached accounts"
    const val ERROR_READING_CACHED_TRANSACTIONS = "Error reading cached transactions"
    const val ERROR_READING_CACHED_SAVINGS_GOALS = "Error reading cached savings goals"
    const val ERROR_READING_CACHED_BALANCE = "Error reading cached balance"
    const val ERROR_CACHING_ACCOUNTS = "Error caching accounts"
    const val ERROR_CACHING_BALANCE = "Error caching balance"
    const val ERROR_CACHING_TRANSACTIONS = "Error caching transactions"
    const val ERROR_CACHING_SAVINGS_GOALS = "Error caching savings goals"
    const val ERROR_CACHING_SINGLE_SAVINGS_GOAL = "Error caching single savings goal"
    const val ERROR_DELETING_CACHED_SAVINGS_GOAL = "Error deleting cached savings goal"
  }

  // Preview Data
  object PreviewData {
    const val TRANSACTION_1_AMOUNT = "£2.36"
    const val TRANSACTION_1_DIRECTION = "OUT"
    const val TRANSACTION_1_TIME = "2026-01-31"
    const val TRANSACTION_1_NAME = "John Doe"

    const val TRANSACTION_2_AMOUNT = "£8.11"
    const val TRANSACTION_2_DIRECTION = "IN"
    const val TRANSACTION_2_TIME = "2026-01-31"
    const val TRANSACTION_2_NAME = "Gary Doe"

    const val TRANSACTION_3_AMOUNT = "£7.44"
    const val TRANSACTION_3_DIRECTION = "OUT"
    const val TRANSACTION_3_TIME = "2023-06-01"
    const val TRANSACTION_3_NAME = "Jamie Oliver"

    const val TRANSACTION_4_AMOUNT = "£9.12"
    const val TRANSACTION_4_DIRECTION = "OUT"
    const val TRANSACTION_4_TIME = "2023-06-01"
    const val TRANSACTION_4_NAME = "John Oliver"

    const val TRANSACTION_5_AMOUNT = "£2.34"
    const val TRANSACTION_5_DIRECTION = "OUT"
    const val TRANSACTION_5_TIME = "2023-06-01"
    const val TRANSACTION_5_NAME = "John Bishop"
  }
}
