package com.example.roundupapp.domain.repository

import android.util.Log
import com.example.roundupapp.BuildConfig
import com.example.roundupapp.data.DataResult
import com.example.roundupapp.data.database.AccountDao
import com.example.roundupapp.data.database.BalanceDao
import com.example.roundupapp.data.database.RoundUpDatabase
import com.example.roundupapp.data.database.SavingsGoalDao
import com.example.roundupapp.data.database.TransactionDao
import com.example.roundupapp.data.database.entities.AccountEntity
import com.example.roundupapp.data.database.entities.BalanceEntity
import com.example.roundupapp.data.database.entities.SavingsGoalEntity
import com.example.roundupapp.data.database.entities.TransactionEntity
import com.example.roundupapp.data.network.RoundUpApi
import com.example.roundupapp.data.network.RoundUpApiService
import com.example.roundupapp.data.network.dto.account.NetworkAccount
import com.example.roundupapp.data.network.dto.account.NetworkAccountsWrapper
import com.example.roundupapp.data.network.dto.balance.NetworkBalance
import com.example.roundupapp.data.network.dto.savingsgoals.CreateAmountTransferRequest
import com.example.roundupapp.data.network.dto.savingsgoals.CreateAmountTransferResponse
import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalRequest
import com.example.roundupapp.data.network.dto.savingsgoals.CreateSavingsGoalResponse
import com.example.roundupapp.data.network.dto.savingsgoals.NetworkSavingsGoal
import com.example.roundupapp.data.network.dto.savingsgoals.NetworkSavingsGoalsWrapper
import com.example.roundupapp.data.network.dto.transactions.NetworkAmount
import com.example.roundupapp.data.network.dto.transactions.NetworkTransaction
import com.example.roundupapp.data.network.dto.transactions.NetworkTransactionsWrapper
import com.example.roundupapp.data.repository.RoundUpRepositoryImpl
import com.example.roundupapp.domain.models.DomainAccount
import com.example.roundupapp.domain.models.DomainAmount
import com.example.roundupapp.domain.models.DomainBalance
import com.example.roundupapp.domain.models.DomainSavingsGoal
import com.example.roundupapp.domain.models.DomainTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.sql.SQLException

/**
 * Unit tests for [com.example.roundupapp.data.repository.RoundUpRepositoryImpl].
 *
 * Tests both the network layer (API calls) and database layer (caching).
 * All external dependencies (retrofitService and DAOs) are mocked.
 */
class RoundUpRepositoryImplTest {

  // --- Mocks ---
  private lateinit var database: RoundUpDatabase
  private lateinit var accountDao: AccountDao
  private lateinit var transactionDao: TransactionDao
  private lateinit var savingsGoalDao: SavingsGoalDao
  private lateinit var balanceDao: BalanceDao
  private lateinit var apiService: RoundUpApiService

  private lateinit var repository: RoundUpRepositoryImpl

  // --- Database Test Entities ---
  private val accountEntity = AccountEntity(
      accountUid = "acc-123",
      name = "Personal",
      defaultCategory = "cat-456"
  )
  private val transactionEntity = TransactionEntity(
      accountUid = "acc-123",
      direction = "OUT",
      amountMinorUnits = 199,
      transactionDate = "2024-01-10T12:00:00Z",
      counterPartyName = "Supermarket"
  )
  private val savingsGoalEntity = SavingsGoalEntity(
      savingsGoalUid = "goal-789",
      accountUid = "acc-123",
      name = "Holiday",
      targetAmountMinorUnits = 100000,
      targetAmountCurrency = "GBP",
      totalSavedMinorUnits = 5000,
      totalSavedCurrency = "GBP",
      state = "ACTIVE"
  )
  private val balanceEntity = BalanceEntity(
      id = 1,
      accountUid = "acc-123",
      effectiveBalanceMinorUnits = 25000,
      effectiveBalanceCurrency = "GBP"
  )

