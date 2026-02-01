package com.example.roundupapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

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
