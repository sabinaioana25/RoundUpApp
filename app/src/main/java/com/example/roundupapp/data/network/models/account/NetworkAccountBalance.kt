package com.example.roundupapp.data.network.models.account

import com.example.roundupapp.data.network.models.feed.NetworkAmount

data class NetworkAccountBalance(
  val clearedBalance: NetworkAmount,
  val effectiveBalance: NetworkAmount
)
