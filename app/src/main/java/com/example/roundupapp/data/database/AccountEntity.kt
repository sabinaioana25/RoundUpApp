package com.example.roundupapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
  @PrimaryKey val accountUid: String,
  val name: String,
  val defaultCategory: String,
)
