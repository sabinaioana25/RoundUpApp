package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DataSource
import com.example.roundupapp.domain.repository.RoundUpRepository
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
      Log.e(TAG, "Unexpected error loading account details", e)
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
        Log.w(TAG, "Network fetch failed, falling back to cache", networkResult.exception)
        loadFromCacheWithFallback(networkResult.exception)
      }
    }
  }

  private suspend fun loadOffline(): Result<AccountDetails> {
    Log.d(TAG, "Device offline, using cached data")
    return loadFromCacheWithFallback(OfflineException("No network connectivity"))
  }

  private suspend fun loadFromCacheWithFallback(
    originalError: Exception
  ): Result<AccountDetails> {
    return when (val cacheResult = fetchFromCache()) {
      is DataResult.Success -> {
        val data = cacheResult.data
        
        // Check if cache has actual data or is empty
        if (data.accounts.isEmpty()) {
          Log.d(TAG, "Cache is empty (first run)")
          Result.failure(originalError) // Return original network error
        } else {
          Result.success(data.copy(dataSource = DataSource.CACHE))
        }
      }
      is DataResult.Error -> {
        Log.e(TAG, "Cache read failed", cacheResult.exception)
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
      return DataResult.Error(Exception("No accounts found"))
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
      is DataResult.Error -> "0.00"
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
      Log.d(TAG, "Cache is empty (expected on first run)")
      return DataResult.Success(
        AccountDetails(
          accounts = emptyList(),
          transactions = emptyList(),
          savingsGoals = emptyList(),
          balance = "0.00",
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
      is DataResult.Success -> balanceResult.data?.effectiveBalance?.gbpUnits ?: "0.00"
      is DataResult.Error -> "0.00"
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
