package com.example.roundupapp.domain.models.feed

import com.example.roundupapp.domain.models.savingsgoal.Amount

data class FeedItem(
  val updatedAt: String,
  val transactionTime: String,
  val amount: Amount,
  val status: String
)
