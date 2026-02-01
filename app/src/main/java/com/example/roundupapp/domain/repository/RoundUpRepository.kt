package com.example.roundupapp.domain.repository

import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainBalance
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction
import kotlinx.coroutines.flow.StateFlow

interface RoundUpRepository {

  val accountDetails: StateFlow<AccountDetails?>

  suspend fun loadFromCache()
  suspend fun refresh()

  suspend fun getAccounts(): List<DomainAccount>

  suspend fun getBalance(
    accountUid: String
  ): DomainBalance?

  suspend fun getTransactions(
    accountUid: String,
    categoryUid: String
  ): List<DomainTransaction>

  suspend fun getSavingsGoals(
    accountUid: String,
  ): List<DomainSavingsGoal>

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

  fun addSavingsGoal(goal: DomainSavingsGoal)
  fun removeSavingsGoal(savingsGoalUid: String)

  fun setRoundUpAmount(amount: Int)

  suspend fun transferToSavingsGoal(
    accountUid: String,
    savingsGoalUid: String,
    transferUid: String,
  ): Boolean
}
