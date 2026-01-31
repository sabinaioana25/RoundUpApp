package com.example.roundupapp.data.repository

import com.example.roundupapp.BuildConfig
import com.example.roundupapp.data.network.RoundUpApi.retrofitService
import com.example.roundupapp.data.network.models.account.NetworkAccountsWrapper
import com.example.roundupapp.data.network.models.balance.NetworkBalanceWrapper
import com.example.roundupapp.data.network.models.feed.NetworkAmount
import com.example.roundupapp.data.network.models.feed.NetworkTransactionsWrapper
import com.example.roundupapp.data.network.models.savingsgoals.CreateSavingsGoalRequest
import com.example.roundupapp.data.network.models.savingsgoals.NetworkSavingsGoal
import com.example.roundupapp.data.network.models.savingsgoals.NetworkSavingsGoalsWrapper
import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.account.toListOfDomainAccounts
import com.example.roundupapp.domain.models.balance.DomainBalance
import com.example.roundupapp.domain.models.balance.toListOfDomainBalances
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.savingsgoal.toDomainSavingsGoal
import com.example.roundupapp.domain.models.savingsgoal.toListOfDomainSavingsGoals
import com.example.roundupapp.domain.models.transaction.DomainTransaction
import com.example.roundupapp.domain.models.transaction.toListOfDomainTransactions
import com.example.roundupapp.domain.repository.RoundUpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RoundUpRepositoryImpl : RoundUpRepository {

  override suspend fun getAccounts(): List<DomainAccount> = withContext(Dispatchers.IO) {
    try {
      retrofitService.getAccounts(BuildConfig.API_KEY)
    } catch (e: Exception) {
      NetworkAccountsWrapper(accounts = emptyList())
    }.toListOfDomainAccounts()
  }

  override suspend fun getBalanceList(
    accountUid: String
  ): List<DomainBalance> = withContext(Dispatchers.IO) {
    try {
      retrofitService.getBalanceList(
        BuildConfig.API_KEY,
        accountUid
      )
    } catch (e: Exception) {
      NetworkBalanceWrapper(balanceList = emptyList())
    }.toListOfDomainBalances()
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
}
