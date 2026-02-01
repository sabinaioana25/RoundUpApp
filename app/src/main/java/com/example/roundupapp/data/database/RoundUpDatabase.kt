package com.example.roundupapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    AccountEntity::class,
    TransactionEntity::class,
    SavingsGoalEntity::class,
    BalanceEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class RoundUpDatabase : RoomDatabase() {
  abstract fun accountDao(): AccountDao
  abstract fun transactionDao(): TransactionDao
  abstract fun savingsGoalDao(): SavingsGoalDao
  abstract fun balanceDao(): BalanceDao

  companion object {
    @Volatile
    private var INSTANCE: RoundUpDatabase? = null

    fun getInstance(context: Context): RoundUpDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          RoundUpDatabase::class.java,
          "roundup_db"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
