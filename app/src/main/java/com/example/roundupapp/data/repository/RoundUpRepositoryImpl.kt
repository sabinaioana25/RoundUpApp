package com.example.roundupapp.data.repository

import android.util.Log
import com.example.roundupapp.BuildConfig
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.data.database.RoundUpDatabase
import com.example.roundupapp.data.database.entities.AccountEntity
import com.example.roundupapp.data.database.entities.BalanceEntity
import com.example.roundupapp.data.database.entities.SavingsGoalEntity
import com.example.roundupapp.data.database.entities.TransactionEntity
import com.example.roundupapp.data.network.RoundUpApi.retrofitService
import com.example.roundupapp.data.network.dto.savingsgoals.CreateAmountTransferRequest
import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalRequest
import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalResponse
import com.example.roundupapp.data.network.dto.transactions.NetworkAmount
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainBalance
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.domain.models.toDomainBalance
import com.example.roundupapp.domain.models.toListOfDomainAccounts
import com.example.roundupapp.domain.models.toListOfDomainSavingsGoals
import com.example.roundupapp.domain.models.toListOfDomainTransactions
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.Constants.ALERT_TRANSFER_FAILURE_EXCEPTION
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_CACHING_ACCOUNTS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_CACHING_BALANCE
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_CACHING_SAVINGS_GOALS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_CACHING_SINGLE_SAVINGS_GOAL
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_CACHING_TRANSACTIONS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_CREATING_SAVINGS_GOAL
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_DELETING_CACHED_SAVINGS_GOAL
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_DELETING_SAVINGS_GOAL
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_FETCHING_ACCOUNTS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_FETCHING_BALANCE
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_FETCHING_SAVINGS_GOALS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_FETCHING_TRANSACTIONS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_READING_CACHED_ACCOUNTS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_READING_CACHED_BALANCE
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_READING_CACHED_SAVINGS_GOALS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_READING_CACHED_TRANSACTIONS
import com.example.roundupapp.utils.Constants.LogMessages.ERROR_TRANSFERRING_TO_SAVINGS_GOAL
import com.example.roundupapp.utils.Constants.REPO_CURRENCY_GBP
import com.example.roundupapp.utils.Constants.REPO_ROUND_UP_TRANSFER_REFERENCE

import com.example.roundupapp.utils.toGbp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Implementation of the [RoundUpRepository] that uses a local database as a cache
 * and a remote API as the single source of truth
 * All methods wrap results in DataResult for consistent error handling
 */
