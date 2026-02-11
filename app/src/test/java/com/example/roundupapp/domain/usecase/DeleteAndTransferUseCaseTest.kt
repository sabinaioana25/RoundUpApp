package com.example.roundupapp.domain.usecase

import android.util.Log
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.ValidationException
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.repository.RoundUpRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

// =============================================================================
// DeleteSavingsGoalUseCaseTest
// =============================================================================
class DeleteSavingsGoalUseCaseTest {

    private lateinit var repository: RoundUpRepository
    private lateinit var connectivityChecker: NetworkConnectivityChecker
    private lateinit var useCase: DeleteSavingsGoalUseCase

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
        useCase = DeleteSavingsGoalUseCase(repository, connectivityChecker)
    }

    // ============================================================
    // Happy path
    // ============================================================

    @Test
    fun `when all valid, deletes from server then deletes from cache`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.deleteSavingsGoalsWithResult("acc-123", "goal-456") } returns DataResult.Success(Unit)
        coEvery { repository.cacheDeleteSavingsGoal("goal-456") } returns DataResult.Success(Unit)

        val result = useCase("acc-123", "goal-456")

        assertTrue(result.isSuccess)
        coVerify { repository.deleteSavingsGoalsWithResult("acc-123", "goal-456") }
        coVerify { repository.cacheDeleteSavingsGoal("goal-456") }
    }

    @Test
    fun `when server deletion succeeds but cache delete fails, still returns success`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.deleteSavingsGoalsWithResult(any(), any()) } returns DataResult.Success(Unit)
        coEvery { repository.cacheDeleteSavingsGoal(any()) } returns DataResult.Error(Exception("cache fail"))

        val result = useCase("acc-123", "goal-456")

        // Server deletion succeeded, so overall operation is a success
        assertTrue(result.isSuccess)
    }

    // ============================================================
    // Offline guard
    // ============================================================

    @Test
    fun `when offline, returns OfflineException without calling repository`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns false

        val result = useCase("acc-123", "goal-456")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is OfflineException)
        coVerify(exactly = 0) { repository.deleteSavingsGoalsWithResult(any(), any()) }
    }

    // ============================================================
    // Input validation
    // ============================================================

    @Test
    fun `when accountUid is blank, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("", "goal-456")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.deleteSavingsGoalsWithResult(any(), any()) }
    }

    @Test
    fun `when accountUid is whitespace, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("   ", "goal-456")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `when savingsGoalUid is blank, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.deleteSavingsGoalsWithResult(any(), any()) }
    }

    @Test
    fun `when savingsGoalUid is whitespace, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "   ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    // ============================================================
    // Server-side failures
    // ============================================================

    @Test
    fun `when server deletion fails, cache is never touched`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.deleteSavingsGoalsWithResult(any(), any()) } returns DataResult.Error(IOException("403 Forbidden"))

        val result = useCase("acc-123", "goal-456")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        coVerify(exactly = 0) { repository.cacheDeleteSavingsGoal(any()) }
    }

    @Test
    fun `when repository throws unexpected exception, returns failure`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.deleteSavingsGoalsWithResult(any(), any()) } throws RuntimeException("crash")

        val result = useCase("acc-123", "goal-456")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
}

// =============================================================================
// TransferToSavingsGoalUseCaseTest
// =============================================================================
class TransferToSavingsGoalUseCaseTest {

    private lateinit var repository: RoundUpRepository
    private lateinit var connectivityChecker: NetworkConnectivityChecker
    private lateinit var useCase: TransferToSavingsGoalUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        connectivityChecker = mockk()
        useCase = TransferToSavingsGoalUseCase(repository, connectivityChecker)
    }

    // ============================================================
    // Happy path
    // ============================================================

    @Test
    fun `when all valid, executes transfer and returns true`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery {
            repository.transferToSavingsGoalWithResult(
                accountUid = "acc-123",
                savingsGoalUid = "goal-456",
                amountMinorUnits = 150,
                transferUid = any()
            )
        } returns DataResult.Success(true)

        val result = useCase("acc-123", "goal-456", 150)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun `transfer generates a unique transferUid on each invocation`() = runTest {
        val capturedUids = mutableListOf<String>()
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery {
            repository.transferToSavingsGoalWithResult(any(), any(), any(), capture(capturedUids))
        } returns DataResult.Success(true)

        useCase("acc-123", "goal-456", 150)
        useCase("acc-123", "goal-456", 150)

        assertTrue("Transfer UIDs should be unique per call", capturedUids.distinct().size == 2)
    }

    // ============================================================
    // Offline guard
    // ============================================================

    @Test
    fun `when offline, returns OfflineException without calling repository`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns false

        val result = useCase("acc-123", "goal-456", 150)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is OfflineException)
        coVerify(exactly = 0) { repository.transferToSavingsGoalWithResult(any(), any(), any(), any()) }
    }

    // ============================================================
    // Input validation
    // ============================================================

    @Test
    fun `when accountUid is blank, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("", "goal-456", 150)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.transferToSavingsGoalWithResult(any(), any(), any(), any()) }
    }

    @Test
    fun `when savingsGoalUid is blank, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "", 150)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `when savingsGoalUid is whitespace, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "   ", 150)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `when amount is zero, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "goal-456", 0)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        coVerify(exactly = 0) { repository.transferToSavingsGoalWithResult(any(), any(), any(), any()) }
    }

    @Test
    fun `when amount is negative, returns ValidationException`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true

        val result = useCase("acc-123", "goal-456", -100)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun `when amount is minimum valid value of 1, proceeds with transfer`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery { repository.transferToSavingsGoalWithResult(any(), any(), any(), any()) } returns DataResult.Success(true)

        val result = useCase("acc-123", "goal-456", 1)

        assertTrue(result.isSuccess)
    }

    // ============================================================
    // Server-side failures
    // ============================================================

    @Test
    fun `when transfer API returns error, returns failure`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery {
            repository.transferToSavingsGoalWithResult(any(), any(), any(), any())
        } returns DataResult.Error(IOException("payment failed"))

        val result = useCase("acc-123", "goal-456", 150)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `when repository throws IllegalStateException for mismatched transferUid, returns failure`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery {
            repository.transferToSavingsGoalWithResult(any(), any(), any(), any())
        } returns DataResult.Error(IllegalStateException("Transfer UID mismatch"))

        val result = useCase("acc-123", "goal-456", 150)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `when repository throws unexpected exception, wraps and returns failure`() = runTest {
        coEvery { connectivityChecker.isNetworkAvailable() } returns true
        coEvery {
            repository.transferToSavingsGoalWithResult(any(), any(), any(), any())
        } throws RuntimeException("unexpected crash")

        val result = useCase("acc-123", "goal-456", 150)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
}
