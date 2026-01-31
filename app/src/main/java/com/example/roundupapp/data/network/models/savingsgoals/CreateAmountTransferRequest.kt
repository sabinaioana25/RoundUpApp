package com.example.roundupapp.data.network.models.savingsgoals

import com.example.roundupapp.data.network.models.transactions.NetworkAmount

data class CreateAmountTransferRequest(
  val amount: NetworkAmount,
  val reference: String
)