class RoundUpRepositoryImpl(
  private val database: RoundUpDatabase
) : RoundUpRepository {

  companion object {
    val TAG: String = RoundUpRepositoryImpl::class.java.simpleName
  }

  // ================================================================================
  // Network Operations
  // ================================================================================

  override suspend fun getAccountsWithResult(): DataResult<List<DomainAccount>> =
    withContext(Dispatchers.IO) {
      try {
        val response = retrofitService.getAccounts(BuildConfig.API_KEY)
        DataResult.Success(response.toListOfDomainAccounts())
      } catch (e: Exception) {
        Log.e(TAG, ERROR_FETCHING_ACCOUNTS, e)
        DataResult.Error(e)
      }
    }

  override suspend fun getBalanceWithResult(accountUid: String): DataResult<DomainBalance> =
    withContext(Dispatchers.IO) {
      try {
        val balance = retrofitService.getBalance(BuildConfig.API_KEY, accountUid)
        DataResult.Success(balance.toDomainBalance())
      } catch (e: Exception) {
        Log.e(TAG, ERROR_FETCHING_BALANCE, e)
        DataResult.Error(e)
      }
    }

  override suspend fun getTransactionsWithResult(
    accountUid: String,
    categoryUid: String
  ): DataResult<List<DomainTransaction>> = withContext(Dispatchers.IO) {
    try {
      val changesSince = ZonedDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_INSTANT)
      val response = retrofitService.getTransactions(
        BuildConfig.API_KEY,
        accountUid,
        categoryUid,
        changesSince
      )
      DataResult.Success(response.toListOfDomainTransactions())
    } catch (e: Exception) {
      Log.e(TAG, ERROR_FETCHING_TRANSACTIONS, e)
      DataResult.Error(e)
    }
  }

  override suspend fun getSavingsGoalsWithResult(accountUid: String): DataResult<List<DomainSavingsGoal>> =
    withContext(Dispatchers.IO) {
      try {
        val goals = retrofitService.getSavingsGoals(BuildConfig.API_KEY, accountUid)
        DataResult.Success(goals.toListOfDomainSavingsGoals())
      } catch (e: Exception) {
        Log.e(TAG, ERROR_FETCHING_SAVINGS_GOALS, e)
        DataResult.Error(e)
      }
    }

  override suspend fun createSavingsGoalsRequest(
    accountUid: String,
    name: String,
    currency: String,
    amountMinorUnits: Int
  ): DataResult<CreateSavingsGoalResponse> = withContext(Dispatchers.IO) {
    try {
      val response = retrofitService.createSavingsGoal(
        BuildConfig.API_KEY,
        accountUid,
        CreateSavingsGoalRequest(
          name = name,
          currency = currency,
          target = NetworkAmount(
            currency = currency,
            minorUnits = amountMinorUnits
          )
        )
      )
      DataResult.Success(response)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_CREATING_SAVINGS_GOAL, e)
      DataResult.Error(e)
    }
  }

  override suspend fun transferToSavingsGoalWithResult(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int,
    transferUid: String
  ): DataResult<Boolean> = withContext(Dispatchers.IO) {
    try {
      val requestBody = CreateAmountTransferRequest(
        amount = NetworkAmount(
          currency = REPO_CURRENCY_GBP,
          minorUnits = amountMinorUnits
        ),
        reference = REPO_ROUND_UP_TRANSFER_REFERENCE
      )

      val response = retrofitService.transferMoneyToSavingsGoal(
        BuildConfig.API_KEY,
        accountUid,
        savingsGoalUid,
        transferUid,
        body = requestBody
      )

      if (response.transferUid != transferUid) {
        throw IllegalStateException(ALERT_TRANSFER_FAILURE_EXCEPTION)
      }
      DataResult.Success(true)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_TRANSFERRING_TO_SAVINGS_GOAL, e)
      DataResult.Error(e)
    }
  }

  override suspend fun deleteSavingsGoalsWithResult(
    accountUid: String,
    savingsGoalUid: String
  ): DataResult<Unit> = withContext(Dispatchers.IO) {
    try {
      retrofitService.deleteSavingsGoal(
        BuildConfig.API_KEY,
        accountUid,
        savingsGoalUid
      )
      DataResult.Success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_DELETING_SAVINGS_GOAL, e)
      DataResult.Error(e)
    }
  }

  // ================================================================================
  // Database Operations - Read
  // ================================================================================

  override suspend fun getCachedAccounts(): DataResult<List<DomainAccount>> = withContext(Dispatchers.IO) {
    try {
      val accounts = database.accountDao().getAll().map { entity ->
        DomainAccount(
          accountUid = entity.accountUid,
          name = entity.name,
          defaultCategory = entity.defaultCategory,
          createdAt = ""
        )
      }
      DataResult.Success(accounts)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_READING_CACHED_ACCOUNTS, e)
      DataResult.Error(e)
    }
  }

  override suspend fun getCachedTransactions(accountUid: String): DataResult<List<DomainTransaction>> =
    withContext(Dispatchers.IO) {
      try {
        val transactions = database.transactionDao()
          .getByAccount(accountUid)
          .map { entity ->
            DomainTransaction(
              direction = entity.direction,
              amount = DomainAmount(
                currency = REPO_CURRENCY_GBP,
                minorUnits = entity.amountMinorUnits,
                gbpUnits = entity.amountMinorUnits.toGbp()
              ),
              transactionTime = entity.transactionDate,
              counterPartyName = entity.counterPartyName
            )
          }
        DataResult.Success(transactions)
      } catch (e: Exception) {
        Log.e(TAG, ERROR_READING_CACHED_TRANSACTIONS, e)
        DataResult.Error(e)
      }
    }

  override suspend fun getCachedSavingsGoals(accountUid: String): DataResult<List<DomainSavingsGoal>> =
    withContext(Dispatchers.IO) {
      try {
        val goals = database.savingsGoalDao()
          .getByAccountSavingsGoal(accountUid)
          .map { entity ->
            DomainSavingsGoal(
              savingsGoalUid = entity.savingsGoalUid,
              name = entity.name,
              targetAmount = DomainAmount(
                currency = entity.targetAmountCurrency,
                minorUnits = entity.targetAmountMinorUnits,
                gbpUnits = entity.targetAmountMinorUnits.toGbp()
              ),
              totalSaved = DomainAmount(
                currency = entity.totalSavedCurrency,
                minorUnits = entity.totalSavedMinorUnits,
                gbpUnits = entity.totalSavedMinorUnits.toGbp()
              ),
              state = entity.state,
            )
          }
        DataResult.Success(goals)
      } catch (e: Exception) {
        Log.e(TAG, ERROR_READING_CACHED_SAVINGS_GOALS, e)
        DataResult.Error(e)
      }
    }

  override suspend fun getCachedBalance(): DataResult<DomainBalance?> = withContext(Dispatchers.IO) {
    try {
      val balance = database.balanceDao()
        .get()
        ?.let {
          DomainBalance(
            effectiveBalance = DomainAmount(
              currency = it.effectiveBalanceCurrency,
              minorUnits = it.effectiveBalanceMinorUnits,
              gbpUnits = it.effectiveBalanceMinorUnits.toGbp()
            )
          )
        }
      DataResult.Success(balance)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_READING_CACHED_BALANCE, e)
      DataResult.Error(e)
    }
  }

  // ================================================================================
  // Database Operations - Write
  // ================================================================================

  override suspend fun cacheAccounts(accounts: List<DomainAccount>): DataResult<Unit> =
    withContext(Dispatchers.IO) {
      try {
        database.accountDao().insertAll(accounts.map {
          AccountEntity(
            accountUid = it.accountUid,
            name = it.name,
            defaultCategory = it.defaultCategory,
          )
        })
        DataResult.Success(Unit)
      } catch (e: Exception) {
        Log.e(TAG, ERROR_CACHING_ACCOUNTS, e)
        DataResult.Error(e)
      }
    }

  override suspend fun cacheBalance(
    accountUid: String,
    balance: DomainBalance
  ): DataResult<Unit> = withContext(Dispatchers.IO) {
    try {
      database.balanceDao().insert(
        BalanceEntity(
          id = 0,
          accountUid = accountUid,
          effectiveBalanceMinorUnits = balance.effectiveBalance.minorUnits,
          effectiveBalanceCurrency = balance.effectiveBalance.currency
        )
      )
      DataResult.Success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_CACHING_BALANCE, e)
      DataResult.Error(e)
    }
  }

  override suspend fun cacheTransactions(
    accountUid: String,
    transactions: List<DomainTransaction>
  ): DataResult<Unit> = withContext(Dispatchers.IO) {
    try {
      database.transactionDao().deleteByAccount(accountUid)
      database.transactionDao().insertAll(transactions.map {
        TransactionEntity(
          accountUid = accountUid,
          direction = it.direction,
          amountMinorUnits = it.amount.minorUnits,
          transactionDate = it.transactionTime,
          counterPartyName = it.counterPartyName,
        )
      })
      DataResult.Success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_CACHING_TRANSACTIONS, e)
      DataResult.Error(e)
    }
  }

  override suspend fun cacheSavingsGoals(
    accountUid: String,
    savingsGoals: List<DomainSavingsGoal>
  ): DataResult<Unit> = withContext(Dispatchers.IO) {
    try {
      database.savingsGoalDao().deleteByAccount(accountUid)
      database.savingsGoalDao().insertAll(savingsGoals.map {
        SavingsGoalEntity(
          savingsGoalUid = it.savingsGoalUid,
          accountUid = accountUid,
          name = it.name,
          targetAmountMinorUnits = it.targetAmount.minorUnits,
          targetAmountCurrency = it.targetAmount.currency,
          totalSavedMinorUnits = it.totalSaved.minorUnits,
          totalSavedCurrency = it.totalSaved.currency,
          state = it.state,
        )
      })
      DataResult.Success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_CACHING_SAVINGS_GOALS, e)
      DataResult.Error(e)
    }
  }

  override suspend fun cacheSavingsGoal(
    accountUid: String,
    goal: DomainSavingsGoal
  ): DataResult<Unit> = withContext(Dispatchers.IO) {
    try {
      database.savingsGoalDao().insertAll(
        listOf(
          SavingsGoalEntity(
            savingsGoalUid = goal.savingsGoalUid,
            accountUid = accountUid,
            name = goal.name,
            targetAmountMinorUnits = goal.targetAmount.minorUnits,
            targetAmountCurrency = goal.targetAmount.currency,
            totalSavedMinorUnits = goal.totalSaved.minorUnits,
            totalSavedCurrency = goal.totalSaved.currency,
            state = goal.state,
          )
        )
      )
      DataResult.Success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, ERROR_CACHING_SINGLE_SAVINGS_GOAL, e)
      DataResult.Error(e)
    }
  }

  override suspend fun cacheDeleteSavingsGoal(savingsGoalUid: String): DataResult<Unit> =
    withContext(Dispatchers.IO) {
      try {
        database.savingsGoalDao().deleteBySavingsGoalUid(savingsGoalUid)
        DataResult.Success(Unit)
      } catch (e: Exception) {
        Log.e(TAG, ERROR_DELETING_CACHED_SAVINGS_GOAL, e)
        DataResult.Error(e)
      }
    }
}
