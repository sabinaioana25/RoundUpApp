package com.example.roundupapp.domain.models

/**
 * Currency conversion utilities for handling minor units
 * Centralizes business logic for currency conversion
 */
object CurrencyConverter {

    fun poundsToMinorUnits(pounds: Int): Int {
        return pounds * 100
    }

    fun poundsToMinorUnits(pounds: Double): Int {
        return (pounds * 100).toInt()
    }

    fun minorUnitsToPounds(minorUnits: Int): String {
        val pounds = minorUnits / 100.0
        return String.format("%.2f", pounds)
    }

    fun parseToMinorUnits(amountString: String): Int? {
        val amount = amountString.toDoubleOrNull() ?: return null
        if (amount <= 0) return null
        return (amount * 100).toInt()
    }
}
