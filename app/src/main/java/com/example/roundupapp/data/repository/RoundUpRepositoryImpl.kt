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
import com.example.roundupapp.data.network.dto.savingsgoals.NetworkSavingsGoal
import com.example.roundupapp.data.network.dto.transactions.NetworkAmount
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainBalance
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.domain.models.toDomainBalance
import com.example.roundupapp.domain.models.toDomainSavingsGoal
import com.example.roundupapp.domain.models.toListOfDomainAccounts
import com.example.roundupapp.domain.models.toListOfDomainSavingsGoals
import com.example.roundupapp.domain.models.toListOfDomainTransactions
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.Constants.ALERT_TRANSFERRING_ERROR
import com.example.roundupapp.utils.Constants.GOALS_ERROR_CREATING
import com.example.roundupapp.utils.Constants.GOALS_ERROR_DELETING
import com.example.roundupapp.utils.Constants.REPO_CURRENCY_GBP
import com.example.roundupapp.utils.Constants.REPO_DEFAULT_BALANCE
import com.example.roundupapp.utils.Constants.REPO_ERROR_FETCHING_ACCOUNTS
import com.example.roundupapp.utils.Constants.REPO_ERROR_FETCHING_BALANCE
import com.example.roundupapp.utils.Constants.REPO_ERROR_FETCHING_SAVINGS_GOALS
import com.example.roundupapp.utils.Constants.REPO_ERROR_FETCHING_TRANSACTIONS
import com.example.roundupapp.utils.Constants.REPO_FETCHING_ACCOUNTS_ERROR
import com.example.roundupapp.utils.Constants.REPO_FETCHING_BALANCE_ERROR
import com.example.roundupapp.utils.Constants.REPO_FETCHING_SAVINGS_GOALS_ERROR
import com.example.roundupapp.utils.Constants.REPO_FETCHING_TRANSACTIONS_ERROR
import com.example.roundupapp.utils.Constants.REPO_NO_ACCOUNTS_FOUND
import com.example.roundupapp.utils.Constants.REPO_NO_ACCOUNTS_IN_CACHE
import com.example.roundupapp.utils.Constants.REPO_ROUND_UP_TRANSFER_REFERENCE
import com.example.roundupapp.utils.Constants.REPO_SAVINGS_GOAL_STATE_ACTIVE
import com.example.roundupapp.utils.toGbp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

  private val _accountDetails = MutableStateFlow<AccountDetails?>(null)
  override val accountDetails: StateFlow<AccountDetails?> = _accountDetails.asStateFlow()

  companion object {
    private val TAG = RoundUpRepositoryImpl::class.java.simpleName
  }

  override suspend fun loadFromCache() {
    val accounts = database.accountDao().getAll().map { entity ->
      DomainAccount(
        accountUid = entity.accountUid,
        name = entity.name,
        defaultCategory = entity.defaultCategory,
        createdAt = ""
      )
    }
    if (accounts.isEmpty()) {
      Log.d(TAG, REPO_NO_ACCOUNTS_IN_CACHE)
      return
    }

    val account = accounts[0]

    val transactions = database.transactionDao()
      .getByAccount(account.accountUid)
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

    val savingsGoals = database.savingsGoalDao()
      .getByAccountSavingsGoal(account.accountUid)
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

    val balanceEntity = database.balanceDao().get()
    val balance = balanceEntity?.effectiveBalanceMinorUnits?.toGbp() ?: REPO_DEFAULT_BALANCE

    _accountDetails.value = AccountDetails(
      accounts = accounts,
      transactions = transactions,
      savingsGoals = savingsGoals,
      balance = balance
    )
  }

  override suspend fun refreshFromNetwork() {
    val currentRoundUpAmount = _accountDetails.value?.roundUpAmount ?: 0

    val accountsResult = getAccountsWithResult()
    if (accountsResult is NetworkResult.Error) {
      Log.e(TAG, "$REPO_ERROR_FETCHING_ACCOUNTS${accountsResult.exception.message}")
      return
    }

    val accounts = (accountsResult as NetworkResult.Success).data
    if (accounts.isEmpty()) {
      Log.w(TAG, REPO_NO_ACCOUNTS_FOUND)
      return
    }

    val account = accounts[0]

    val transactionsResult = getTransactionsWithResult(account.accountUid, account.defaultCategory)
    val savingsGoalsResult = getSavingsGoalsWithResult(account.accountUid)
    val balanceResult = getBalanceWithResult(account.accountUid)

    // Only update if there is data (don't clear on errors)
    val transactions = when (transactionsResult) {
      is NetworkResult.Success -> transactionsResult.data
      is NetworkResult.Error -> {
        Log.e(TAG, REPO_ERROR_FETCHING_TRANSACTIONS)
        _accountDetails.value?.transactions ?: emptyList()
      }
    }

    val savingsGoals = when (savingsGoalsResult) {
      is NetworkResult.Success -> savingsGoalsResult.data
      is NetworkResult.Error -> {
        Log.e(TAG, REPO_ERROR_FETCHING_SAVINGS_GOALS)
        _accountDetails.value?.savingsGoals ?: emptyList()
      }
    }

    val balance = when (balanceResult) {
      is NetworkResult.Success -> balanceResult.data.effectiveBalance.minorUnits.toGbp()
      is NetworkResult.Error -> {
        Log.e(TAG, REPO_ERROR_FETCHING_BALANCE)
        _accountDetails.value?.balance ?: REPO_DEFAULT_BALANCE
      }
    }

    if (transactionsResult is NetworkResult.Success ||
      savingsGoalsResult is NetworkResult.Success ||
      balanceResult is NetworkResult.Success
    ) {
      saveToCache(
        accounts,
        if (transactionsResult is NetworkResult.Success) transactions else emptyList(),
        if (savingsGoalsResult is NetworkResult.Success) savingsGoals else emptyList(),
        account,
        if (balanceResult is NetworkResult.Success) balanceResult.data else null,
        shouldUpdateTransactions = transactionsResult is NetworkResult.Success,
        shouldUpdateGoals = savingsGoalsResult is NetworkResult.Success
      )
    }

    _accountDetails.value = AccountDetails(
      accounts = accounts,
      transactions = transactions,
      savingsGoals = savingsGoals,
      balance = balance,
      roundUpAmount = currentRoundUpAmount
    )
  }

  private suspend fun saveToCache(
    accounts: List<DomainAccount>,
    transactions: List<DomainTransaction>,
    savingsGoals: List<DomainSavingsGoal>,
    account: DomainAccount,
    balance: DomainBalance?,
    shouldUpdateTransactions: Boolean = true,
    shouldUpdateGoals: Boolean = true
  ) {
    database.accountDao().insertAll(accounts.map { 
      AccountEntity(
        accountUid = it.accountUid,
        name = it.name,
        defaultCategory = it.defaultCategory,
      )
    })

    if (shouldUpdateTransactions) {
      database.transactionDao().deleteByAccount(account.accountUid)
      database.transactionDao().insertAll(transactions.map {
        TransactionEntity(
          accountUid = account.accountUid,
          direction = it.direction,
          amountMinorUnits = it.amount.minorUnits,
          transactionDate = it.transactionTime,
          counterPartyName = it.counterPartyName,
        )
      })
    }

    if (shouldUpdateGoals) {
      database.savingsGoalDao().deleteByAccount(account.accountUid)
      database.savingsGoalDao().insertAll(savingsGoals.map {
        SavingsGoalEntity(
          savingsGoalUid = it.savingsGoalUid,
          accountUid = account.accountUid,
          name = it.name,
          targetAmountMinorUnits = it.targetAmount.minorUnits,
          targetAmountCurrency = it.targetAmount.currency,
          totalSavedMinorUnits = it.totalSaved.minorUnits,
          totalSavedCurrency = it.totalSaved.currency,
          state = it.state,
        )
      })
    }

    balance?.let {
      database.balanceDao().insert(
        BalanceEntity(
          id = 0,
          accountUid = account.accountUid,
          effectiveBalanceMinorUnits = it.effectiveBalance.minorUnits,
          effectiveBalanceCurrency = it.effectiveBalance.currency
        )
      )
    }
  }

  override suspend fun addSavingsGoal(goal: DomainSavingsGoal, accountUid: String) =
    withContext(Dispatchers.IO) {
      _accountDetails.update { it?.copy(savingsGoals = it.savingsGoals + goal) }

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

  override suspend fun removeSavingsGoal(savingsGoalUid: String) = withContext(Dispatchers.IO) {
    _accountDetails.update { it?.copy(savingsGoals = it.savingsGoals.filter { g -> g.savingsGoalUid != savingsGoalUid }) }
    database.savingsGoalDao().deleteBySavingsGoalUid(savingsGoalUid)
  }

  override fun setRoundUpAmount(amount: Int) {
    _accountDetails.update { it?.copy(roundUpAmount = amount) }
  }

  override suspend fun createSavingsGoal(
    accountUid: String,
    name: String,
    amountMinorUnits: Int,
    currency: String
  ): DomainSavingsGoal? = withContext(Dispatchers.IO) {
    try {
      val request = CreateSavingsGoalRequest(
        name = name,
        currency = REPO_CURRENCY_GBP,
        target = NetworkAmount(
          currency = REPO_CURRENCY_GBP,
          minorUnits = amountMinorUnits
        )
      )
      val response = retrofitService.createSavingsGoal(
        BuildConfig.API_KEY,
        accountUid,
        body = request,
      )
      response.toDomainSavingsGoal(
        NetworkSavingsGoal(
          savingsGoalUid = response.savingsGoalUid,
          name = name,
          target = request.target,
          totalSaved = NetworkAmount(
            currency = REPO_CURRENCY_GBP,
            minorUnits = 0
          ),
          state = REPO_SAVINGS_GOAL_STATE_ACTIVE
        )
      )
    } catch (e: Exception) {
      Log.e(TAG, GOALS_ERROR_CREATING, e)
      null
    }
  }

  override suspend fun deleteSavingsGoal(
    accountUid: String,
    savingsGoalUid: String
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val response = retrofitService.deleteSavingsGoal(
        BuildConfig.API_KEY,
        accountUid,
        savingsGoalUid
      )
      response.isSuccessful
    } catch (e: Exception) {
      Log.e(TAG, GOALS_ERROR_DELETING, e)
      false
    }
  }

  override suspend fun transferToSavingsGoal(
    accountUid: String,
    savingsGoalUid: String,
    transferUid: String
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val response = retrofitService.transferMoneyToSavingsGoal(
        BuildConfig.API_KEY,
        accountUid,
        savingsGoalUid,
        transferUid,
        body = CreateAmountTransferRequest(
          amount = NetworkAmount(
            currency = REPO_CURRENCY_GBP,
            minorUnits = _accountDetails.value?.roundUpAmount ?: 0
          ),
          reference = REPO_ROUND_UP_TRANSFER_REFERENCE
        )
      )
      response.transferUid == transferUid
    } catch (e: Exception) {
      Log.e(TAG, ALERT_TRANSFERRING_ERROR, e)
      false
    }
  }

  private suspend fun getAccountsWithResult(): NetworkResult<List<DomainAccount>> =
    withContext(Dispatchers.IO) {
      try {
        val response = retrofitService.getAccounts(BuildConfig.API_KEY)
        NetworkResult.Success(response.toListOfDomainAccounts())
      } catch (e: Exception) {
        Log.e(TAG, REPO_FETCHING_ACCOUNTS_ERROR, e)
        NetworkResult.Error(e)
      }
    }

  private suspend fun getBalanceWithResult(accountUid: String): NetworkResult<DomainBalance> =
    withContext(Dispatchers.IO) {
      try {
        val balance = retrofitService.getBalance(BuildConfig.API_KEY, accountUid)
        NetworkResult.Success(balance.toDomainBalance())
      } catch (e: Exception) {
        Log.e(TAG, REPO_FETCHING_BALANCE_ERROR, e)
        NetworkResult.Error(e)
      }
    }

  private suspend fun getTransactionsWithResult(
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
      Log.e(TAG, REPO_FETCHING_TRANSACTIONS_ERROR, e)
      NetworkResult.Error(e)
    }
  }

  private suspend fun getSavingsGoalsWithResult(accountUid: String): NetworkResult<List<DomainSavingsGoal>> =
    withContext(Dispatchers.IO) {
      try {
        val goals = retrofitService.getSavingsGoals(BuildConfig.API_KEY, accountUid)
        NetworkResult.Success(goals.toListOfDomainSavingsGoals())
      } catch (e: Exception) {
        Log.e(TAG, REPO_FETCHING_SAVINGS_GOALS_ERROR, e)
        NetworkResult.Error(e)
      }
    }
}
