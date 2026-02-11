package com.example.roundupapp.domain.validation

import com.example.roundupapp.domain.ValidationException

/**
 * Centralized validation utilities to avoid repetition across use cases
 */
object Validator {

    fun requireAccountUid(accountUid: String) {
        if (accountUid.isBlank()) {
            throw ValidationException("Account UID is required")
        }
    }

    fun requireSavingsGoalUid(savingsGoalUid: String) {
        if (savingsGoalUid.isBlank()) {
            throw ValidationException("Savings goal UID is required")
        }
    }
    
    fun requireNonBlankName(name: String, fieldName: String = "Name") {
        if (name.isBlank()) {
            throw ValidationException("$fieldName cannot be blank")
        }
    }
    
    fun requirePositiveAmount(amount: Int, fieldName: String = "Amount") {
        if (amount <= 0) {
            throw ValidationException("$fieldName must be greater than zero")
        }
    }
}
