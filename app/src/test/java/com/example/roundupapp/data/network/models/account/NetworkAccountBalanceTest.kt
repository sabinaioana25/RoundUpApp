package com.example.roundupapp.data.network.models.account

import com.example.roundupapp.data.network.models.feed.NetworkAmount
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkAccountBalanceTest {

    @Test
    fun `test NetworkAccountBalance deserialization from JSON`() {
        val json = """
            {
                "clearedBalance": {
                    "currency": "GBP",
                    "minorUnits": 988124
                },
                "effectiveBalance": {
                    "currency": "GBP",
                    "minorUnits": 988124
                }
            }
        """.trimIndent()

        val gson = Gson()
        val balance = gson.fromJson(json, NetworkAccountBalance::class.java)

        assertEquals("GBP", balance.clearedBalance.currency)
        assertEquals(988124, balance.clearedBalance.minorUnits)
        assertEquals("GBP", balance.effectiveBalance.currency)
        assertEquals(988124, balance.effectiveBalance.minorUnits)
    }

    @Test
    fun `test NetworkAccountBalance serialization to JSON`() {
        val balance = NetworkAccountBalance(
            clearedBalance = NetworkAmount(
                currency = "GBP",
                minorUnits = 988124
            ),
            effectiveBalance = NetworkAmount(
                currency = "GBP",
                minorUnits = 988124
            )
        )

        val gson = Gson()
        val json = gson.toJson(balance)

        assert(json.contains("\"currency\":\"GBP\""))
        assert(json.contains("\"minorUnits\":988124"))
        assert(json.contains("clearedBalance"))
        assert(json.contains("effectiveBalance"))
    }

    @Test
    fun `test NetworkAccountBalance data class properties`() {
        val clearedBalance = NetworkAmount("GBP", 988124)
        val effectiveBalance = NetworkAmount("GBP", 988124)
        val balance = NetworkAccountBalance(clearedBalance, effectiveBalance)

        assertEquals(clearedBalance, balance.clearedBalance)
        assertEquals(effectiveBalance, balance.effectiveBalance)
    }
}
