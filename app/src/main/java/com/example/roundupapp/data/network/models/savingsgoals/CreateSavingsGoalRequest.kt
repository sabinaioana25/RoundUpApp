package com.example.roundupapp.data.network.models.savingsgoals

import com.example.roundupapp.data.network.models.feed.NetworkAmount
import com.google.gson.annotations.SerializedName

data class CreateSavingsGoalRequest(
    val name: String,
    val currency: String,
    @SerializedName("target")
    val target: NetworkAmount
)
