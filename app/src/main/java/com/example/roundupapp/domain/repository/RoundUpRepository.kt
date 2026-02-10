package com.example.roundupapp.domain.repository

import com.example.roundupapp.data.DataResult
import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalResponse
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
  suspend fun getAccountsWithResult(): DataResult<List<DomainAccount>>
  suspend fun getBalanceWithResult(accountUid: String): DataResult<DomainBalance>
  suspend fun getTransactionsWithResult(
    accountUid: String,
    categoryUid: String
  ): DataResult<List<DomainTransaction>>

  suspend fun getSavingsGoalsWithResult(accountUid: String): DataResult<List<DomainSavingsGoal>>
  suspend fun createSavingsGoalsRequest(
    accountUid: String,
    name: String,
    currency: String,
    amountMinorUnits: Int
  ): DataResult<CreateSavingsGoalResponse>
  suspend fun deleteSavingsGoalsWithResult(accountUid: String, savingsGoalUid: String): DataResult<Unit>


  // ================================================================================
  // Database Operations - Read
  // ================================================================================
  suspend fun getCachedAccounts(): DataResult<List<DomainAccount>>
  suspend fun getCachedTransactions(accountUid: String): DataResult<List<DomainTransaction>>
  suspend fun getCachedSavingsGoals(accountUid: String): DataResult<List<DomainSavingsGoal>>
  suspend fun getCachedBalance(): DataResult<DomainBalance?>


  // ================================================================================
  // Database Operations - Write
  // ================================================================================
  suspend fun cacheAccounts(accounts: List<DomainAccount>): Any
  suspend fun cacheBalance(
    accountUid: String,
    balance: DomainBalance
  ): Any

  suspend fun cacheTransactions(
    accountUid: String,
    transactions: List<DomainTransaction>
  ): Any

  suspend fun cacheSavingsGoals(
    accountUid: String,
    savingsGoals: List<DomainSavingsGoal>
  ): Any

  suspend fun cacheSavingsGoal(
    accountUid: String,
    goal: DomainSavingsGoal
  ): Any

  suspend fun cacheDeleteSavingsGoal(savingsGoalUid: String): Any


  suspend fun transferToSavingsGoalWithResult(
    accountUid: String,
    savingsGoalUid: String,
    amountMinorUnits: Int,
    transferUid: String,
  ): DataResult<Boolean>
}