  // --- Test Domain Models ---
  private val domainAccount = DomainAccount(
      accountUid = "acc-123",
      name = "Personal",
      defaultCategory = "cat-456",
      createdAt = ""
  )
  private val domainTransaction = DomainTransaction(
      direction = "OUT",
      amount = DomainAmount("GBP", 199, "1.99"),
      transactionTime = "2024-01-10T12:00:00Z",
      counterPartyName = "Supermarket"
  )
  private val domainSavingsGoal = DomainSavingsGoal(
      savingsGoalUid = "goal-789",
      name = "Holiday",
      targetAmount = DomainAmount("GBP", 100000, "1000.00"),
      totalSaved = DomainAmount("GBP", 5000, "50.00"),
      state = "ACTIVE"
  )
  private val domainBalance = DomainBalance(
      effectiveBalance = DomainAmount("GBP", 25000, "250.00")
  )

  // --- Test Network DTOs ---
  private val networkAccount = NetworkAccount(
      accountUid = "acc-123",
      accountType = "PRIMARY",
      defaultCategory = "cat-456",
      currency = "GBP",
      createdAt = "2024-01-01T00:00:00Z",
      name = "Personal"
  )
  private val networkTransaction = NetworkTransaction(
      feedItemUid = "tx-001",
      categoryUid = "cat-456",
      amount = NetworkAmount(currency = "GBP", minorUnits = 199),
      direction = "OUT",
      updatedAt = "2024-01-10T12:00:00Z",
      transactionTime = "2024-01-10T12:00:00Z",
      counterPartyName = "Supermarket",
  )
  private val networkSavingsGoal = NetworkSavingsGoal(
      savingsGoalUid = "goal-789",
      name = "Holiday",
      target = NetworkAmount(currency = "GBP", minorUnits = 100000),
      totalSaved = NetworkAmount(currency = "GBP", minorUnits = 5000),
      state = "ACTIVE"
  )
  private val networkBalance = NetworkBalance(
      effectiveBalance = NetworkAmount(currency = "GBP", minorUnits = 25000),
  )

  @Before
  fun setUp() {
    // Mock Android Log
      mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0
    every { Log.w(any(), any<String>()) } returns 0
    every { Log.w(any(), any<String>(), any()) } returns 0
    every { Log.e(any(), any<String>()) } returns 0
    every { Log.e(any(), any<String>(), any()) } returns 0

    // Mock database and DAOs
    database = mockk()
    accountDao = mockk(relaxed = true)
    transactionDao = mockk(relaxed = true)
    savingsGoalDao = mockk(relaxed = true)
    balanceDao = mockk(relaxed = true)

    every { database.accountDao() } returns accountDao
    every { database.transactionDao() } returns transactionDao
    every { database.savingsGoalDao() } returns savingsGoalDao
    every { database.balanceDao() } returns balanceDao

    // Mock API service
    apiService = mockk()
      mockkObject(RoundUpApi)
    every { RoundUpApi.retrofitService } returns apiService

    repository = RoundUpRepositoryImpl(database)
  }

  @After
  fun tearDown() {
      unmockkStatic(Log::class)
      unmockkObject(RoundUpApi)
  }

  // ============================================================
  // NETWORK LAYER - getAccountsWithResult
  // ============================================================

  @Test
  fun `getAccountsWithResult returns Success with mapped domain accounts`() = runTest {
      val networkResponse = NetworkAccountsWrapper(accounts = listOf(networkAccount))
      coEvery { apiService.getAccounts(BuildConfig.API_KEY) } returns networkResponse

      val result = repository.getAccountsWithResult()

      Assert.assertTrue(result is DataResult.Success)
      val accounts = (result as DataResult.Success).data
      Assert.assertEquals(1, accounts.size)
      Assert.assertEquals("acc-123", accounts[0].accountUid)
      Assert.assertEquals("Personal", accounts[0].name)
      Assert.assertEquals("cat-456", accounts[0].defaultCategory)
  }

  @Test
  fun `getAccountsWithResult returns empty list when API returns no accounts`() = runTest {
      val networkResponse = NetworkAccountsWrapper(accounts = emptyList())
      coEvery { apiService.getAccounts(BuildConfig.API_KEY) } returns networkResponse

      val result = repository.getAccountsWithResult()

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertTrue((result as DataResult.Success).data.isEmpty())
  }

