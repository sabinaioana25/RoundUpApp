package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.domain.repository.RoundUpRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CalculateRoundUpUseCaseTest {

    private val repository = mockk<RoundUpRepository>(relaxed = true)
    private val calculateRoundUpUseCase = CalculateRoundUpUseCase(repository)

    @Test
    fun `invoke calculates round up correctly for multiple transactions`() = runBlocking {
        // Given
        val transactions = listOf(
            createTransaction(435, "OUT"), // 4.35 -> round up diff = 0.65
            createTransaction(520, "OUT"), // 5.20 -> round up diff = 0.80
            createTransaction(100, "OUT"), // 1.00 -> round up diff = 0.00
            createTransaction(99, "OUT"),  // 0.99 -> round up diff = 0.01
            createTransaction(500, "IN")   // IN direction ignored
        )
        val accountDetails = AccountDetails(
            accounts = emptyList(),
            transactions = transactions,
            savingsGoals = emptyList(),
            balance = "£10.00"
        )
        val stateFlow = MutableStateFlow<AccountDetails?>(accountDetails)
        every { repository.accountDetails } returns stateFlow

        // When
        calculateRoundUpUseCase()

        // Then
        coVerify { repository.setRoundUpAmount(146) }
        coVerify { repository.refresh() }
    }

    @Test
    fun `invoke does nothing if accountDetails is null`() = runBlocking {
        // Given
        every { repository.accountDetails } returns MutableStateFlow(null)

        // When
        calculateRoundUpUseCase()

        // Then
        coVerify(exactly = 0) { repository.setRoundUpAmount(any()) }
    }

    private fun createTransaction(minorUnits: Int, direction: String): DomainTransaction {
        return DomainTransaction(
            amount = DomainAmount("GBP", minorUnits, ""),
            direction = direction,
            transactionTime = "2023-01-01",
            counterPartyName = "Test"
        )
    }
}
