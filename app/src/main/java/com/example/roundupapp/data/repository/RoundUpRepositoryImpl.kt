package com.example.roundupapp.data.repository

import android.util.Log
import com.example.roundupapp.BuildConfig
import com.example.roundupapp.data.database.AccountEntity
import com.example.roundupapp.data.database.RoundUpDatabase
import com.example.roundupapp.data.database.SavingsGoalEntity
import com.example.roundupapp.data.database.TransactionEntity
import com.example.roundupapp.data.network.RoundUpApi.retrofitService
import com.example.roundupapp.data.network.models.account.NetworkAccountsWrapper
import com.example.roundupapp.data.network.models.savingsgoals.CreateAmountTransferRequest
import com.example.roundupapp.data.network.models.savingsgoals.CreateSavingsGoalRequest
import com.example.roundupapp.data.network.models.savingsgoals.NetworkSavingsGoal
import com.example.roundupapp.data.network.models.savingsgoals.NetworkSavingsGoalsWrapper
import com.example.roundupapp.data.network.models.transactions.NetworkAmount
import com.example.roundupapp.data.network.models.transactions.NetworkTransactionsWrapper
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.account.toListOfDomainAccounts
import com.example.roundupapp.domain.models.balance.DomainBalance
import com.example.roundupapp.domain.models.balance.toDomainBalance
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.savingsgoal.toDomainSavingsGoal
import com.example.roundupapp.domain.models.savingsgoal.toListOfDomainSavingsGoals
import com.example.roundupapp.domain.models.transaction.DomainAmount
import com.example.roundupapp.domain.models.transaction.DomainTransaction
import com.example.roundupapp.domain.models.transaction.toListOfDomainTransactions
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
      .getBySavingsGoal(account.accountUid)
      .map { entity ->
        DomainSavingsGoal(
          savingsGoalUid = entity.savingsGoalUid,
          name = entity.name,
          targetAmount = DomainAmount(
            currency = entity.targetAmountCurrency,
            minorUnits = entity.targetAmountMinorUnits,
            gbpUnits = ""
          ),
          totalSaved = DomainAmount(
            currency = entity.targetAmountCurrency,
            minorUnits = entity.targetAmountMinorUnits,
            gbpUnits = ""
          ),
          state = entity.state,
          createdAt = entity.createdAt
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
    val balance = getBalance(account.accountUid)?.effectiveBalance?.minorUnits.toGbp()

    saveToCache(accounts, transactions, savingsGoals, account)

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
    account: DomainAccount
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
        createdAt = it.createdAt
      )
    })
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
      retrofitService.getAccounts(BuildConfig.API_KEY)
    } catch (e: Exception) {
      NetworkAccountsWrapper(accounts = emptyList())
    }.toListOfDomainAccounts()
  }

  override suspend fun getBalance(
    accountUid: String
  ): DomainBalance? = withContext(Dispatchers.IO) {
    try {
      val balance = retrofitService.getBalance(
        BuildConfig.API_KEY,
        accountUid
      )
      balance
    } catch (e: Exception) {
      Log.e("RoundUpRepository", "getBalance: $e")
      null
    }?.toDomainBalance()
  }

  override suspend fun getTransactions(
    accountUid: String,
    categoryUid: String
  ): List<DomainTransaction> = withContext(Dispatchers.IO) {
    try {
      val changesSince = ZonedDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_INSTANT)
      retrofitService.getTransactions(
        BuildConfig.API_KEY,
        accountUid,
        categoryUid,
        changesSince
      )
    } catch (e: Exception) {
      NetworkTransactionsWrapper(feedItems = emptyList())
    }.toListOfDomainTransactions()
  }

  override suspend fun getSavingsGoals(accountUid: String): List<DomainSavingsGoal> =
    withContext(Dispatchers.IO) {
      val goals = try {
        retrofitService.getSavingsGoals(
          BuildConfig.API_KEY,
          accountUid
        )
      } catch (e: Exception) {
        null
      }
      (goals
        ?: NetworkSavingsGoalsWrapper(savingsGoalList = emptyList())).toListOfDomainSavingsGoals()
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
          targetAmount = request.target,
          createdAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
          totalSaved = NetworkAmount(
            currency = "GBP",
            minorUnits = amountMinorUnits
          ),
          state = "ACTIVE"
        )
      )
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun deleteSavingsGoal(
    accountUid: String,
    savingsGoalUid: String
  ): Boolean {
    val response = retrofitService.deleteSavingsGoal(
      BuildConfig.API_KEY,
      accountUid,
      savingsGoalUid
    )
    return response.isSuccessful
  }

  override suspend fun transferToSavingsGoal(
    accountUid: String,
    savingsGoalUid: String,
    transferUid: String
  ): Boolean {
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
        reference = "reference"
      )
    )
    return response.transferUid == transferUid
  }
}
