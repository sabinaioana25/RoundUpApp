package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.models.account.DomainAccount
import com.example.roundupapp.domain.models.savingsgoal.DomainSavingsGoal
import com.example.roundupapp.domain.models.transaction.DomainTransaction
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.utils.toGbp
import jakarta.inject.Inject

data class InitialData(
  val accounts: List<DomainAccount>,
  val transactions: List<DomainTransaction>,
  val savingsGoals: List<DomainSavingsGoal>,
  val balance: String
)

class AccountDetailsUseCase @Inject constructor(
  private val repository: RoundUpRepository
) {
  suspend operator fun invoke() : InitialData? {
    val accounts = repository.getAccounts()
    if (accounts.isEmpty()) return null

    val account = accounts[0]
    val transactions = repository.getTransactions(account.accountUid, account.defaultCategory)
    val savingsGoals = repository.getSavingsGoals(account.accountUid)
    val balance = repository.getBalance(account.accountUid)?.effectiveBalance?.minorUnits.toGbp()
    return InitialData(accounts, transactions, savingsGoals, balance)
  }
}
