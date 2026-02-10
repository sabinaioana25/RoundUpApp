package com.example.roundupapp.domain.repository

import com.example.roundupapp.data.NetworkResult
import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalResponse
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainBalance
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction

/**
 * Repository interface for managing account data, savings goals, and round-up transfers
 * Provides methods for caching, network refresh, and savings goal operations
 */
interface RoundUpRepository {

  // ================================================================================
  // Network Operations
  // ================================================================================
  suspend fun getAccountsWithResult(): NetworkResult<List<DomainAccount>>
  suspend fun getBalanceWithResult(accountUid: String): NetworkResult<DomainBalance>
  suspend fun getTransactionsWithResult(
    accountUid: String,
    categoryUid: String
  ): NetworkResult<List<DomainTransaction>>

  suspend fun getSavingsGoalsWithResult(accountUid: String): NetworkResult<List<DomainSavingsGoal>>
  suspend fun createSavingsGoalsRequest(
    accountUid: String,
    name: String,
    currency: String,
    amountMinorUnits: Int
  ): CreateSavingsGoalResponse
  suspend fun deleteSavingsGoalsWithResult(accountUid: String, savingsGoalUid: String): NetworkResult<Unit>


  // ================================================================================
  // Database Operations - Read
  // ================================================================================
  suspend fun getCachedAccounts(): List<DomainAccount>
  suspend fun getCachedTransactions(accountUid: String): List<DomainTransaction>
  suspend fun getCachedSavingsGoals(accountUid: String): List<DomainSavingsGoal>
  suspend fun getCachedBalance(): DomainBalance?


  // ================================================================================
  // Database Operations - Write
  // ================================================================================
  suspend fun cacheAccounts(accounts: List<DomainAccount>)
  suspend fun cacheBalance(
    accountUid: String,
    balance: DomainBalance
  )
  suspend fun cacheTransactions(
    accountUid: String,
    transactions: List<DomainTransaction>
  )
  suspend fun cacheSavingsGoals(
    accountUid: String,
    savingsGoals: List<DomainSavingsGoal>
  )
  suspend fun cacheSavingsGoal(
    accountUid: String,
    goal: DomainSavingsGoal
  )
  suspend fun cacheDeleteSavingsGoal(savingsGoalUid: String)


  suspend fun transferToSavingsGoalWithResult(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int,
    transferUid: String,
  ): NetworkResult<Boolean>
}
