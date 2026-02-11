package com.example.roundupapp.domain.usecase

import android.util.Log
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
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.SQLException

/**
 * Unit tests for [com.example.roundupapp.data.repository.RoundUpRepositoryImpl].
 *
 * Network calls via [retrofitService] are tested at the integration level;
 * these tests focus entirely on the **database (cache) layer** to ensure
 * correct entity ↔ domain mapping and proper error handling when the
 * DAOs throw exceptions.
 */
class RoundUpRepositoryImplTest {

    // --- Database mocks ---
    private lateinit var database: RoundUpDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var savingsGoalDao: SavingsGoalDao
    private lateinit var balanceDao: BalanceDao

    private lateinit var repository: RoundUpRepositoryImpl

    // --- Shared test entities ---
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

    @Before
    fun setUp() {
        database = mockk()
        accountDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        savingsGoalDao = mockk(relaxed = true)
        balanceDao = mockk(relaxed = true)

        every { database.accountDao() } returns accountDao
        every { database.transactionDao() } returns transactionDao
        every { database.savingsGoalDao() } returns savingsGoalDao
        every { database.balanceDao() } returns balanceDao

        repository = RoundUpRepositoryImpl(database)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
    }

    // ============================================================
    // getCachedAccounts
    // ============================================================

    @Test
    fun `getCachedAccounts returns mapped domain accounts on success`() = runTest {
        coEvery { accountDao.getAll() } returns listOf(accountEntity)

        val result = repository.getCachedAccounts()

        assertTrue(result is DataResult.Success)
        val accounts = (result as DataResult.Success).data
        assertEquals(1, accounts.size)
        assertEquals("acc-123", accounts[0].accountUid)
        assertEquals("Personal", accounts[0].name)
        assertEquals("cat-456", accounts[0].defaultCategory)
    }

    @Test
    fun `getCachedAccounts returns empty list when DAO returns nothing`() = runTest {
        coEvery { accountDao.getAll() } returns emptyList()

        val result = repository.getCachedAccounts()

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }

    @Test
    fun `getCachedAccounts returns DataResult_Error when DAO throws`() = runTest {
        coEvery { accountDao.getAll() } throws SQLException("DB read error")

        val result = repository.getCachedAccounts()

        assertTrue(result is DataResult.Error)
        assertTrue((result as DataResult.Error).exception is SQLException)
    }

    // ============================================================
    // getCachedTransactions
    // ============================================================

    @Test
    fun `getCachedTransactions returns mapped domain transactions for given accountUid`() = runTest {
        coEvery { transactionDao.getByAccount("acc-123") } returns listOf(transactionEntity)

        val result = repository.getCachedTransactions("acc-123")

        assertTrue(result is DataResult.Success)
        val transactions = (result as DataResult.Success).data
        assertEquals(1, transactions.size)
        assertEquals("OUT", transactions[0].direction)
        assertEquals(199, transactions[0].amount.minorUnits)
        assertEquals("Supermarket", transactions[0].counterPartyName)
    }

    @Test
    fun `getCachedTransactions returns empty list when no transactions stored`() = runTest {
        coEvery { transactionDao.getByAccount("acc-123") } returns emptyList()

        val result = repository.getCachedTransactions("acc-123")

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }

    @Test
    fun `getCachedTransactions returns DataResult_Error when DAO throws`() = runTest {
        coEvery { transactionDao.getByAccount(any()) } throws RuntimeException("corrupt database")

        val result = repository.getCachedTransactions("acc-123")

        assertTrue(result is DataResult.Error)
    }

    @Test
    fun `getCachedTransactions uses correct accountUid to query DAO`() = runTest {
        coEvery { transactionDao.getByAccount("acc-999") } returns emptyList()

        repository.getCachedTransactions("acc-999")

        coVerify { transactionDao.getByAccount("acc-999") }
    }

    // ============================================================
    // getCachedSavingsGoals
    // ============================================================

    @Test
    fun `getCachedSavingsGoals returns mapped domain savings goals`() = runTest {
        coEvery { savingsGoalDao.getByAccountSavingsGoal("acc-123") } returns listOf(savingsGoalEntity)

        val result = repository.getCachedSavingsGoals("acc-123")

        assertTrue(result is DataResult.Success)
        val goals = (result as DataResult.Success).data
        assertEquals(1, goals.size)
        assertEquals("goal-789", goals[0].savingsGoalUid)
        assertEquals("Holiday", goals[0].name)
        assertEquals(100000, goals[0].targetAmount.minorUnits)
        assertEquals(5000, goals[0].totalSaved.minorUnits)
        assertEquals("ACTIVE", goals[0].state)
    }

    @Test
    fun `getCachedSavingsGoals returns empty list when none stored`() = runTest {
        coEvery { savingsGoalDao.getByAccountSavingsGoal(any()) } returns emptyList()

        val result = repository.getCachedSavingsGoals("acc-123")

        assertTrue(result is DataResult.Success)
        assertTrue((result as DataResult.Success).data.isEmpty())
    }

