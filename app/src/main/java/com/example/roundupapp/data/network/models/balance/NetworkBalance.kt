package com.example.roundupapp.data.network.models.balance

import com.example.roundupapp.data.network.models.transactions.NetworkAmount

data class NetworkBalance(
  val clearedBalance: NetworkAmount,
  val effectiveBalance: NetworkAmount
)
