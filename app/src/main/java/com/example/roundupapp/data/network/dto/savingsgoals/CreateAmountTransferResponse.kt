package com.example.roundupapp.data.network.dto.savingsgoals

data class CreateAmountTransferResponse(
  val transferUid: String,
  val error: List<String>?
)
