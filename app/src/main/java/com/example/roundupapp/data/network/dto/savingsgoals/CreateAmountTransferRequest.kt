package com.example.roundupapp.data.network.dto.savingsgoals

import com.example.roundupapp.data.network.dto.transactions.NetworkAmount

data class CreateAmountTransferRequest(
  val amount: NetworkAmount,
  val reference: String
)
