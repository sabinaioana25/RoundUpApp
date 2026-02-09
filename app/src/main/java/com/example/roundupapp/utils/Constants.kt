package com.example.roundupapp.utils

object Constants {

    // HomeScreen & VM
    const val HOME_SCREEN_ERROR_EMPTY_STATE = "No data to display"
    const val HOME_SCREEN_LOADING_MESSAGE = "Loading your account..."
    const val HOME_SCREEN_ERROR_LOADING_INITIAL_DATA = "Error loading initial data"
    const val HOME_SCREEN_ALREADY_LOADING = "Already loading"
    const val HOME_SCREEN_ERROR_REFRESHING = "Error refreshing data"
    const val HOME_SCREEN_PULL_TO_REFRESH = "Pull down to refresh"
    const val HOME_SCREEN_ROUND_UP_BUTTON = "Round Up"
    const val HOME_SCREEN_REFRESH_ICON = "Refresh"
    const val HOME_SCREEN_BALANCE = "Balance"
    const val HOME_SCREEN_SAMPLE_BALANCE = "£1000.00"
    const val HOME_SCREEN_NO_ROUND_UP_AVAILABLE = "No round up available to transfer"
    // Goals card
    const val GOALS_CARD_COMPOSABLE_NAME_GOAL = "Goal"
    const val GOALS_CARD_COMPOSABLE_TARGET = "Target"
    const val GOALS_NAME_BLANK_WARNING = "Name cannot be blank"
    const val GOALS_VALUE_MUST_BE_GREATER_THAN_ZERO = "Value must be greater than zero"
    const val GOALS_PLEASE_WAIT_DELETE_ACTION = "Please wait..."
    const val GOALS_ERROR_CREATING = "Error creating goal"
    const val GOALS_FAILURE_CREATING = "Failed to create goal"
    const val GOALS_ERROR_DELETING = "Error deleting goal"
    const val GOALS_FAILURE_DELETING = "Failed to delete goal"
    const val GOALS_CARD_COMPOSABLE_TEXT_TRANSACTIONS = "Transactions"
    const val GOALS_CREATE_GOAL_BUTTON = "Create Goal"
    const val GOALS_TOTAL_SAVED = "Total saved: "
    const val GOALS_TARGET = "Target: "
    const val GOALS_ROUND_UP_AVAILABLE = "Round-up available"
    const val GOALS_TRANSFERRING_BUTTON = "Transferring"
    const val GOALS_TRANSFER_BUTTON = "Transfer"
    const val GOALS_DELETING_BUTTON = "Deleting"
    const val GOALS_DELETE_GOAL_BUTTON = "Delete Goal"

    // Alert dialog
    const val ALERT_DIALOG_COMPOSABLE_DIALOG_TITLE = "Create a new Savings Goal"
    const val ALERT_DIALOG_COMPOSABLE_BUTTON_CREATE = "Create"
    const val ALERT_DIALOG_COMPOSABLE_BUTTON_CANCEL = "Cancel"
    const val ALERT_WAIT_FOR_ACTION_TO_COMPLETE = "Wait to complete"
    const val ALERT_TRANSFER_FAILED = "Transfer failed. Please try again"
    const val ALERT_TRANSFERRING_ERROR= "Error transferring to savings goal"
    const val ALERT_TRANSFERRING_FAILURE = "Failed to transfer funds"
    const val ALERT_DIALOG_CURRENCY_PREFIX = "£"
    const val ALERT_DIALOG_DECIMAL_REGEX = "^\\d*\\.?\\d{0,2}$"
    const val ALERT_SAMPLE_GOAL_NAME = "Holiday Fund"

    //RoundUpRepositoryImpl
    const val REPO_NO_ACCOUNTS_IN_CACHE = "No accounts in cache"
    const val REPO_CURRENCY_GBP = "GBP"
    const val REPO_DEFAULT_BALANCE = "0.00"
    const val REPO_ERROR_FETCHING_ACCOUNTS = "Failed to fetch accounts: "
    const val REPO_NO_ACCOUNTS_FOUND = "No accounts found"
    const val REPO_ERROR_FETCHING_TRANSACTIONS = "Failed to fetch transactions, keeping cached data"
    const val REPO_ERROR_FETCHING_SAVINGS_GOALS = "Failed to fetch savings goals, keeping cached data"
    const val REPO_ERROR_FETCHING_BALANCE = "Failed to fetch balance, keeping cached data"
    const val REPO_SAVINGS_GOAL_STATE_ACTIVE = "ACTIVE"
    const val REPO_ROUND_UP_TRANSFER_REFERENCE = "Round-up transfer"
    const val REPO_FETCHING_ACCOUNTS_ERROR = "Error fetching accounts"
    const val REPO_FETCHING_BALANCE_ERROR = "Error fetching balance"
    const val REPO_FETCHING_TRANSACTIONS_ERROR = "Error fetching transactions"
    const val REPO_FETCHING_SAVINGS_GOALS_ERROR = "Error fetching savings goals"

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
}
