package com.example.roundupapp.domain.models

data class DomainTransferResponse(
  val transferUid: String,
  val error: List<String>?
)
