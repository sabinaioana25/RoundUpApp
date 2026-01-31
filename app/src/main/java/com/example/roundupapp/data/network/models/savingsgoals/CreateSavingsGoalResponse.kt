package com.example.roundupapp.data.network.models.savingsgoals

data class CreateSavingsGoalResponse(
    val savingsGoalUid: String,
    val errors: List<String>?
)
