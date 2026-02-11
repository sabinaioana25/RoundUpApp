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

  // Offline handling
  const val HOME_SCREEN_OFFLINE_ERROR = "No internet connection. Showing cached data."
  const val HOME_SCREEN_OFFLINE_INDICATOR = "Offline mode"
}
