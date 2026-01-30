package com.example.roundupapp.data.network.models

data class NetworkAccount(
  val accountUid: String,
  val accountType: String,
  val defaultCategory: String,
  val currency: String,
  val createdAt: String,
  val name: String
)
