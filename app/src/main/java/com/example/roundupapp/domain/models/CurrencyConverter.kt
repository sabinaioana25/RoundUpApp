package com.example.roundupapp.domain.models

/**
 * Currency conversion utilities for handling minor units
 * Centralizes business logic for currency conversion
 */
object CurrencyConverter {

    fun poundsToMinorUnits(pounds: Int): Int {
        return pounds * 100
    }

    fun parseToMinorUnits(amountString: String): Int? {
        val amount = amountString.toDoubleOrNull() ?: return null
        if (amount <= 0) return null
        return (amount * 100).toInt()
    }
}