    @Test
    fun `getCachedSavingsGoals returns DataResult_Error when DAO throws`() = runTest {
        coEvery { savingsGoalDao.getByAccountSavingsGoal(any()) } throws Exception("IO failure")

        val result = repository.getCachedSavingsGoals("acc-123")

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // getCachedBalance
    // ============================================================

    @Test
    fun `getCachedBalance returns mapped domain balance when stored`() = runTest {
        coEvery { balanceDao.get() } returns balanceEntity

        val result = repository.getCachedBalance()

        assertTrue(result is DataResult.Success)
        val balance = (result as DataResult.Success).data
        assertNotNull(balance)
        assertEquals(25000, balance!!.effectiveBalance.minorUnits)
        assertEquals("GBP", balance.effectiveBalance.currency)
    }

    @Test
    fun `getCachedBalance returns null when no balance cached`() = runTest {
        coEvery { balanceDao.get() } returns null

        val result = repository.getCachedBalance()

        assertTrue(result is DataResult.Success)
        assertNull((result as DataResult.Success).data)
    }

    @Test
    fun `getCachedBalance returns DataResult_Error when DAO throws`() = runTest {
        coEvery { balanceDao.get() } throws Exception("DB failure")

        val result = repository.getCachedBalance()

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // cacheAccounts
    // ============================================================

    @Test
    fun `cacheAccounts inserts mapped AccountEntity list to DAO`() = runTest {
        coEvery { accountDao.insertAll(any()) } just Runs

        val result = repository.cacheAccounts(listOf(domainAccount))

        assertTrue(result is DataResult.Success)
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
    fun `cacheAccounts with empty list inserts empty list`() = runTest {
        coEvery { accountDao.insertAll(any()) } just Runs

        val result = repository.cacheAccounts(emptyList())

        assertTrue(result is DataResult.Success)
        coVerify { accountDao.insertAll(emptyList()) }
    }

    @Test
    fun `cacheAccounts returns DataResult_Error when DAO throws`() = runTest {
        coEvery { accountDao.insertAll(any()) } throws SQLException("insert fail")

        val result = repository.cacheAccounts(listOf(domainAccount))

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // cacheBalance
    // ============================================================

    @Test
    fun `cacheBalance inserts BalanceEntity with correct fields`() = runTest {
        coEvery { balanceDao.insert(any()) } just Runs

        val result = repository.cacheBalance("acc-123", domainBalance)

        assertTrue(result is DataResult.Success)
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

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // cacheTransactions
    // ============================================================

    @Test
    fun `cacheTransactions deletes existing then inserts new transactions`() = runTest {
        coEvery { transactionDao.deleteByAccount("acc-123") } just Runs
        coEvery { transactionDao.insertAll(any()) } just Runs

        val result = repository.cacheTransactions("acc-123", listOf(domainTransaction))

        assertTrue(result is DataResult.Success)
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

        assertEquals(listOf("delete", "insert"), callOrder)
    }

    @Test
    fun `cacheTransactions with empty list clears all transactions for account`() = runTest {
        coEvery { transactionDao.deleteByAccount("acc-123") } just Runs
        coEvery { transactionDao.insertAll(any()) } just Runs

        val result = repository.cacheTransactions("acc-123", emptyList())

        assertTrue(result is DataResult.Success)
        coVerify { transactionDao.deleteByAccount("acc-123") }
        coVerify { transactionDao.insertAll(emptyList()) }
    }

    @Test
    fun `cacheTransactions returns DataResult_Error when delete throws`() = runTest {
        coEvery { transactionDao.deleteByAccount(any()) } throws SQLException("delete fail")

        val result = repository.cacheTransactions("acc-123", listOf(domainTransaction))

        assertTrue(result is DataResult.Error)
    }

    @Test
    fun `cacheTransactions returns DataResult_Error when insert throws`() = runTest {
        coEvery { transactionDao.deleteByAccount(any()) } just Runs
        coEvery { transactionDao.insertAll(any()) } throws SQLException("insert fail")

        val result = repository.cacheTransactions("acc-123", listOf(domainTransaction))

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // cacheSavingsGoals
    // ============================================================

    @Test
    fun `cacheSavingsGoals deletes existing then inserts new goals`() = runTest {
        coEvery { savingsGoalDao.deleteByAccount("acc-123") } just Runs
        coEvery { savingsGoalDao.insertAll(any()) } just Runs

        val result = repository.cacheSavingsGoals("acc-123", listOf(domainSavingsGoal))

        assertTrue(result is DataResult.Success)
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

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // cacheSavingsGoal (single)
    // ============================================================

    @Test
    fun `cacheSavingsGoal inserts a single goal with correct mapping`() = runTest {
        coEvery { savingsGoalDao.insertAll(any()) } just Runs

        val result = repository.cacheSavingsGoal("acc-123", domainSavingsGoal)

        assertTrue(result is DataResult.Success)
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

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // cacheDeleteSavingsGoal
    // ============================================================

    @Test
    fun `cacheDeleteSavingsGoal calls DAO with correct savingsGoalUid`() = runTest {
        coEvery { savingsGoalDao.deleteBySavingsGoalUid("goal-789") } just Runs

        val result = repository.cacheDeleteSavingsGoal("goal-789")

        assertTrue(result is DataResult.Success)
        coVerify { savingsGoalDao.deleteBySavingsGoalUid("goal-789") }
    }

    @Test
    fun `cacheDeleteSavingsGoal returns DataResult_Error when DAO throws`() = runTest {
        coEvery { savingsGoalDao.deleteBySavingsGoalUid(any()) } throws SQLException("delete fail")

        val result = repository.cacheDeleteSavingsGoal("goal-789")

        assertTrue(result is DataResult.Error)
    }

    // ============================================================
    // Multiple accounts — ensures first() is used correctly by use cases
    // ============================================================

    @Test
    fun `getCachedAccounts correctly maps multiple accounts`() = runTest {
        val entities = listOf(
            AccountEntity("acc-1", "Primary", "cat-1"),
            AccountEntity("acc-2", "Secondary", "cat-2"),
        )
        coEvery { accountDao.getAll() } returns entities

        val result = repository.getCachedAccounts()

        assertTrue(result is DataResult.Success)
        assertEquals(2, (result as DataResult.Success).data.size)
        assertEquals("acc-1", result.data[0].accountUid)
        assertEquals("acc-2", result.data[1].accountUid)
    }
}
