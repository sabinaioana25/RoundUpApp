package com.example.roundupapp.domain.repository

import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.balance.DomainBalance
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.transaction.DomainTransaction

interface RoundUpRepository {
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

  suspend fun transferToSavingsGoal(
    accountUid: String,
    savingsGoalUid: String,
    transferUid: String,
  ): Boolean
}
