package com.example.roundupapp.data.repository

import android.util.Log
import com.example.roundupapp.BuildConfig
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

  override suspend fun loadFromCache() {
    val accounts = database.accountDao().getAll().map { entity ->
      DomainAccount(
        accountUid = entity.accountUid,
        name = entity.name,
        defaultCategory = entity.defaultCategory,
        createdAt = ""
      )
    }
    if (accounts.isEmpty()) return

    val account = accounts[0]
    val transactions = database.transactionDao()
      .getByAccount(account.accountUid)
      .map { entity ->
        DomainTransaction(
          direction = entity.direction,
          amount = DomainAmount(
            currency = "",
            minorUnits = entity.amountMinorUnits,
            gbpUnits = ""
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
    val balance = balanceEntity?.effectiveBalanceMinorUnits?.toGbp() ?: "0.00"

    _accountDetails.value = AccountDetails(
      accounts = accounts,
      transactions = transactions,
      savingsGoals = savingsGoals,
      balance = balance
    )
  }

  override suspend fun refresh() {
    val accounts = getAccounts()
    if (accounts.isEmpty()) {
      _accountDetails.update { it?.copy(accounts = emptyList()) }
      return
    }

    val account = accounts[0]
    val transactions = getTransactions(account.accountUid, account.defaultCategory)
    val savingsGoals = getSavingsGoals(account.accountUid)
    val balanceDomain = getBalance(account.accountUid)
    val balance = getBalance(account.accountUid)?.effectiveBalance?.minorUnits.toGbp()

    saveToCache(accounts, transactions, savingsGoals, account, balanceDomain)

    if (_accountDetails.value == null) {
      _accountDetails.value = AccountDetails(
        accounts = accounts,
        transactions = transactions,
        savingsGoals = savingsGoals,
        balance = balance,
      )
    } else {
      _accountDetails.update {
        it?.copy(
          accounts = accounts,
          transactions = transactions,
          savingsGoals = savingsGoals,
          balance = balance,
        )
      }
    }
  }

  private suspend fun saveToCache(
    accounts: List<DomainAccount>,
    transactions: List<DomainTransaction>,
    savingsGoals: List<DomainSavingsGoal>,
    account: DomainAccount,
    balance: DomainBalance?,
  ) {
    database.accountDao().insertAll(accounts.map { 
      AccountEntity(
        accountUid = it.accountUid,
        name = it.name,
        defaultCategory = it.defaultCategory,
      )
    })

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

  override fun addSavingsGoal(goal: DomainSavingsGoal) {
    _accountDetails.update { it?.copy(savingsGoals = it.savingsGoals + goal) }
  }

  override fun removeSavingsGoal(savingsGoalUid: String) {
    _accountDetails.update { it?.copy(savingsGoals = it.savingsGoals.filter { g -> g.savingsGoalUid != savingsGoalUid }) }
  }

  override fun setRoundUpAmount(amount: Int) {
    _accountDetails.update { it?.copy(roundUpAmount = amount) }
  }

  override suspend fun getAccounts(): List<DomainAccount> = withContext(Dispatchers.IO) {
    try {
      val response = retrofitService.getAccounts(BuildConfig.API_KEY)
      response.toListOfDomainAccounts()
    } catch (e: Exception) {
      Log.e("RoundUpRepository", "Error fetching accounts", e)
      emptyList()
    }
  }

  override suspend fun getBalance(
    accountUid: String
  ): DomainBalance? = withContext(Dispatchers.IO) {
    try {
      val balance = retrofitService.getBalance(
        BuildConfig.API_KEY,
        accountUid
      )
      balance.toDomainBalance()
    } catch (e: Exception) {
      Log.e("RoundUpRepository", "Error fetching balance", e)
      null
    }
  }

  override suspend fun getTransactions(
    accountUid: String,
    categoryUid: String
  ): List<DomainTransaction> = withContext(Dispatchers.IO) {
    try {
      // Fetch transactions from the last 7 days
      val changesSince =
        ZonedDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_INSTANT)
      val response = retrofitService.getTransactions(
        BuildConfig.API_KEY,
        accountUid,
        categoryUid,
        changesSince
      )
      response.toListOfDomainTransactions()
    } catch (e: Exception) {
      Log.e("RoundUpRepository", "Error fetching transactions", e)
      emptyList()
    }
  }

  override suspend fun getSavingsGoals(accountUid: String): List<DomainSavingsGoal> =
    withContext(Dispatchers.IO) {
      try {
        val goals = retrofitService.getSavingsGoals(
          BuildConfig.API_KEY,
          accountUid
        )
        goals.toListOfDomainSavingsGoals()
      } catch (e: Exception) {
        Log.e("RoundUpRepository", "Error fetching savings goals", e)
        emptyList()
      }
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
        currency = "GBP",
        target = NetworkAmount(
          currency = "GBP",
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
            currency = "GBP",
            minorUnits = 0
          ),
          state = "ACTIVE"
        )
      )
    } catch (e: Exception) {
      Log.e("RoundUpRepository", "Error creating savings goal", e)
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
      Log.e("RoundUpRepository", "Error deleting savings goal", e)
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
            currency = "GBP",
            minorUnits = _accountDetails.value?.roundUpAmount ?: 0
          ),
          reference = "Round-up transfer"
        )
      )
      response.transferUid == transferUid
    } catch (e: Exception) {
      Log.e("RoundUpRepository", "Error transferring to savings goal", e)
      false
    }
  }
}
