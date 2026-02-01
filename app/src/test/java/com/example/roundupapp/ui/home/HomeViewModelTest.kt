package com.example.roundupapp.ui.home

import app.cash.turbine.test
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.usecase.AccountDetailsUseCase
import com.example.roundupapp.domain.usecase.CalculateRoundUpUseCase
import com.example.roundupapp.domain.usecase.CreateSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.DeleteSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.TransferToSavingsGoalUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<RoundUpRepository>(relaxed = true)
    private val accountDetailsUseCase = mockk<AccountDetailsUseCase>(relaxed = true)
    private val createSavingsGoalUseCase = mockk<CreateSavingsGoalUseCase>(relaxed = true)
    private val deleteSavingsGoalUseCase = mockk<DeleteSavingsGoalUseCase>(relaxed = true)
    private val transferToSavingsGoalUseCase = mockk<TransferToSavingsGoalUseCase>(relaxed = true)
    private val calculateRoundUpUseCase = mockk<CalculateRoundUpUseCase>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.accountDetails } returns MutableStateFlow(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is ScreenState()`() = runTest {
        viewModel = HomeViewModel(
            repository,
            accountDetailsUseCase,
            createSavingsGoalUseCase,
            deleteSavingsGoalUseCase,
            transferToSavingsGoalUseCase,
            calculateRoundUpUseCase
        )
        assertEquals(ScreenState(), viewModel.state.value)
    }

    @Test
    fun `loadData updates state when repository has accountDetails`() = runTest {
        val accountDetails = AccountDetails(
            accounts = emptyList(),
            transactions = emptyList(),
            savingsGoals = emptyList(),
            balance = "£100.00",
            roundUpAmount = 50
        )
        val stateFlow = MutableStateFlow<AccountDetails?>(accountDetails)
        every { repository.accountDetails } returns stateFlow

        viewModel = HomeViewModel(
            repository,
            accountDetailsUseCase,
            createSavingsGoalUseCase,
            deleteSavingsGoalUseCase,
            transferToSavingsGoalUseCase,
            calculateRoundUpUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("£100.00", state.balance)
            assertEquals(50, state.roundedAmount)
        }
    }

    @Test
    fun `createSavingsGoal with blank goalName sets error`() = runTest {
        viewModel = HomeViewModel(
            repository,
            accountDetailsUseCase,
            createSavingsGoalUseCase,
            deleteSavingsGoalUseCase,
            transferToSavingsGoalUseCase,
            calculateRoundUpUseCase
        )

        viewModel.processIntent(Intent.CreateSavingsGoal("", 100, "GBP"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Name cannot be blank", viewModel.state.value.error)
    }

    @Test
    fun `transferToSavingsGoal calls usecase and refreshes data`() = runTest {
        viewModel = HomeViewModel(
            repository,
            accountDetailsUseCase,
            createSavingsGoalUseCase,
            deleteSavingsGoalUseCase,
            transferToSavingsGoalUseCase,
            calculateRoundUpUseCase
        )

        viewModel.processIntent(Intent.TransferToSavingsGoal)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { transferToSavingsGoalUseCase() }
        coVerify { accountDetailsUseCase() }
        coVerify { calculateRoundUpUseCase() }
    }
}
