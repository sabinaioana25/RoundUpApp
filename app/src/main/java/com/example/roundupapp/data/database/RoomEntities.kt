package com.example.roundupapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.internal.http2.flowcontrol.WindowCounter

@Entity(tableName = "accounts")
data class AccountEntity(
  @PrimaryKey val accountUid: String,
  val name: String,
  val defaultCategory: String,
)

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) var feedItemUid: Int = 0,
  val accountUid: String,
  val direction: String,
  val amountMinorUnits: Int,
  val transactionDate: String,
  val counterPartyName: String
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
  @PrimaryKey val savingsGoalUid: String,
  val accountUid: String,
  val name: String,
  val targetAmountMinorUnits: Int,
  val targetAmountCurrency: String,
  val totalSavedMinorUnits: Int,
  val totalSavedCurrency: String,
  val state: String,
  val createdAt: String
)

@Entity(tableName = "balance")
data class BalanceEntity(
  @PrimaryKey val id: Int = 0,
  val accountUid: String,
  val effectiveBalanceMinorUnits: Int,
  val effectiveBalanceCurrency: String
)
