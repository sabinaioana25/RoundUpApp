package com.example.roundupapp.data.network.dto.savingsgoals

import com.example.roundupapp.data.network.dto.transactions.NetworkAmount
import com.google.gson.annotations.SerializedName

data class CreateSavingsGoalRequest(
    val name: String,
    val currency: String,
    @SerializedName("target")
    val target: NetworkAmount
)
