package com.example.roundupapp.data.network.models.savingsgoals

data class NetworkCreateSavingsGoalResponse(
    val savingsGoalUid: String,
    val errors: List<String>?
)
