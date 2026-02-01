package com.example.roundupapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.roundupapp.data.database.entities.AccountEntity
import com.example.roundupapp.data.database.entities.BalanceEntity
import com.example.roundupapp.data.database.entities.SavingsGoalEntity
import com.example.roundupapp.data.database.entities.TransactionEntity

@Dao
interface AccountDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(accounts: List<AccountEntity>)

  @Query("SELECT * FROM accounts")
  suspend fun getAll(): List<AccountEntity>
}

@Dao
interface TransactionDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<TransactionEntity>)

  @Query("SELECT * FROM transactions WHERE accountUid = :accountUid")
  suspend fun getByAccount(accountUid: String): List<TransactionEntity>

  @Query("DELETE FROM transactions WHERE accountUid = :accountUid")
  suspend fun deleteByAccount(accountUid: String)
}

@Dao
interface SavingsGoalDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(goals: List<SavingsGoalEntity>)

  @Query("SELECT * FROM savings_goals WHERE accountUid = :accountUid")
  suspend fun getBySavingsGoal(accountUid: String): List<SavingsGoalEntity>

  @Query("DELETE FROM savings_goals WHERE accountUid = :accountUid")
  suspend fun deleteByAccount(accountUid: String)
}

@Dao
interface BalanceDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(balance: BalanceEntity)

  @Query("SELECT * FROM balance WHERE id = 0")
  suspend fun get(): BalanceEntity?
}
