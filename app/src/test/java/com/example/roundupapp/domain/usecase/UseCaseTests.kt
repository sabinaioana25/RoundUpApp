package com.example.roundupapp.domain.usecase

import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCaseTests {

  private val repository = mockk<RoundUpRepository>(relaxed = true)

  @Test
  fun `AccountDetailsUseCase calls loadFromCache and refresh`() = runBlocking {
    val useCase = AccountDetailsUseCase(repository)
    useCase()
    coVerify { repository.loadFromCache() }
    coVerify { repository.refreshFromNetwork() }
  }

  @Test
  fun `CreateSavingsGoalUseCase calls repository createSavingsGoal`() = runBlocking {
    val useCase = CreateSavingsGoalUseCase(repository)
    val account = DomainAccount("acc123", "cat456", "2023-01-01", "Personal")
    val details = AccountDetails(listOf(account), emptyList(), emptyList(), "£0.00")
    every { repository.accountDetails } returns MutableStateFlow(details)

    val newGoal = DomainSavingsGoal(
      "goal789",
      "Trip",
      DomainAmount("GBP", 10000, "£100.00"),
      DomainAmount("GBP", 0, "£0.00"),
      "ACTIVE"
    )
    coEvery { repository.createSavingsGoal(any(), any(), any(), any()) } returns newGoal

    useCase("Trip", 100)

    coVerify { repository.createSavingsGoal("acc123", "Trip", 10000, "GBP") }
    coVerify { repository.addSavingsGoal(newGoal, "acc123") }
  }

  @Test
  fun `DeleteSavingsGoalUseCase calls repository deleteSavingsGoal`() = runBlocking {
    val useCase = DeleteSavingsGoalUseCase(repository)
    val account = DomainAccount("acc123", "cat456", "2023-01-01", "Personal")
    val goal = DomainSavingsGoal(
      "goal789",
      "Trip",
      DomainAmount("GBP", 10000, "£100.00"),
      DomainAmount("GBP", 0, "£0.00"),
      "ACTIVE"
    )
    val details = AccountDetails(listOf(account), emptyList(), listOf(goal), "£0.00")
    every { repository.accountDetails } returns MutableStateFlow(details)
    coEvery { repository.deleteSavingsGoal(any(), any()) } returns true

    useCase()

    coVerify { repository.deleteSavingsGoal("acc123", "goal789") }
    coVerify { repository.removeSavingsGoal("goal789") }
  }

  @Test
  fun `TransferToSavingsGoalUseCase calls repository transferToSavingsGoal`() = runBlocking {
    val useCase = TransferToSavingsGoalUseCase(repository)
    val account = DomainAccount("acc123", "cat456", "2023-01-01", "Personal")
    val goal = DomainSavingsGoal(
      "goal789",
      "Trip",
      DomainAmount("GBP", 10000, "£100.00"),
      DomainAmount("GBP", 0, "£0.00"),
      "ACTIVE"
    )
    val details = AccountDetails(listOf(account), emptyList(), listOf(goal), "£0.00", 146)
    every { repository.accountDetails } returns MutableStateFlow(details)
    coEvery { repository.transferToSavingsGoal(any(), any(), any()) } returns true

    val result = useCase()

    assertTrue(result)
    coVerify { repository.transferToSavingsGoal("acc123", "goal789", any()) }
    coVerify { repository.refreshFromNetwork() }
  }
}
