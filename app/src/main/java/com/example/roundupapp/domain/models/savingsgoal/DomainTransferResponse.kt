package com.example.roundupapp.domain.models.savingsgoal

data class DomainTransferResponse(
  val transferUid: String,
  val error: List<String>?
)
