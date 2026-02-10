package com.example.roundupapp.data.repository

import android.util.Log
import com.example.roundupapp.BuildConfig
import com.example.roundupapp.data.NetworkResult
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
import com.example.roundupapp.domain.models.AccountDetails
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
import com.example.roundupapp.utils.Constants.REPO_CURRENCY_GBP
import com.example.roundupapp.utils.Constants.REPO_DEFAULT_BALANCE
import com.example.roundupapp.utils.Constants.REPO_NO_ACCOUNTS_IN_CACHE
import com.example.roundupapp.utils.Constants.REPO_ROUND_UP_TRANSFER_REFERENCE
import com.example.roundupapp.utils.toGbp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Implementation of the [RoundUpRepository] that uses a local database as a cache
 * and a remote API as the single source of truth
 */
class RoundUpRepositoryImpl(
  private val database: RoundUpDatabase
) : RoundUpRepository {

  companion object {
    val TAG = RoundUpRepositoryImpl::class.java.simpleName
  }

  // ================================================================================
  // Network Operations
  // ================================================================================
  override suspend fun getAccountsWithResult(): NetworkResult<List<DomainAccount>> =
    withContext(Dispatchers.IO) {
      try {
        val response = retrofitService.getAccounts(BuildConfig.API_KEY)
        NetworkResult.Success(response.toListOfDomainAccounts())
      } catch (e: Exception) {
        NetworkResult.Error(e)
      }
    }

  override suspend fun getBalanceWithResult(accountUid: String): NetworkResult<DomainBalance> =
    withContext(Dispatchers.IO) {
      try {
        val balance = retrofitService.getBalance(BuildConfig.API_KEY, accountUid)
        NetworkResult.Success(balance.toDomainBalance())
      } catch (e: Exception) {
        NetworkResult.Error(e)
      }
    }

  override suspend fun getTransactionsWithResult(
    accountUid: String,
    categoryUid: String
  ): NetworkResult<List<DomainTransaction>> = withContext(Dispatchers.IO) {
    try {
      // Fetch transactions from the last 7 days
      val changesSince = ZonedDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_INSTANT)
      val response = retrofitService.getTransactions(
        BuildConfig.API_KEY,
        accountUid,
        categoryUid,
        changesSince
      )
      NetworkResult.Success(response.toListOfDomainTransactions())
    } catch (e: Exception) {
      NetworkResult.Error(e)
    }
  }

  override suspend fun getSavingsGoalsWithResult(accountUid: String): NetworkResult<List<DomainSavingsGoal>> =
    withContext(Dispatchers.IO) {
      try {
        val goals = retrofitService.getSavingsGoals(BuildConfig.API_KEY, accountUid)
        NetworkResult.Success(goals.toListOfDomainSavingsGoals())
      } catch (e: Exception) {
        NetworkResult.Error(e)
      }
    }

  override suspend fun createSavingsGoalsRequest(
    accountUid: String,
    name: String,
    currency: String,
    amountMinorUnits: Int
  ): CreateSavingsGoalResponse = withContext(Dispatchers.IO) {
    try {
      retrofitService.createSavingsGoal(
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
    } catch (e: Exception) {
      throw e
    }
  }

  override suspend fun transferToSavingsGoalWithResult(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int,
    transferUid: String
  ): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
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
      NetworkResult.Success(true)
    } catch (e: Exception) {
      NetworkResult.Error(e)
    }
  }

  override suspend fun deleteSavingsGoalsWithResult(accountUid: String, savingsGoalUid: String): NetworkResult<Unit> =
    withContext(Dispatchers.IO) {
      try {
        retrofitService.deleteSavingsGoal(
          BuildConfig.API_KEY,
          accountUid,
          savingsGoalUid
        )
        NetworkResult.Success(Unit)
      } catch (e: Exception) {
        NetworkResult.Error(e)
//         TODO: check state exception below
//        throw IllegalStateException(REPO_ERROR_FETCHING_SAVINGS_GOALS)
      }
    }

  // ================================================================================
  // Database Operations - Read
  // ================================================================================
  override suspend fun getCachedAccounts(): List<DomainAccount> = withContext(Dispatchers.IO) {
    database.accountDao().getAll().map { entity ->
      DomainAccount(
        accountUid = entity.accountUid,
        name = entity.name,
        defaultCategory = entity.defaultCategory,
        createdAt = ""
      )
    }
  }

  override suspend fun getCachedTransactions(accountUid: String): List<DomainTransaction> =
    withContext(Dispatchers.IO) {
      database.transactionDao()
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
    }

  override suspend fun getCachedSavingsGoals(accountUid: String): List<DomainSavingsGoal> =
    withContext(Dispatchers.IO) {
      database.savingsGoalDao()
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
    }

  override suspend fun getCachedBalance(): DomainBalance? = withContext(Dispatchers.IO) {
    database.balanceDao()
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
  }


  // ================================================================================
  // Database Operations - Write
  // ================================================================================
  override suspend fun cacheAccounts(accounts: List<DomainAccount>) = withContext(Dispatchers.IO) {
    database.accountDao().insertAll(accounts.map {
      AccountEntity(
        accountUid = it.accountUid,
        name = it.name,
        defaultCategory = it.defaultCategory,
      )
    })
  }

  override suspend fun cacheBalance(
    accountUid: String,
    balance: DomainBalance
  ) = withContext(Dispatchers.IO) {
    database.balanceDao().insert(
      BalanceEntity(
        id = 0,
        accountUid = accountUid,
        effectiveBalanceMinorUnits = balance.effectiveBalance.minorUnits,
        effectiveBalanceCurrency = balance.effectiveBalance.currency
      )
    )
  }

  override suspend fun cacheTransactions(
    accountUid: String,
    transactions: List<DomainTransaction>
  ) = withContext(Dispatchers.IO) {
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
  }

  override suspend fun cacheSavingsGoals(
    accountUid: String,
    savingsGoals: List<DomainSavingsGoal>
  ) = withContext(Dispatchers.IO) {
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
  }

  override suspend fun cacheSavingsGoal(
    accountUid: String,
    goal: DomainSavingsGoal
  ) = withContext(Dispatchers.IO) {
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
  }

  override suspend fun cacheDeleteSavingsGoal(savingsGoalUid: String) = withContext(Dispatchers.IO) {
    database.savingsGoalDao().deleteBySavingsGoalUid(savingsGoalUid)
  }
}
