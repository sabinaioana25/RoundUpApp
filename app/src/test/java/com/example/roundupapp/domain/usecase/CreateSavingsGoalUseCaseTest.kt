package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalResponse
import com.example.roundupapp.domain.ValidationException
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.repository.RoundUpRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CreateSavingsGoalUseCaseTest {

    private lateinit var repository: RoundUpRepository
    private lateinit var connectivityChecker: NetworkConnectivityChecker
    private lateinit var useCase: CreateSavingsGoalUseCase

    private val fakeGoal = DomainSavingsGoal(
        savingsGoalUid = "goal-001",
        name = "Holiday Fund",
        targetAmount = DomainAmount("GBP", 100000, "1000.00"),
        totalSaved = DomainAmount("GBP", 0, "0.00"),
        state = "ACTIVE"
    )
    private val fakeCreateResponse = CreateSavingsGoalResponse(savingsGoalUid = "goal-001", errors = emptyList())

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        repository = mockk(relaxed = true)
        connectivityChecker = mockk()
        useCase = CreateSavingsGoalUseCase(repository, connectivityChecker)
    }

    // ============================================================
    // Happy path
    // ============================================================

    @Test
    fun `when all valid, creates goal, fetches list and returns first goal`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.createSavingsGoalsRequest(any(), any(), any(), any()) } returns DataResult.Success(fakeCreateResponse)
        coEvery { repository.getSavingsGoalsWithResult("acc-123") } returns DataResult.Success(listOf(fakeGoal))

        val result = useCase("acc-123", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isSuccess)
        assertEquals(fakeGoal, result.getOrThrow())
    }

    @Test
    fun `when creation succeeds, caches the newly created goal`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.createSavingsGoalsRequest(any(), any(), any(), any()) } returns DataResult.Success(fakeCreateResponse)
        coEvery { repository.getSavingsGoalsWithResult("acc-123") } returns DataResult.Success(listOf(fakeGoal))

        useCase("acc-123", "Holiday Fund", 100000, "GBP")

        coVerify { repository.cacheSavingsGoal("acc-123", fakeGoal) }
    }

    @Test
    fun `when cache write fails after successful creation, still returns success`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.createSavingsGoalsRequest(any(), any(), any(), any()) } returns DataResult.Success(fakeCreateResponse)
        coEvery { repository.getSavingsGoalsWithResult("acc-123") } returns DataResult.Success(listOf(fakeGoal))
        coEvery { repository.cacheSavingsGoal(any(), any()) } returns DataResult.Error(Exception("DB full"))

        val result = useCase("acc-123", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isSuccess)
    }

    // ============================================================
    // Offline guard
    // ============================================================

    @Test
    fun `when offline, returns OfflineException immediately`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns false

        val result = useCase("acc-123", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is OfflineException)
        coVerify(exactly = 0) { repository.createSavingsGoalsRequest(any(), any(), any(), any()) }
    }

    // ============================================================
    // Input validation
    // ============================================================

    @Test
    fun `when accountUid is blank, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.createSavingsGoalsRequest(any(), any(), any(), any()) }
    }

    @Test
    fun `when accountUid is whitespace only, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("   ", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `when goal name is blank, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.createSavingsGoalsRequest(any(), any(), any(), any()) }
    }

    @Test
    fun `when goal name is whitespace only, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "   ", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `when amount is zero, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "Holiday Fund", 0, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.createSavingsGoalsRequest(any(), any(), any(), any()) }
    }

    @Test
    fun `when amount is negative, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "Holiday Fund", -500, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    // ============================================================
    // Server-side failures
    // ============================================================

    @Test
    fun `when createSavingsGoalsRequest fails, returns failure immediately`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.createSavingsGoalsRequest(any(), any(), any(), any()) } returns DataResult.Error(IOException("server error"))

        val result = useCase("acc-123", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        coVerify(exactly = 0) { repository.getSavingsGoalsWithResult(any()) }
    }

    @Test
    fun `when getSavingsGoals after creation returns empty list, returns failure`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.createSavingsGoalsRequest(any(), any(), any(), any()) } returns DataResult.Success(fakeCreateResponse)
        coEvery { repository.getSavingsGoalsWithResult("acc-123") } returns DataResult.Success(emptyList())

        val result = useCase("acc-123", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }

    @Test
    fun `when getSavingsGoals after creation fails with network error, returns failure`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.createSavingsGoalsRequest(any(), any(), any(), any()) } returns DataResult.Success(fakeCreateResponse)
        coEvery { repository.getSavingsGoalsWithResult("acc-123") } returns DataResult.Error(IOException("timeout"))

        val result = useCase("acc-123", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `when repository throws unexpected exception, returns failure`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.createSavingsGoalsRequest(any(), any(), any(), any()) } throws RuntimeException("unexpected crash")

        val result = useCase("acc-123", "Holiday Fund", 100000, "GBP")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
}
