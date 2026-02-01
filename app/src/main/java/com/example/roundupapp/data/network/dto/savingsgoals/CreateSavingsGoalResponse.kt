package com.example.roundupapp.data.network.dto.savingsgoals

data class CreateSavingsGoalResponse(
    val savingsGoalUid: String,
    val errors: List<String>?
)
