package com.example.roundupapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) var feedItemUid: Int = 0,
  val accountUid: String,
  val direction: String,
  val amountMinorUnits: Int,
  val transactionDate: String,
  val counterPartyName: String
)
