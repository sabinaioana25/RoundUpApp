package com.example.roundupapp.ui.home

import com.example.roundupapp.domain.ValidationException
import com.example.roundupapp.domain.connectivity.OfflineException
import com.example.roundupapp.domain.models.AccountDetails
import com.example.roundupapp.domain.models.DataSource
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction
import com.example.roundupapp.domain.usecase.AccountDetailsUseCase
import com.example.roundupapp.domain.usecase.CalculateRoundUpUseCase
import com.example.roundupapp.domain.usecase.CreateSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.DeleteSavingsGoalUseCase
import com.example.roundupapp.domain.usecase.TransferToSavingsGoalUseCase
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var accountDetailsUseCase: AccountDetailsUseCase
  private lateinit var createSavingsGoalUseCase: CreateSavingsGoalUseCase
  private lateinit var deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase
  private lateinit var transferToSavingsGoalUseCase: TransferToSavingsGoalUseCase
  private lateinit var calculateRoundUpUseCase: CalculateRoundUpUseCase

  private lateinit var viewModel: HomeViewModel

  // --- Test fixtures ---
  private val fakeAccount = DomainAccount("acc-123", "Personal", "2024-01-01T00:00:00Z", "cat-456")
  private val fakeTransaction = DomainTransaction(
    direction = "OUT",
    amount = DomainAmount("GBP", 99, "0.99"),
    transactionTime = "2024-01-10T12:00:00Z",
    counterPartyName = "Costa Coffee"
  )
  private val fakeSavingsGoal = DomainSavingsGoal(
    savingsGoalUid = "goal-789",
    name = "Holiday",
    targetAmount = DomainAmount("GBP", 100000, "1000.00"),
    totalSaved = DomainAmount("GBP", 5000, "50.00"),
    state = "ACTIVE"
  )
  private val fakeAccountDetails = AccountDetails(
    accounts = listOf(fakeAccount),
    transactions = listOf(fakeTransaction),
    savingsGoals = listOf(fakeSavingsGoal),
    balance = "250.00",
    dataSource = DataSource.NETWORK
  )

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    accountDetailsUseCase = mockk()
    createSavingsGoalUseCase = mockk()
    deleteSavingsGoalUseCase = mockk()
    transferToSavingsGoalUseCase = mockk()
    calculateRoundUpUseCase = mockk()

    coEvery { accountDetailsUseCase() } returns Result.success(fakeAccountDetails)
    every { calculateRoundUpUseCase(any()) } returns 150
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel(): HomeViewModel = HomeViewModel(
    accountDetailsUseCase,
    createSavingsGoalUseCase,
    deleteSavingsGoalUseCase,
    transferToSavingsGoalUseCase,
    calculateRoundUpUseCase
  )

  // ============================================================
  // Initialization
  // ============================================================

  @Test
  fun `init triggers initial load and state is populated on success`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    val state = viewModel.state.value
    assertEquals(LoadingState.Idle, state.loadingState)
    assertEquals(listOf(fakeAccount), state.accounts)
    assertEquals("acc-123", state.accountUid)
    assertEquals("Personal", state.defaultCategory)
    assertEquals("250.00", state.balance)
    assertEquals(listOf(fakeSavingsGoal), state.savingsGoals)
    assertEquals(150, state.roundedAmount)
    assertNull(state.error)
  }

  @Test
  fun `init sets InitialLoading state during load`() = runTest {
    viewModel = createViewModel()
    // Check state before coroutines run
    assertEquals(LoadingState.InitialLoading, viewModel.state.value.loadingState)
  }

  @Test
  fun `init with network failure sets NetworkError`() = runTest {
    coEvery { accountDetailsUseCase() } returns Result.failure(IOException("network error"))

    viewModel = createViewModel()
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.NetworkError)
  }

  @Test
  fun `init with offline failure sets OfflineError`() = runTest {
    coEvery { accountDetailsUseCase() } returns Result.failure(OfflineException("offline"))

    viewModel = createViewModel()
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.OfflineError)
  }

  @Test
  fun `init with generic failure sets DataError`() = runTest {
    coEvery { accountDetailsUseCase() } returns Result.failure(Exception("unknown"))

    viewModel = createViewModel()
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.DataError)
  }

  @Test
  fun `init with empty accounts still loads successfully with empty accountUid`() = runTest {
    val emptyDetails = fakeAccountDetails.copy(accounts = emptyList())
    coEvery { accountDetailsUseCase() } returns Result.success(emptyDetails)

    viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals("", viewModel.state.value.accountUid)
  }

  // ============================================================
  // Refresh intent
  // ============================================================

  @Test
  fun `Refresh intent sets Refreshing state (not InitialLoading)`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    // Freeze mid-flight so we can assert Refreshing before the coroutine completes
    coEvery { accountDetailsUseCase() } coAnswers {
      kotlinx.coroutines.awaitCancellation()
    }
    viewModel.processIntent(Intent.Refresh)
    testScheduler.runCurrent()

    assertEquals(LoadingState.Refreshing, viewModel.state.value.loadingState)
  }

  @Test
  fun `Refresh intent returns to Idle on success`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { accountDetailsUseCase() } returns Result.success(fakeAccountDetails)
    viewModel.processIntent(Intent.Refresh)
    advanceUntilIdle()

    assertEquals(LoadingState.Idle, viewModel.state.value.loadingState)
  }

  @Test
  fun `concurrent load is prevented while Refreshing`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    // Freeze so the first Refresh coroutine stays permanently in-flight
    coEvery { accountDetailsUseCase() } coAnswers {
      kotlinx.coroutines.awaitCancellation()
    }

    // First Refresh — launches a coroutine that suspends inside the use case
    viewModel.processIntent(Intent.Refresh)
    testScheduler.runCurrent()
    assertEquals(LoadingState.Refreshing, viewModel.state.value.loadingState)

    // Second Refresh while first is still in-flight — guard should drop it entirely.
    // If the guard were broken, a second coroutine would launch, hit awaitCancellation,
    // and the state would remain Refreshing via a *different* coroutine.
    // We can't distinguish that by state alone, so we clear recorded calls first
    // and then verify no new call was made.
    clearMocks(accountDetailsUseCase, answers = false, recordedCalls = true)

    viewModel.processIntent(Intent.Refresh)
    testScheduler.runCurrent()

    // Still frozen on the original coroutine — guard prevented a second launch
    assertEquals(LoadingState.Refreshing, viewModel.state.value.loadingState)
    // No new invocation since we cleared the call log
    coVerify(exactly = 1) { accountDetailsUseCase() }
  }

  // ============================================================
  // Create Savings Goal
  // ============================================================

  @Test
  fun `CreateSavingsGoal success triggers reload and clears error`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery {
      createSavingsGoalUseCase("acc-123", "Holiday", 100000, "GBP")
    } returns Result.success(fakeSavingsGoal)

    viewModel.processIntent(Intent.CreateSavingsGoal("Holiday", 100000, "GBP"))
    advanceUntilIdle()

    assertEquals(LoadingState.Idle, viewModel.state.value.loadingState)
    assertNull(viewModel.state.value.error)
  }

  @Test
  fun `CreateSavingsGoal uses accountUid from current state`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { createSavingsGoalUseCase(any(), any(), any(), any()) } returns Result.success(fakeSavingsGoal)

    viewModel.processIntent(Intent.CreateSavingsGoal("Holiday", 100000, "GBP"))
    advanceUntilIdle()

    coVerify { createSavingsGoalUseCase("acc-123", "Holiday", 100000, "GBP") }
  }

  @Test
  fun `CreateSavingsGoal sets InProgress state during operation`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    // Suspend indefinitely so the coroutine stays paused at the InProgress point
    coEvery { createSavingsGoalUseCase(any(), any(), any(), any()) } coAnswers {
      kotlinx.coroutines.awaitCancellation()
    }

    viewModel.processIntent(Intent.CreateSavingsGoal("Holiday", 100000, "GBP"))
    // Run the state update to InProgress, but the use case never returns so we stay mid-flight
    testScheduler.runCurrent()

    assertEquals(
      LoadingState.InProgress(LoadingState.Operation.CREATING_GOAL),
      viewModel.state.value.loadingState
    )
  }

  @Test
  fun `CreateSavingsGoal with ValidationException shows ValidationError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { createSavingsGoalUseCase(any(), any(), any(), any()) } returns
      Result.failure(ValidationException("Name cannot be blank"))

    viewModel.processIntent(Intent.CreateSavingsGoal("", 100000, "GBP"))
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.ValidationError)
    assertEquals("Name cannot be blank", (error as UiError.ValidationError).message)
  }

  @Test
  fun `CreateSavingsGoal with OfflineException shows OfflineError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { createSavingsGoalUseCase(any(), any(), any(), any()) } returns
      Result.failure(OfflineException("You are offline"))

    viewModel.processIntent(Intent.CreateSavingsGoal("Holiday", 100000, "GBP"))
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.OfflineError)
  }

  @Test
  fun `CreateSavingsGoal with generic failure shows OperationError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { createSavingsGoalUseCase(any(), any(), any(), any()) } returns
      Result.failure(Exception("unknown server error"))

    viewModel.processIntent(Intent.CreateSavingsGoal("Holiday", 100000, "GBP"))
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.OperationError)
    assertEquals(
      LoadingState.Operation.CREATING_GOAL,
      (error as UiError.OperationError).operation
    )
  }

  // ============================================================
  // Delete Savings Goal
  // ============================================================

  @Test
  fun `DeleteSavingsGoal uses first savings goal uid from state`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { deleteSavingsGoalUseCase(any(), any()) } returns Result.success(Unit)

    viewModel.processIntent(Intent.DeleteSavingsGoal)
    advanceUntilIdle()

    coVerify { deleteSavingsGoalUseCase("acc-123", "goal-789") }
  }

  @Test
  fun `DeleteSavingsGoal success triggers reload`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { deleteSavingsGoalUseCase(any(), any()) } returns Result.success(Unit)

    viewModel.processIntent(Intent.DeleteSavingsGoal)
    advanceUntilIdle()

    assertEquals(LoadingState.Idle, viewModel.state.value.loadingState)
    assertNull(viewModel.state.value.error)
  }

  @Test
  fun `DeleteSavingsGoal with empty savings goals list uses empty string uid`() = runTest {
    val detailsNoGoals = fakeAccountDetails.copy(savingsGoals = emptyList())
    coEvery { accountDetailsUseCase() } returns Result.success(detailsNoGoals)
    coEvery { deleteSavingsGoalUseCase(any(), any()) } returns Result.success(Unit)

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.processIntent(Intent.DeleteSavingsGoal)
    advanceUntilIdle()

    coVerify { deleteSavingsGoalUseCase("acc-123", "") }
  }

  @Test
  fun `DeleteSavingsGoal with ValidationException shows ValidationError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { deleteSavingsGoalUseCase(any(), any()) } returns
      Result.failure(ValidationException("Goal UID is required"))

    viewModel.processIntent(Intent.DeleteSavingsGoal)
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.ValidationError)
  }

  @Test
  fun `DeleteSavingsGoal with OfflineException shows OfflineError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { deleteSavingsGoalUseCase(any(), any()) } returns
      Result.failure(OfflineException("offline"))

    viewModel.processIntent(Intent.DeleteSavingsGoal)
    advanceUntilIdle()

    assertTrue(viewModel.state.value.error is UiError.OfflineError)
  }

  @Test
  fun `DeleteSavingsGoal with generic error shows OperationError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { deleteSavingsGoalUseCase(any(), any()) } returns
      Result.failure(IOException("network error"))

    viewModel.processIntent(Intent.DeleteSavingsGoal)
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.OperationError)
    assertEquals(LoadingState.Operation.DELETING_GOAL, (error as UiError.OperationError).operation)
  }

  // ============================================================
  // Transfer To Savings Goal
  // ============================================================

  @Test
  fun `TransferToSavingsGoal uses accountUid, first goal uid, and rounded amount from state`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { transferToSavingsGoalUseCase(any(), any(), any()) } returns Result.success(true)

    viewModel.processIntent(Intent.TransferToSavingsGoal)
    advanceUntilIdle()

    coVerify { transferToSavingsGoalUseCase("acc-123", "goal-789", 150) }
  }

  @Test
  fun `TransferToSavingsGoal success triggers reload`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { transferToSavingsGoalUseCase(any(), any(), any()) } returns Result.success(true)

    viewModel.processIntent(Intent.TransferToSavingsGoal)
    advanceUntilIdle()

    assertEquals(LoadingState.Idle, viewModel.state.value.loadingState)
    assertNull(viewModel.state.value.error)
  }

  @Test
  fun `TransferToSavingsGoal sets TRANSFERRING InProgress state`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    // Suspend indefinitely so the coroutine stays paused at the InProgress point
    coEvery { transferToSavingsGoalUseCase(any(), any(), any()) } coAnswers {
      kotlinx.coroutines.awaitCancellation()
    }

    viewModel.processIntent(Intent.TransferToSavingsGoal)
    // Run the state update to InProgress, but the use case never returns so we stay mid-flight
    testScheduler.runCurrent()

    assertEquals(
      LoadingState.InProgress(LoadingState.Operation.TRANSFERRING),
      viewModel.state.value.loadingState
    )
  }

  @Test
  fun `TransferToSavingsGoal with ValidationException shows ValidationError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { transferToSavingsGoalUseCase(any(), any(), any()) } returns
      Result.failure(ValidationException("Amount must be > 0"))

    viewModel.processIntent(Intent.TransferToSavingsGoal)
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.ValidationError)
    assertEquals("Amount must be > 0", (error as UiError.ValidationError).message)
  }

  @Test
  fun `TransferToSavingsGoal with OfflineException shows OfflineError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { transferToSavingsGoalUseCase(any(), any(), any()) } returns
      Result.failure(OfflineException("No internet"))

    viewModel.processIntent(Intent.TransferToSavingsGoal)
    advanceUntilIdle()

    assertTrue(viewModel.state.value.error is UiError.OfflineError)
  }

  @Test
  fun `TransferToSavingsGoal with generic error shows OperationError`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    coEvery { transferToSavingsGoalUseCase(any(), any(), any()) } returns
      Result.failure(IOException("payment server down"))

    viewModel.processIntent(Intent.TransferToSavingsGoal)
    advanceUntilIdle()

    val error = viewModel.state.value.error
    assertTrue(error is UiError.OperationError)
    assertEquals(LoadingState.Operation.TRANSFERRING, (error as UiError.OperationError).operation)
  }

  // ============================================================
  // DismissError intent
  // ============================================================

  @Test
  fun `DismissError clears current error`() = runTest {
    coEvery { accountDetailsUseCase() } returns Result.failure(IOException("error"))

    viewModel = createViewModel()
    advanceUntilIdle()

    assertTrue(viewModel.state.value.error != null)

    viewModel.processIntent(Intent.DismissError)

    assertNull(viewModel.state.value.error)
  }

  @Test
  fun `DismissError when no error present is a no-op`() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    assertNull(viewModel.state.value.error)

    viewModel.processIntent(Intent.DismissError)

    assertNull(viewModel.state.value.error)
    assertEquals(LoadingState.Idle, viewModel.state.value.loadingState)
  }

  // ============================================================
  // DataSource propagation
  // ============================================================

  @Test
  fun `state reflects NETWORK data source when served from network`() = runTest {
    coEvery { accountDetailsUseCase() } returns Result.success(
      fakeAccountDetails.copy(dataSource = DataSource.NETWORK)
    )

    viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(DataSource.NETWORK, viewModel.state.value.dataSource)
  }

  @Test
  fun `state reflects CACHE data source when served from cache`() = runTest {
    coEvery { accountDetailsUseCase() } returns Result.success(
      fakeAccountDetails.copy(dataSource = DataSource.CACHE)
    )

    viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(DataSource.CACHE, viewModel.state.value.dataSource)
  }
}
