package com.example.roundupapp.domain.validation

import com.example.roundupapp.domain.ValidationException

/**
 * Centralized validation utilities to avoid repetition across use cases
 */
object Validator {
    
    /**
     * Validates that account UID is not blank
     * @throws ValidationException if validation fails
     */
    fun requireAccountUid(accountUid: String) {
        if (accountUid.isBlank()) {
            throw ValidationException("Account UID is required")
        }
    }
    
    /**
     * Validates that savings goal UID is not blank
     * @throws ValidationException if validation fails
     */
    fun requireSavingsGoalUid(savingsGoalUid: String) {
        if (savingsGoalUid.isBlank()) {
            throw ValidationException("Savings goal UID is required")
        }
    }
    
    /**
     * Validates that goal name is not blank
     * @throws ValidationException if validation fails
     */
    fun requireNonBlankName(name: String, fieldName: String = "Name") {
        if (name.isBlank()) {
            throw ValidationException("$fieldName cannot be blank")
        }
    }
    
    /**
     * Validates that amount is positive
     * @throws ValidationException if validation fails
     */
    fun requirePositiveAmount(amount: Int, fieldName: String = "Amount") {
        if (amount <= 0) {
            throw ValidationException("$fieldName must be greater than zero")
        }
    }
}
