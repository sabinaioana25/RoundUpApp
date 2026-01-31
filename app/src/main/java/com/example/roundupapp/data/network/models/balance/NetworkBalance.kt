package com.example.roundupapp.data.network.models.balance

import com.example.roundupapp.data.network.models.feed.NetworkAmount

data class NetworkBalance(
  val clearedBalance: NetworkAmount,
  val effectiveBalance: NetworkAmount
)
