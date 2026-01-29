package com.example.roundupapp.domain.models.account

data class Account(
  val accountUid: String,
  val accountType: String,
  val defaultCategory: String,
  val currency: String,
  val createdAt: String,
  val name: String
)
