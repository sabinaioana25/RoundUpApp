package com.example.roundupapp.data.network.dto.transactions

import com.google.gson.annotations.SerializedName

data class NetworkTransaction(
  val feedItemUid: String,
  val categoryUid: String,
  @SerializedName("sourceAmount")
  val amount: NetworkAmount,
  val direction: String,
  val updatedAt: String,
  val transactionTime: String,
  val counterPartyName: String
)
