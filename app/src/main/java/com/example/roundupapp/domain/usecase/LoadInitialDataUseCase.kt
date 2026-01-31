package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.balance.DomainBalance
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.transaction.DomainTransaction
import com.example.roundupapp.domain.repository.RoundUpRepository
import jakarta.inject.Inject

data class InitialData(
  val accounts: List<DomainAccount>,
  val transactions: List<DomainTransaction>,
  val savingsGoals: List<DomainSavingsGoal>,
  val balance: List<DomainBalance>
)

class LoadInitialDataUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke() : InitialData? {
    val accounts = repository.getAccounts()
    if (accounts.isEmpty()) return null

    val account = accounts[0]
    val transactions = repository.getTransactions(account.accountUid, account.defaultCategory)
    val savingsGoals = repository.getSavingsGoals(account.accountUid)
    val balance = repository.getBalanceList(account.accountUid)

    return InitialData(accounts, transactions, savingsGoals, balance)
  }
}
