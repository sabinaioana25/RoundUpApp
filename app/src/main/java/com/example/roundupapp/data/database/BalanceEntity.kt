package com.example.roundupapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balance")
data class BalanceEntity(
  @PrimaryKey val id: Int = 0,
  val accountUid: String,
  val effectiveBalanceMinorUnits: Int,
  val effectiveBalanceCurrency: String
)