  @Test
  fun `getAccountsWithResult wraps IOException in DataResult Error`() = runTest {
      coEvery { apiService.getAccounts(any()) } throws IOException("Network error")

      val result = repository.getAccountsWithResult()

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IOException)
  }

  @Test
  fun `getAccountsWithResult wraps generic Exception in DataResult Error`() = runTest {
      coEvery { apiService.getAccounts(any()) } throws RuntimeException("Server error")

      val result = repository.getAccountsWithResult()

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is RuntimeException)
  }

  // ============================================================
  // NETWORK LAYER - getBalanceWithResult
  // ============================================================

  @Test
  fun `getBalanceWithResult returns Success with mapped domain balance`() = runTest {
      val networkResponse = NetworkBalance(
          effectiveBalance = networkBalance.effectiveBalance,
      )
      coEvery { apiService.getBalance(BuildConfig.API_KEY, "acc-123") } returns networkResponse

      val result = repository.getBalanceWithResult("acc-123")

      Assert.assertTrue(result is DataResult.Success)

      val balance = (result as DataResult.Success).data
      Assert.assertEquals(25000, balance.effectiveBalance.minorUnits)
      Assert.assertEquals("GBP", balance.effectiveBalance.currency)
      Assert.assertEquals("£250.00", balance.effectiveBalance.gbpUnits)
  }

  @Test
  fun `getBalanceWithResult wraps IOException in DataResult Error`() = runTest {
      coEvery { apiService.getBalance(any(), any()) } throws IOException("Network timeout")

      val result = repository.getBalanceWithResult("acc-123")

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IOException)
  }

  // ============================================================
  // NETWORK LAYER - getTransactionsWithResult
  // ============================================================

  @Test
  fun `getTransactionsWithResult returns Success with mapped domain transactions`() = runTest {
      val networkResponse = NetworkTransactionsWrapper(feedItems = listOf(networkTransaction))
      coEvery {
          apiService.getTransactions(BuildConfig.API_KEY, "acc-123", "cat-456", any())
      } returns networkResponse

      val result = repository.getTransactionsWithResult("acc-123", "cat-456")

      Assert.assertTrue(result is DataResult.Success)
      val transactions = (result as DataResult.Success).data
      Assert.assertEquals(1, transactions.size)
      Assert.assertEquals("OUT", transactions[0].direction)
      Assert.assertEquals(199, transactions[0].amount.minorUnits)
      Assert.assertEquals("Supermarket", transactions[0].counterPartyName)
  }

  @Test
  fun `getTransactionsWithResult returns empty list when no transactions`() = runTest {
      val networkResponse = NetworkTransactionsWrapper(feedItems = emptyList())
      coEvery {
          apiService.getTransactions(any(), any(), any(), any())
      } returns networkResponse

      val result = repository.getTransactionsWithResult("acc-123", "cat-456")

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertTrue((result as DataResult.Success).data.isEmpty())
  }

  @Test
  fun `getTransactionsWithResult queries last 7 days of transactions`() = runTest {
      val changesSinceSlot = slot<String>()
      val networkResponse = NetworkTransactionsWrapper(feedItems = emptyList())
      coEvery {
          apiService.getTransactions(any(), any(), any(), capture(changesSinceSlot))
      } returns networkResponse

      repository.getTransactionsWithResult("acc-123", "cat-456")

      // Verify the changesSince parameter is a valid ISO instant timestamp
      val capturedChangesSince = changesSinceSlot.captured
      Assert.assertTrue(capturedChangesSince.isNotEmpty())
      Assert.assertTrue(capturedChangesSince.contains("T"))
      Assert.assertTrue(capturedChangesSince.contains("Z"))
  }

  @Test
  fun `getTransactionsWithResult wraps IOException in DataResult Error`() = runTest {
      coEvery {
          apiService.getTransactions(any(), any(), any(), any())
      } throws IOException("API error")

      val result = repository.getTransactionsWithResult("acc-123", "cat-456")

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IOException)
  }

  // ============================================================
  // NETWORK LAYER - getSavingsGoalsWithResult
  // ============================================================

  @Test
  fun `getSavingsGoalsWithResult returns Success with mapped domain goals`() = runTest {
      val networkResponse = NetworkSavingsGoalsWrapper(savingsGoalList = listOf(networkSavingsGoal))
      coEvery {
          apiService.getSavingsGoals(BuildConfig.API_KEY, "acc-123")
      } returns networkResponse

      val result = repository.getSavingsGoalsWithResult("acc-123")

      Assert.assertTrue(result is DataResult.Success)
      val goals = (result as DataResult.Success).data
      Assert.assertEquals(1, goals.size)
      Assert.assertEquals("goal-789", goals[0].savingsGoalUid)
      Assert.assertEquals("Holiday", goals[0].name)
      Assert.assertEquals(100000, goals[0].targetAmount.minorUnits)
      Assert.assertEquals(5000, goals[0].totalSaved.minorUnits)
  }

  @Test
  fun `getSavingsGoalsWithResult returns empty list when no goals`() = runTest {
      val networkResponse = NetworkSavingsGoalsWrapper(savingsGoalList = emptyList())
      coEvery { apiService.getSavingsGoals(any(), any()) } returns networkResponse

      val result = repository.getSavingsGoalsWithResult("acc-123")

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertTrue((result as DataResult.Success).data.isEmpty())
  }

  @Test
  fun `getSavingsGoalsWithResult wraps IOException in DataResult Error`() = runTest {
      coEvery { apiService.getSavingsGoals(any(), any()) } throws IOException("Server error")

      val result = repository.getSavingsGoalsWithResult("acc-123")

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IOException)
  }

  // ============================================================
  // NETWORK LAYER - createSavingsGoalsRequest
  // ============================================================

  @Test
  fun `createSavingsGoalsRequest returns Success with response on successful creation`() = runTest {
      val createResponse = CreateSavingsGoalResponse(
          savingsGoalUid = "goal-new",
          errors = emptyList()
      )
      coEvery {
          apiService.createSavingsGoal(BuildConfig.API_KEY, "acc-123", any())
      } returns createResponse

      val result = repository.createSavingsGoalsRequest(
          accountUid = "acc-123",
          name = "Vacation",
          currency = "GBP",
          amountMinorUnits = 50000
      )

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertEquals("goal-new", (result as DataResult.Success).data.savingsGoalUid)
  }

  @Test
  fun `createSavingsGoalsRequest sends correct request body to API`() = runTest {
      val createResponse = CreateSavingsGoalResponse("goal-new", emptyList())
      val capturedRequests = mutableListOf<CreateSavingsGoalRequest>()
      coEvery {
          apiService.createSavingsGoal(any(), any(), capture(capturedRequests))
      } returns createResponse

      repository.createSavingsGoalsRequest(
          accountUid = "acc-123",
          name = "Vacation",
          currency = "GBP",
          amountMinorUnits = 50000
      )

      Assert.assertEquals(1, capturedRequests.size)
      Assert.assertEquals("Vacation", capturedRequests[0].name)
      Assert.assertEquals("GBP", capturedRequests[0].currency)
      Assert.assertEquals(50000, capturedRequests[0].target.minorUnits)
  }

  @Test
  fun `createSavingsGoalsRequest wraps IOException in DataResult Error`() = runTest {
      coEvery {
          apiService.createSavingsGoal(any(), any(), any())
      } throws IOException("Network error")

      val result = repository.createSavingsGoalsRequest(
          accountUid = "acc-123",
          name = "Vacation",
          currency = "GBP",
          amountMinorUnits = 50000
      )

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IOException)
  }

  // ============================================================
  // NETWORK LAYER - transferToSavingsGoalWithResult
  // ============================================================

  @Test
  fun `transferToSavingsGoalWithResult returns Success true when transfer succeeds`() = runTest {
      val transferUid = "transfer-123"
      val transferResponse = CreateAmountTransferResponse(
          transferUid = transferUid,
          error = emptyList()
      )
      coEvery {
          apiService.transferMoneyToSavingsGoal(
              BuildConfig.API_KEY,
              "acc-123",
              "goal-789",
              transferUid,
              any()
          )
      } returns transferResponse

      val result = repository.transferToSavingsGoalWithResult(
          accountUid = "acc-123",
          savingsGoalUid = "goal-789",
          amountMinorUnits = 150,
          transferUid = transferUid
      )

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertTrue((result as DataResult.Success).data)
  }

  @Test
  fun `transferToSavingsGoalWithResult sends correct request body`() = runTest {
      val transferUid = "transfer-123"
      val transferResponse = CreateAmountTransferResponse(transferUid, emptyList())
      val capturedRequests = mutableListOf<CreateAmountTransferRequest>()
      coEvery {
          apiService.transferMoneyToSavingsGoal(any(), any(), any(), any(), capture(capturedRequests))
      } returns transferResponse

      repository.transferToSavingsGoalWithResult(
          accountUid = "acc-123",
          savingsGoalUid = "goal-789",
          amountMinorUnits = 150,
          transferUid = transferUid
      )

      Assert.assertEquals(1, capturedRequests.size)
      Assert.assertEquals("GBP", capturedRequests[0].amount.currency)
      Assert.assertEquals(150, capturedRequests[0].amount.minorUnits)
      Assert.assertTrue(capturedRequests[0].reference.isNotEmpty())
  }

  @Test
  fun `transferToSavingsGoalWithResult returns Error when transferUid mismatch`() = runTest {
      val transferResponse = CreateAmountTransferResponse(
          transferUid = "different-uid",
          error = emptyList()
      )
      coEvery {
          apiService.transferMoneyToSavingsGoal(any(), any(), any(), any(), any())
      } returns transferResponse

      val result = repository.transferToSavingsGoalWithResult(
          accountUid = "acc-123",
          savingsGoalUid = "goal-789",
          amountMinorUnits = 150,
          transferUid = "expected-uid"
      )

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IllegalStateException)
  }

  @Test
  fun `transferToSavingsGoalWithResult wraps IOException in DataResult Error`() = runTest {
      coEvery {
          apiService.transferMoneyToSavingsGoal(any(), any(), any(), any(), any())
      } throws IOException("Payment failed")

      val result = repository.transferToSavingsGoalWithResult(
          accountUid = "acc-123",
          savingsGoalUid = "goal-789",
          amountMinorUnits = 150,
          transferUid = "transfer-123"
      )

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IOException)
  }

  // ============================================================
  // NETWORK LAYER - deleteSavingsGoalsWithResult
  // ============================================================

  @Test
  fun `deleteSavingsGoalsWithResult wraps IOException in DataResult Error`() = runTest {
      coEvery {
          apiService.deleteSavingsGoal(any(), any(), any())
      } throws IOException("Delete failed")

      val result = repository.deleteSavingsGoalsWithResult("acc-123", "goal-789")

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is IOException)
  }

  // ============================================================
  // DATABASE LAYER - getCachedAccounts
  // ============================================================

  @Test
  fun `getCachedAccounts returns mapped domain accounts on success`() = runTest {
      coEvery { accountDao.getAll() } returns listOf(accountEntity)

      val result = repository.getCachedAccounts()

      Assert.assertTrue(result is DataResult.Success)
      val accounts = (result as DataResult.Success).data
      Assert.assertEquals(1, accounts.size)
      Assert.assertEquals("acc-123", accounts[0].accountUid)
      Assert.assertEquals("Personal", accounts[0].name)
      Assert.assertEquals("cat-456", accounts[0].defaultCategory)
  }

  @Test
  fun `getCachedAccounts returns empty list when DAO returns nothing`() = runTest {
      coEvery { accountDao.getAll() } returns emptyList()

      val result = repository.getCachedAccounts()

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertTrue((result as DataResult.Success).data.isEmpty())
  }

  @Test
  fun `getCachedAccounts returns DataResult_Error when DAO throws`() = runTest {
      coEvery { accountDao.getAll() } throws SQLException("DB read error")

      val result = repository.getCachedAccounts()

      Assert.assertTrue(result is DataResult.Error)
      Assert.assertTrue((result as DataResult.Error).exception is SQLException)
  }

  // ============================================================
  // DATABASE LAYER - getCachedTransactions
  // ============================================================

  @Test
  fun `getCachedTransactions returns mapped domain transactions for given accountUid`() = runTest {
      coEvery { transactionDao.getByAccount("acc-123") } returns listOf(transactionEntity)

      val result = repository.getCachedTransactions("acc-123")

      Assert.assertTrue(result is DataResult.Success)
      val transactions = (result as DataResult.Success).data
      Assert.assertEquals(1, transactions.size)
      Assert.assertEquals("OUT", transactions[0].direction)
      Assert.assertEquals(199, transactions[0].amount.minorUnits)
      Assert.assertEquals("Supermarket", transactions[0].counterPartyName)
  }

  @Test
  fun `getCachedTransactions returns empty list when no transactions stored`() = runTest {
      coEvery { transactionDao.getByAccount("acc-123") } returns emptyList()

      val result = repository.getCachedTransactions("acc-123")

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertTrue((result as DataResult.Success).data.isEmpty())
  }

  @Test
  fun `getCachedTransactions returns DataResult_Error when DAO throws`() = runTest {
      coEvery { transactionDao.getByAccount(any()) } throws RuntimeException("corrupt database")

      val result = repository.getCachedTransactions("acc-123")

      Assert.assertTrue(result is DataResult.Error)
  }

  @Test
  fun `getCachedTransactions uses correct accountUid to query DAO`() = runTest {
      coEvery { transactionDao.getByAccount("acc-999") } returns emptyList()

      repository.getCachedTransactions("acc-999")

      coVerify { transactionDao.getByAccount("acc-999") }
  }

  // ============================================================
  // DATABASE LAYER - getCachedSavingsGoals
  // ============================================================

  @Test
  fun `getCachedSavingsGoals returns mapped domain goals for given accountUid`() = runTest {
      coEvery { savingsGoalDao.getByAccountSavingsGoal("acc-123") } returns listOf(savingsGoalEntity)

      val result = repository.getCachedSavingsGoals("acc-123")

      Assert.assertTrue(result is DataResult.Success)
      val goals = (result as DataResult.Success).data
      Assert.assertEquals(1, goals.size)
      Assert.assertEquals("goal-789", goals[0].savingsGoalUid)
      Assert.assertEquals("Holiday", goals[0].name)
      Assert.assertEquals(100000, goals[0].targetAmount.minorUnits)
  }

  @Test
  fun `getCachedSavingsGoals returns empty list when no goals stored`() = runTest {
      coEvery { savingsGoalDao.getByAccountSavingsGoal("acc-123") } returns emptyList()

      val result = repository.getCachedSavingsGoals("acc-123")

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertTrue((result as DataResult.Success).data.isEmpty())
  }

  @Test
  fun `getCachedSavingsGoals returns DataResult_Error when DAO throws`() = runTest {
      coEvery { savingsGoalDao.getByAccountSavingsGoal(any()) } throws SQLException("read fail")

      val result = repository.getCachedSavingsGoals("acc-123")

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // DATABASE LAYER - getCachedBalance
  // ============================================================

  @Test
  fun `getCachedBalance returns mapped domain balance when entity exists`() = runTest {
      coEvery { balanceDao.get() } returns balanceEntity

      val result = repository.getCachedBalance()

      Assert.assertTrue(result is DataResult.Success)
      val balance = (result as DataResult.Success).data
      Assert.assertNotNull(balance)
      Assert.assertEquals(25000, balance?.effectiveBalance?.minorUnits)
      Assert.assertEquals("GBP", balance?.effectiveBalance?.currency)
  }

  @Test
  fun `getCachedBalance returns Success with null when no balance stored`() = runTest {
      coEvery { balanceDao.get() } returns null

      val result = repository.getCachedBalance()

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertNull((result as DataResult.Success).data)
  }

  @Test
  fun `getCachedBalance returns DataResult_Error when DAO throws`() = runTest {
      coEvery { balanceDao.get() } throws SQLException("db error")

      val result = repository.getCachedBalance()

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // DATABASE LAYER - cacheAccounts
  // ============================================================

  @Test
  fun `cacheAccounts inserts entities with correct mapping`() = runTest {
      coEvery { accountDao.insertAll(any()) } just Runs

      val result = repository.cacheAccounts(listOf(domainAccount))

      Assert.assertTrue(result is DataResult.Success)
      coVerify {
          accountDao.insertAll(match { entities ->
              entities.size == 1 &&
                  entities[0].accountUid == "acc-123" &&
                  entities[0].name == "Personal" &&
                  entities[0].defaultCategory == "cat-456"
          })
      }
  }

  @Test
  fun `cacheAccounts returns DataResult_Error when DAO throws`() = runTest {
      coEvery { accountDao.insertAll(any()) } throws SQLException("insert fail")

      val result = repository.cacheAccounts(listOf(domainAccount))

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // DATABASE LAYER - cacheBalance
  // ============================================================

  @Test
  fun `cacheBalance inserts BalanceEntity with correct fields`() = runTest {
      coEvery { balanceDao.insert(any()) } just Runs

      val result = repository.cacheBalance("acc-123", domainBalance)

      Assert.assertTrue(result is DataResult.Success)
      coVerify {
          balanceDao.insert(match { entity ->
              entity.accountUid == "acc-123" &&
                  entity.effectiveBalanceMinorUnits == 25000 &&
                  entity.effectiveBalanceCurrency == "GBP"
          })
      }
  }

  @Test
  fun `cacheBalance returns DataResult_Error when DAO throws`() = runTest {
      coEvery { balanceDao.insert(any()) } throws SQLException("write fail")

      val result = repository.cacheBalance("acc-123", domainBalance)

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // DATABASE LAYER - cacheTransactions
  // ============================================================

  @Test
  fun `cacheTransactions deletes existing then inserts new transactions`() = runTest {
      coEvery { transactionDao.deleteByAccount("acc-123") } just Runs
      coEvery { transactionDao.insertAll(any()) } just Runs

      val result = repository.cacheTransactions("acc-123", listOf(domainTransaction))

      Assert.assertTrue(result is DataResult.Success)
      coVerify { transactionDao.deleteByAccount("acc-123") }
      coVerify {
          transactionDao.insertAll(match { entities ->
              entities.size == 1 &&
                  entities[0].accountUid == "acc-123" &&
                  entities[0].direction == "OUT" &&
                  entities[0].amountMinorUnits == 199 &&
                  entities[0].counterPartyName == "Supermarket"
          })
      }
  }

  @Test
  fun `cacheTransactions deletes before inserting to prevent stale data`() = runTest {
      val callOrder = mutableListOf<String>()
      coEvery { transactionDao.deleteByAccount(any()) } answers { callOrder.add("delete") }
      coEvery { transactionDao.insertAll(any()) } answers { callOrder.add("insert") }

      repository.cacheTransactions("acc-123", listOf(domainTransaction))

      Assert.assertEquals(listOf("delete", "insert"), callOrder)
  }

  @Test
  fun `cacheTransactions with empty list clears all transactions for account`() = runTest {
      coEvery { transactionDao.deleteByAccount("acc-123") } just Runs
      coEvery { transactionDao.insertAll(any()) } just Runs

      val result = repository.cacheTransactions("acc-123", emptyList())

      Assert.assertTrue(result is DataResult.Success)
      coVerify { transactionDao.deleteByAccount("acc-123") }
      coVerify { transactionDao.insertAll(emptyList()) }
  }

  @Test
  fun `cacheTransactions returns DataResult_Error when delete throws`() = runTest {
      coEvery { transactionDao.deleteByAccount(any()) } throws SQLException("delete fail")

      val result = repository.cacheTransactions("acc-123", listOf(domainTransaction))

      Assert.assertTrue(result is DataResult.Error)
  }

  @Test
  fun `cacheTransactions returns DataResult_Error when insert throws`() = runTest {
      coEvery { transactionDao.deleteByAccount(any()) } just Runs
      coEvery { transactionDao.insertAll(any()) } throws SQLException("insert fail")

      val result = repository.cacheTransactions("acc-123", listOf(domainTransaction))

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // DATABASE LAYER - cacheSavingsGoals
  // ============================================================

  @Test
  fun `cacheSavingsGoals deletes existing then inserts new goals`() = runTest {
      coEvery { savingsGoalDao.deleteByAccount("acc-123") } just Runs
      coEvery { savingsGoalDao.insertAll(any()) } just Runs

      val result = repository.cacheSavingsGoals("acc-123", listOf(domainSavingsGoal))

      Assert.assertTrue(result is DataResult.Success)
      coVerify { savingsGoalDao.deleteByAccount("acc-123") }
      coVerify {
          savingsGoalDao.insertAll(match { entities ->
              entities.size == 1 &&
                  entities[0].savingsGoalUid == "goal-789" &&
                  entities[0].accountUid == "acc-123" &&
                  entities[0].name == "Holiday" &&
                  entities[0].targetAmountMinorUnits == 100000 &&
                  entities[0].totalSavedMinorUnits == 5000
          })
      }
  }

  @Test
  fun `cacheSavingsGoals returns DataResult_Error when DAO throws`() = runTest {
      coEvery { savingsGoalDao.deleteByAccount(any()) } throws Exception("db error")

      val result = repository.cacheSavingsGoals("acc-123", listOf(domainSavingsGoal))

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // DATABASE LAYER - cacheSavingsGoal (single)
  // ============================================================

  @Test
  fun `cacheSavingsGoal inserts a single goal with correct mapping`() = runTest {
      coEvery { savingsGoalDao.insertAll(any()) } just Runs

      val result = repository.cacheSavingsGoal("acc-123", domainSavingsGoal)

      Assert.assertTrue(result is DataResult.Success)
      coVerify {
          savingsGoalDao.insertAll(match { entities ->
              entities.size == 1 &&
                  entities[0].savingsGoalUid == "goal-789" &&
                  entities[0].accountUid == "acc-123" &&
                  entities[0].name == "Holiday" &&
                  entities[0].state == "ACTIVE"
          })
      }
  }

  @Test
  fun `cacheSavingsGoal returns DataResult_Error when DAO throws`() = runTest {
      coEvery { savingsGoalDao.insertAll(any()) } throws SQLException("conflict")

      val result = repository.cacheSavingsGoal("acc-123", domainSavingsGoal)

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // DATABASE LAYER - cacheDeleteSavingsGoal
  // ============================================================

  @Test
  fun `cacheDeleteSavingsGoal calls DAO with correct savingsGoalUid`() = runTest {
      coEvery { savingsGoalDao.deleteBySavingsGoalUid("goal-789") } just Runs

      val result = repository.cacheDeleteSavingsGoal("goal-789")

      Assert.assertTrue(result is DataResult.Success)
      coVerify { savingsGoalDao.deleteBySavingsGoalUid("goal-789") }
  }

  @Test
  fun `cacheDeleteSavingsGoal returns DataResult_Error when DAO throws`() = runTest {
      coEvery { savingsGoalDao.deleteBySavingsGoalUid(any()) } throws SQLException("delete fail")

      val result = repository.cacheDeleteSavingsGoal("goal-789")

      Assert.assertTrue(result is DataResult.Error)
  }

  // ============================================================
  // EDGE CASES - Multiple accounts
  // ============================================================

  @Test
  fun `getCachedAccounts correctly maps multiple accounts`() = runTest {
      val entities = listOf(
          AccountEntity("acc-1", "Primary", "cat-1"),
          AccountEntity("acc-2", "Secondary", "cat-2"),
      )
      coEvery { accountDao.getAll() } returns entities

      val result = repository.getCachedAccounts()

      Assert.assertTrue(result is DataResult.Success)
      Assert.assertEquals(2, (result as DataResult.Success).data.size)
      Assert.assertEquals("acc-1", result.data[0].accountUid)
      Assert.assertEquals("acc-2", result.data[1].accountUid)
  }
}
