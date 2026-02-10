package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.NetworkResult
import com.example.roundupapp.data.repository.RoundUpRepositoryImpl
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.Constants.REPO_DEFAULT_BALANCE
import com.example.roundupapp.utils.Constants.REPO_NO_ACCOUNTS_IN_CACHE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Loads account details from cache and refreshes from network
 * Used to initialize and update account data in the application
 */
class AccountDetailsUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke(): Result<AccountDetails> =
    try {
      val networkResult = fetchFromNetwork()

      when {
        networkResult.isSuccess -> networkResult
        else -> {
          // network failed, try cache
          val cached = getCachedAccountDetails()
          if (cached != null) {
            Result.success(cached)
          } else {
            networkResult
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error in AccountDetailsUseCase", e)
      Result.failure(e)
    }

  private suspend fun fetchFromNetwork(): Result<AccountDetails> {
    // fetch accounts first
    val accountsResult = repository.getAccountsWithResult()

    if (accountsResult is NetworkResult.Error) {
      return Result.failure(accountsResult.exception)
    }

    val accounts = (accountsResult as NetworkResult.Success).data
    if (accounts.isEmpty()) {
      return Result.failure(Exception("No accounts found"))
    }

    val account = accounts.first()

    // fetch other data in parallel
    val transactionResult = repository.getTransactionsWithResult(account.accountUid, account.defaultCategory)
    val savingsGoalResult = repository.getSavingsGoalsWithResult(account.accountUid)
    val balanceResult = repository.getBalanceWithResult(account.accountUid)

    // If all fail return error
    if (transactionResult is NetworkResult.Error &&
      savingsGoalResult is NetworkResult.Error &&
      balanceResult is NetworkResult.Error
    ) {
      return Result.failure(transactionResult.exception)
    }

    // Extract data or use cache fallback
    val cached = getCachedAccountDetails()
    val transactions = when (transactionResult) {
      is NetworkResult.Success -> transactionResult.data
      is NetworkResult.Error -> cached?.transactions ?: emptyList()
    }

    val savingsGoals = when (savingsGoalResult) {
      is NetworkResult.Success -> savingsGoalResult.data
      is NetworkResult.Error -> cached?.savingsGoals ?: emptyList()
    }

    val balance = when (balanceResult) {
      is NetworkResult.Success -> balanceResult.data.effectiveBalance.gbpUnits
      is NetworkResult.Error -> cached?.balance ?: "0.00"
    }

    // Update cache with results
    repository.cacheAccounts(accounts)

    if (transactionResult is NetworkResult.Success) {
      repository.cacheTransactions(account.accountUid, transactions)
    }

    if (savingsGoalResult is NetworkResult.Success) {
      repository.cacheSavingsGoals(account.accountUid, savingsGoals)
    }

    if (balanceResult is NetworkResult.Success) {
      repository.cacheBalance(account.accountUid, balanceResult.data)
    }
    return Result.success(
      AccountDetails(
        accounts = accounts,
        transactions = transactions,
        savingsGoals = savingsGoals,
        balance = balance
      )
    )
  }

  private suspend fun getCachedAccountDetails(): AccountDetails? = withContext(Dispatchers.IO) {
    val accounts = repository.getCachedAccounts()

    if (accounts.isEmpty()) {
      Log.d(RoundUpRepositoryImpl.TAG, REPO_NO_ACCOUNTS_IN_CACHE)
      return@withContext null
    }

    val cachedAccount = accounts.first()
    val cachedTransactions = repository.getCachedTransactions(cachedAccount.accountUid)
    val cachedSavingsGoals = repository.getCachedSavingsGoals(cachedAccount.accountUid)

    val cachedBalance = repository.getCachedBalance()
    val balance = cachedBalance?.effectiveBalance?.gbpUnits ?: REPO_DEFAULT_BALANCE

    AccountDetails(
      accounts = accounts,
      transactions = cachedTransactions,
      savingsGoals = cachedSavingsGoals,
      balance = balance
    )
  }

  companion object {
    private val TAG = AccountDetailsUseCase::class.java.simpleName
  }
}
