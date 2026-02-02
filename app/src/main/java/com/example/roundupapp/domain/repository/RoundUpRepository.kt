package com.example.roundupapp.domain.repository

import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DomainSavingsGoal
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for managing account data, savings goals, and round-up transfers
 * Provides methods for caching, network refresh, and savings goal operations
 */
interface RoundUpRepository {

  val accountDetails: StateFlow<AccountDetails?>

  suspend fun loadFromCache()

  suspend fun refreshFromNetwork()

  suspend fun createSavingsGoal(
    accountUid: String,
    name: String,
    amountMinorUnits: Int,
    currency: String
  ): DomainSavingsGoal?

  suspend fun deleteSavingsGoal(
    accountUid: String,
    savingsGoalUid: String
  ): Boolean

  suspend fun addSavingsGoal(goal: DomainSavingsGoal, accountUid: String)

  suspend fun removeSavingsGoal(savingsGoalUid: String)

  fun setRoundUpAmount(amount: Int)

  suspend fun transferToSavingsGoal(
    accountUid: String,
    savingsGoalUid: String,
    transferUid: String,
  ): Boolean
}
