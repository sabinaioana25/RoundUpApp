package com.example.roundupapp.data.network.dto.balance

import com.example.roundupapp.data.network.dto.transactions.NetworkAmount

data class NetworkBalance(
  val effectiveBalance: NetworkAmount,
)
