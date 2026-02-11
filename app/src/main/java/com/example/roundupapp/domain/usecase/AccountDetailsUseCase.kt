package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DataSource
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.Constants.DEFAULT_BALANCE
import com.example.roundupapp.utils.Constants.ERROR_CACHE_EMPTY_FIRST_RUN
import com.example.roundupapp.utils.Constants.ERROR_NO_ACCOUNTS_FOUND
import com.example.roundupapp.utils.Constants.ERROR_UNEXPECTED_ACCOUNT_DETAILS
import com.example.roundupapp.utils.Constants.LogMessages.CACHE_EMPTY_EXPECTED
import com.example.roundupapp.utils.Constants.LogMessages.CACHE_READ_FAILED
import com.example.roundupapp.utils.Constants.LogMessages.DEVICE_OFFLINE
import com.example.roundupapp.utils.Constants.LogMessages.NETWORK_FETCH_FAILED
import com.example.roundupapp.utils.Constants.OFFLINE_NO_NETWORK
import javax.inject.Inject

/**
 * Loads account details from network and falls back to cache on failure
 * Coordinates fetching accounts, transactions, savings goals, and balance
 */
class AccountDetailsUseCase @Inject constructor(
  private val repository: RoundUpRepository,
  private val connectivityChecker: NetworkConnectivityChecker
) {
  companion object {
    private val TAG = AccountDetailsUseCase::class.java.simpleName
  }

  suspend operator fun invoke(): Result<AccountDetails> {
    return try {
      val isOnline = connectivityChecker.isNetworkAvailable()

      if (isOnline) {
        loadWithNetworkFirst()
      } else {
        loadOffline()
      }
    } catch (e: Exception) {
      Log.e(TAG, ERROR_UNEXPECTED_ACCOUNT_DETAILS, e)
      Result.failure(e)
    }
  }

  private suspend fun loadWithNetworkFirst(): Result<AccountDetails> {
    // Try network first
    return when (val networkResult = fetchFromNetwork()) {
      is DataResult.Success -> {
        Result.success(networkResult.data.copy(dataSource = DataSource.NETWORK))
      }
      is DataResult.Error -> {
        // Network failed, fallback to cache
        Log.w(TAG, NETWORK_FETCH_FAILED, networkResult.exception)
        loadFromCacheWithFallback(networkResult.exception)
      }
    }
  }

  private suspend fun loadOffline(): Result<AccountDetails> {
    Log.d(TAG, DEVICE_OFFLINE)
    return loadFromCacheWithFallback(OfflineException(OFFLINE_NO_NETWORK))
  }

  private suspend fun loadFromCacheWithFallback(
    originalError: Exception
  ): Result<AccountDetails> {
    return when (val cacheResult = fetchFromCache()) {
      is DataResult.Success -> {
        val data = cacheResult.data

        // Check if cache has actual data or is empty
        if (data.accounts.isEmpty()) {
          Log.d(TAG, ERROR_CACHE_EMPTY_FIRST_RUN)
          Result.failure(originalError) // Return original network error
        } else {
          Result.success(data.copy(dataSource = DataSource.CACHE))
        }
      }
      is DataResult.Error -> {
        Log.e(TAG, CACHE_READ_FAILED, cacheResult.exception)
        Result.failure(originalError) // Return original network error
      }
    }
  }

  private suspend fun fetchFromNetwork(): DataResult<AccountDetails> {
    // Fetch accounts first
    val accountsResult = repository.getAccountsWithResult()
    if (accountsResult is DataResult.Error) {
      return DataResult.Error(accountsResult.exception)
    }

    val accounts = (accountsResult as DataResult.Success).data
    if (accounts.isEmpty()) {
      return DataResult.Error(Exception(ERROR_NO_ACCOUNTS_FOUND))
    }

    val account = accounts.first()
    val accountUid = account.accountUid
    val categoryUid = account.defaultCategory

    // Fetch all other data
    val transactionsResult = repository.getTransactionsWithResult(accountUid, categoryUid)
    val savingsGoalsResult = repository.getSavingsGoalsWithResult(accountUid)
    val balanceResult = repository.getBalanceWithResult(accountUid)

    // If all secondary calls fail, return error
    if (transactionsResult is DataResult.Error &&
      savingsGoalsResult is DataResult.Error &&
      balanceResult is DataResult.Error
    ) {
      return DataResult.Error(transactionsResult.exception)
    }

    // Extract successful data
    val transactions = when (transactionsResult) {
      is DataResult.Success -> transactionsResult.data
      is DataResult.Error -> emptyList()
    }

    val savingsGoals = when (savingsGoalsResult) {
      is DataResult.Success -> savingsGoalsResult.data
      is DataResult.Error -> emptyList()
    }

    val balance = when (balanceResult) {
      is DataResult.Success -> balanceResult.data.effectiveBalance.gbpUnits
      is DataResult.Error -> DEFAULT_BALANCE
    }

    // Update cache with successful results
    repository.cacheAccounts(accounts)

    if (transactionsResult is DataResult.Success) {
      repository.cacheTransactions(accountUid, transactions)
    }

    if (savingsGoalsResult is DataResult.Success) {
      repository.cacheSavingsGoals(accountUid, savingsGoals)
    }

    if (balanceResult is DataResult.Success) {
      repository.cacheBalance(accountUid, balanceResult.data)
    }

    return DataResult.Success(
      AccountDetails(
        accounts = accounts,
        transactions = transactions,
        savingsGoals = savingsGoals,
        balance = balance,
        dataSource = DataSource.NETWORK
      )
    )
  }

  private suspend fun fetchFromCache(): DataResult<AccountDetails> {
    val accountsResult = repository.getCachedAccounts()

    if (accountsResult is DataResult.Error) {
      return DataResult.Error(accountsResult.exception)
    }

    val accounts = (accountsResult as DataResult.Success).data
    if (accounts.isEmpty()) {
      Log.d(TAG, CACHE_EMPTY_EXPECTED)
      return DataResult.Success(
        AccountDetails(
          accounts = emptyList(),
          transactions = emptyList(),
          savingsGoals = emptyList(),
          balance = DEFAULT_BALANCE,
          dataSource = DataSource.CACHE
        )
      )
    }

    val account = accounts.first()
    val accountUid = account.accountUid

    val transactionsResult = repository.getCachedTransactions(accountUid)
    val savingsGoalsResult = repository.getCachedSavingsGoals(accountUid)
    val balanceResult = repository.getCachedBalance()

    val transactions = when (transactionsResult) {
      is DataResult.Success -> transactionsResult.data
      is DataResult.Error -> emptyList()
    }

    val savingsGoals = when (savingsGoalsResult) {
      is DataResult.Success -> savingsGoalsResult.data
      is DataResult.Error -> emptyList()
    }

    val balance = when (balanceResult) {
      is DataResult.Success -> balanceResult.data?.effectiveBalance?.gbpUnits ?: DEFAULT_BALANCE
      is DataResult.Error -> DEFAULT_BALANCE
    }

    return DataResult.Success(
      AccountDetails(
        accounts = accounts,
        transactions = transactions,
        savingsGoals = savingsGoals,
        balance = balance,
        dataSource = DataSource.CACHE
      )
    )
  }
}
