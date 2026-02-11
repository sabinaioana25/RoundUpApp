package com.example.roundupapp.domain.usecase

import com.example.roundupapp.data.DataResult
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.models.DataSource
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainBalance
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.domain.repository.RoundUpRepository
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AccountDetailsUseCaseTest {

  private lateinit var repository: RoundUpRepository
  private lateinit var connectivityChecker: NetworkConnectivityChecker
  private lateinit var useCase: AccountDetailsUseCase

  // --- Test fixtures ---
  private val fakeAccount = DomainAccount(
    accountUid = "acc-123",
    name = "Personal Account",
    defaultCategory = "cat-456",
    createdAt = "2024-01-01T00:00:00Z"
  )
  private val fakeTransaction = DomainTransaction(
    direction = "OUT",
    amount = DomainAmount(currency = "GBP", minorUnits = 499, gbpUnits = "4.99"),
    transactionTime = "2024-01-10T12:00:00Z",
    counterPartyName = "Coffee Shop"
  )
  private val fakeSavingsGoal = DomainSavingsGoal(
    savingsGoalUid = "goal-789",
    name = "Holiday Fund",
    targetAmount = DomainAmount(currency = "GBP", minorUnits = 100000, gbpUnits = "1000.00"),
    totalSaved = DomainAmount(currency = "GBP", minorUnits = 5000, gbpUnits = "50.00"),
    state = "ACTIVE"
  )
  private val fakeBalance = DomainBalance(
    effectiveBalance = DomainAmount(currency = "GBP", minorUnits = 25000, gbpUnits = "250.00")
  )

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
    useCase = AccountDetailsUseCase(repository, connectivityChecker)
  }

  @After
  fun tearDown() {
    unmockkStatic(Log::class)
  }

  // ============================================================
  // ONLINE - Happy path
  // ============================================================

  @Test
  fun `when online and all network calls succeed, returns network data with NETWORK source`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getTransactionsWithResult(any(), any()) } returns DataResult.Success(listOf(fakeTransaction))
    coEvery { repository.getSavingsGoalsWithResult(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getBalanceWithResult(any()) } returns DataResult.Success(fakeBalance)

    val result = useCase()

    assertTrue(result.isSuccess)
    val details = result.getOrThrow()
    assertEquals(DataSource.NETWORK, details.dataSource)
    assertEquals(listOf(fakeAccount), details.accounts)
    assertEquals(listOf(fakeTransaction), details.transactions)
    assertEquals(listOf(fakeSavingsGoal), details.savingsGoals)
  }

  @Test
  fun `when online, successful network data is persisted to all caches`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getTransactionsWithResult(any(), any()) } returns DataResult.Success(listOf(fakeTransaction))
    coEvery { repository.getSavingsGoalsWithResult(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getBalanceWithResult(any()) } returns DataResult.Success(fakeBalance)

    useCase()

    coVerify { repository.cacheAccounts(listOf(fakeAccount)) }
    coVerify { repository.cacheTransactions("acc-123", listOf(fakeTransaction)) }
    coVerify { repository.cacheSavingsGoals("acc-123", listOf(fakeSavingsGoal)) }
    coVerify { repository.cacheBalance("acc-123", fakeBalance) }
  }

  @Test
  fun `when online and accounts returns empty list, returns error`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(emptyList())

    val result = useCase()

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("No accounts found") == true)
  }

  // ============================================================
  // ONLINE - Partial failures (secondary calls fail)
  // ============================================================

  @Test
  fun `when online and only transactions fail, succeeds with empty transactions list`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getTransactionsWithResult(any(), any()) } returns DataResult.Error(IOException("timeout"))
    coEvery { repository.getSavingsGoalsWithResult(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getBalanceWithResult(any()) } returns DataResult.Success(fakeBalance)

    val result = useCase()

    assertTrue(result.isSuccess)
    val details = result.getOrThrow()
    assertTrue(details.transactions.isEmpty())
    assertEquals(listOf(fakeSavingsGoal), details.savingsGoals)
  }

  @Test
  fun `when online and only savings goals fail, succeeds with empty savings goals list`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getTransactionsWithResult(any(), any()) } returns DataResult.Success(listOf(fakeTransaction))
    coEvery { repository.getSavingsGoalsWithResult(any()) } returns DataResult.Error(IOException("server error"))
    coEvery { repository.getBalanceWithResult(any()) } returns DataResult.Success(fakeBalance)

    val result = useCase()

    assertTrue(result.isSuccess)
    val details = result.getOrThrow()
    assertTrue(details.savingsGoals.isEmpty())
    assertEquals(listOf(fakeTransaction), details.transactions)
  }

  @Test
  fun `when online and only balance fails, succeeds with default balance of 0_00`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getTransactionsWithResult(any(), any()) } returns DataResult.Success(listOf(fakeTransaction))
    coEvery { repository.getSavingsGoalsWithResult(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getBalanceWithResult(any()) } returns DataResult.Error(IOException("error"))

    val result = useCase()

    assertTrue(result.isSuccess)
    assertEquals("0.00", result.getOrThrow().balance)
  }

  @Test
  fun `when online and all secondary calls fail, returns error`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getTransactionsWithResult(any(), any()) } returns DataResult.Error(IOException("tx fail"))
    coEvery { repository.getSavingsGoalsWithResult(any()) } returns DataResult.Error(IOException("goal fail"))
    coEvery { repository.getBalanceWithResult(any()) } returns DataResult.Error(IOException("bal fail"))

    val result = useCase()

    assertTrue(result.isFailure)
  }

  @Test
  fun `when online and failed secondary calls do not trigger their cache writes`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getTransactionsWithResult(any(), any()) } returns DataResult.Error(IOException("fail"))
    coEvery { repository.getSavingsGoalsWithResult(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getBalanceWithResult(any()) } returns DataResult.Success(fakeBalance)

    useCase()

    coVerify(exactly = 0) { repository.cacheTransactions(any(), any()) }
    coVerify { repository.cacheSavingsGoals(any(), any()) }
  }

  // ============================================================
  // ONLINE - Network fails, falls back to cache
  // ============================================================

  @Test
  fun `when online but network fails, falls back to cache and returns CACHE source`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Error(IOException("network down"))
    coEvery { repository.getCachedAccounts() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getCachedTransactions(any()) } returns DataResult.Success(listOf(fakeTransaction))
    coEvery { repository.getCachedSavingsGoals(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getCachedBalance() } returns DataResult.Success(fakeBalance)

    val result = useCase()

    assertTrue(result.isSuccess)
    assertEquals(DataSource.CACHE, result.getOrThrow().dataSource)
  }

  @Test
  fun `when online but network fails and cache is also empty, returns failure`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Error(IOException("network down"))
    coEvery { repository.getCachedAccounts() } returns DataResult.Success(emptyList())

    val result = useCase()

    assertTrue(result.isFailure)
    // Original network error should surface, not a cache-specific error
    assertTrue(result.exceptionOrNull() is IOException)
  }

  @Test
  fun `when online but network fails and cache read also throws, returns failure with network error`() = runTest {
    val networkError = IOException("network down")
    coEvery { connectivityChecker.isNetworkAvailable() } returns true
    coEvery { repository.getAccountsWithResult() } returns DataResult.Error(networkError)
    coEvery { repository.getCachedAccounts() } returns DataResult.Error(Exception("DB corrupt"))

    val result = useCase()

    assertTrue(result.isFailure)
    assertEquals(networkError, result.exceptionOrNull())
  }

  // ============================================================
  // OFFLINE
  // ============================================================

  @Test
  fun `when offline, reads from cache and returns CACHE source`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns false
    coEvery { repository.getCachedAccounts() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getCachedTransactions(any()) } returns DataResult.Success(listOf(fakeTransaction))
    coEvery { repository.getCachedSavingsGoals(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getCachedBalance() } returns DataResult.Success(fakeBalance)

    val result = useCase()

    assertTrue(result.isSuccess)
    assertEquals(DataSource.CACHE, result.getOrThrow().dataSource)
    coVerify(exactly = 0) { repository.getAccountsWithResult() }
  }

  @Test
  fun `when offline and cache has no accounts, returns OfflineException`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns false
    coEvery { repository.getCachedAccounts() } returns DataResult.Success(emptyList())

    val result = useCase()

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is OfflineException)
  }

  @Test
  fun `when offline and cache transactions missing, returns empty transactions`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns false
    coEvery { repository.getCachedAccounts() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getCachedTransactions(any()) } returns DataResult.Error(Exception("no tx cache"))
    coEvery { repository.getCachedSavingsGoals(any()) } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getCachedBalance() } returns DataResult.Success(fakeBalance)

    val result = useCase()

    assertTrue(result.isSuccess)
    assertTrue(result.getOrThrow().transactions.isEmpty())
  }

  @Test
  fun `when offline and cache balance is null, returns default balance 0_00`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns false
    coEvery { repository.getCachedAccounts() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getCachedTransactions(any()) } returns DataResult.Success(emptyList())
    coEvery { repository.getCachedSavingsGoals(any()) } returns DataResult.Success(emptyList())
    coEvery { repository.getCachedBalance() } returns DataResult.Success(null)

    val result = useCase()

    assertTrue(result.isSuccess)
    assertEquals("0.00", result.getOrThrow().balance)
  }

  @Test
  fun `when offline, uses correct accountUid to query cached transactions and goals`() = runTest {
    coEvery { connectivityChecker.isNetworkAvailable() } returns false
    coEvery { repository.getCachedAccounts() } returns DataResult.Success(listOf(fakeAccount))
    coEvery { repository.getCachedTransactions("acc-123") } returns DataResult.Success(listOf(fakeTransaction))
    coEvery { repository.getCachedSavingsGoals("acc-123") } returns DataResult.Success(listOf(fakeSavingsGoal))
    coEvery { repository.getCachedBalance() } returns DataResult.Success(fakeBalance)

    useCase()

    coVerify { repository.getCachedTransactions("acc-123") }
    coVerify { repository.getCachedSavingsGoals("acc-123") }
  }
}
